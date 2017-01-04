/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.irc.client.library.implementation;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.ScheduledFuture;
import org.kitteh.irc.client.library.event.client.ClientConnectionClosedEvent;
import org.kitteh.irc.client.library.event.dcc.DCCConnectedEvent;
import org.kitteh.irc.client.library.event.dcc.DCCConnectionClosedEvent;
import org.kitteh.irc.client.library.event.dcc.DCCFailedEvent;
import org.kitteh.irc.client.library.event.dcc.DCCSocketBoundEvent;
import org.kitteh.irc.client.library.exception.KittehConnectionException;
import org.kitteh.irc.client.library.exception.KittehSTSException;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageType;
import org.kitteh.irc.client.library.feature.sts.STSClientState;
import org.kitteh.irc.client.library.feature.sts.STSMachine;
import org.kitteh.irc.client.library.feature.sts.STSPolicy;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

final class NettyManager {
    static final class ClientConnection {
        private static final int MAX_LINE_LENGTH = 4096;

        private final InternalClient client;
        private final Channel channel;
        private boolean reconnect = true;

        private ScheduledFuture<?> ping;

        private ClientConnection(@Nonnull final InternalClient client, @Nonnull ChannelFuture channelFuture) {
            this.client = client;
            this.channel = channelFuture.channel();

            channelFuture.addListener(future -> {
                if (future.isSuccess()) {
                    this.buildOurFutureTogether();
                    this.client.beginMessageSendingImmediate(this.channel::writeAndFlush);
                } else {
                    this.client.getExceptionListener().queue(new KittehConnectionException(future.cause(), false));
                    this.scheduleReconnect();
                    removeClientConnection(ClientConnection.this, ClientConnection.this.reconnect);
                }
            });
        }

        private void buildOurFutureTogether() {
            addOutputEncoder(this.channel, this.client, true);

            // Handle timeout
            this.channel.pipeline().addLast("[INPUT] Idle state handler", new IdleStateHandler(250, 0, 0));
            this.channel.pipeline().addLast("[INPUT] Catch idle", new ChannelDuplexHandler() {
                @Override
                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                    if (evt instanceof IdleStateEvent) {
                        IdleStateEvent e = (IdleStateEvent) evt;
                        if ((e.state() == IdleState.READER_IDLE) && e.isFirst()) {
                            ClientConnection.this.shutdown(DefaultMessageType.QUIT_PING_TIMEOUT, true);
                        }
                    }
                }
            });

            addInputDecoder(MAX_LINE_LENGTH, this.channel, this.client, this.client::processLine);

            // SSL
            if (this.client.isSSL()) {
                try {
                    File keyCertChainFile = this.client.getConfig().get(Config.SSL_KEY_CERT_CHAIN);
                    File keyFile = this.client.getConfig().get(Config.SSL_KEY);
                    String keyPassword = this.client.getConfig().get(Config.SSL_KEY_PASSWORD);
                    TrustManagerFactory factory = this.client.getConfig().get(Config.SSL_TRUST_MANAGER_FACTORY);
                    if (factory == null) {
                        factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        factory.init((KeyStore) null);
                    }
                    SslContext sslContext = SslContextBuilder.forClient().trustManager(factory).keyManager(keyCertChainFile, keyFile, keyPassword).build();
                    InetSocketAddress addr = this.client.getConfig().getNotNull(Config.SERVER_ADDRESS);
                    // The presence of the two latter arguments enables SNI.
                    final SslHandler sslHandler = sslContext.newHandler(this.channel.alloc(), addr.getHostString(), addr.getPort());
                    sslHandler.handshakeFuture().addListener(handshakeFuture -> {
                        if (!handshakeFuture.isSuccess() && ClientConnection.this.client.getSTSMachine().isPresent()) {
                            STSMachine machine = ClientConnection.this.client.getSTSMachine().get();
                            if (machine.getCurrentState() == STSClientState.STS_PRESENT_RECONNECTING) {
                                ClientConnection.this.shutdown(DefaultMessageType.STS_FAILURE, false);
                                machine.setCurrentState(STSClientState.STS_PRESENT_CANNOT_CONNECT);
                                throw new KittehSTSException("Handshake failure, aborting STS-protected connection attempt.", handshakeFuture.cause());
                            }
                        }
                    });
                    this.channel.pipeline().addFirst(sslHandler);
                } catch (SSLException | NoSuchAlgorithmException | KeyStoreException e) {
                    this.client.getExceptionListener().queue(new KittehConnectionException(e, true));
                    return;
                }
            }

            // Exception handling
            this.channel.pipeline().addLast("[INPUT] Exception handler", new ChannelInboundHandlerAdapter() {
                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    if (cause instanceof Exception) {
                        ClientConnection.this.handleException((Exception) cause);
                    }
                }
            });
            this.channel.pipeline().addFirst("[OUTPUT] Exception handler", new ChannelOutboundHandlerAdapter() {
                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    if (cause instanceof Exception) {
                        ClientConnection.this.handleException((Exception) cause);
                    }
                }
            });

            // Clean up on disconnect
            this.channel.closeFuture().addListener(futureListener -> {
                if (ClientConnection.this.reconnect) {
                    this.scheduleReconnect();
                }
                if (this.ping != null) {
                    this.ping.cancel(true);
                }
                ClientConnection.this.client.getEventManager().callEvent(new ClientConnectionClosedEvent(ClientConnection.this.client, ClientConnection.this.reconnect));
                removeClientConnection(ClientConnection.this, ClientConnection.this.reconnect);
            });
        }

        private void scheduleReconnect() {
            ClientConnection.this.channel.eventLoop().schedule(ClientConnection.this.client::connect, 5, TimeUnit.SECONDS);
        }

        private void handleException(Exception thrown) {
            this.client.getExceptionListener().queue(thrown);
            if (thrown instanceof IOException) {
                this.shutdown(DefaultMessageType.QUIT_INTERNAL_EXCEPTION, true);
            }
        }

        void startSending() {
            this.ping = this.channel.eventLoop().scheduleWithFixedDelay(this.client::ping, 60, 60, TimeUnit.SECONDS);
        }

        void shutdown(DefaultMessageType messageType, boolean reconnect) {
            this.shutdown(this.client.getDefaultMessageMap().getDefault(messageType).orElse(null), reconnect);
        }

        void shutdown(@Nullable String message, boolean reconnect) {
            this.reconnect = reconnect;

            this.client.pauseMessageSending();
            this.channel.writeAndFlush("QUIT" + ((message != null) ? (" :" + message) : ""));
            this.channel.close();
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("client", this.client).toString();
        }
    }

    static class DCCConnection extends ChannelInitializer<SocketChannel> {
        private final ActorProvider.IRCDCCExchange exchange;
        private final InternalClient client;
        private final AtomicBoolean connected = new AtomicBoolean();
        private Channel channel;

        private DCCConnection(ActorProvider.IRCDCCExchange ex, InternalClient client) {
            this.exchange = ex;
            this.client = client;
        }

        @Override
        public void initChannel(SocketChannel channel) throws Exception {
            if (!this.connected.compareAndSet(false, true)) {
                // Only one connection is allowed.
                channel.close();
                return;
            }
            // we can close the server socket now
            channel.parent().close();

            this.channel = channel;
            this.exchange.setNettyChannel(channel);
            NettyManager.dccConnections.computeIfAbsent(this.client, c -> new ArrayList<>()).add(this);

            addOutputEncoder(channel, this.client, false);

            // Inbound & exceptions
            String successHandler = "[INPUT] Success Handler";
            channel.pipeline().addFirst(successHandler, new ChannelInboundHandlerAdapter() {
                @Override
                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                    DCCConnection.this.exchange.setLocalAddress(ctx.channel().localAddress());
                    DCCConnection.this.exchange.setRemoteAddress(ctx.channel().remoteAddress());
                    DCCConnection.this.exchange.setConnected(true);
                    DCCConnection.this.client.getEventManager().callEvent(new DCCConnectedEvent(DCCConnection.this.client, Collections.emptyList(), DCCConnection.this.exchange.snapshot()));
                    ctx.channel().pipeline().remove(this);
                }
            });
            addInputDecoder(Integer.MAX_VALUE, channel, this.client, this.exchange::onMessage);
            channel.pipeline().addFirst("[INPUT] Exception Handler", new ChannelInboundHandlerAdapter() {
                private boolean firstRemove;

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    if (!this.firstRemove) {
                        ctx.channel().pipeline().remove(successHandler);
                        ctx.channel().close().addListener(ft -> {
                            if (ft.isDone()) {
                                DCCConnection.this.client.getEventManager().callEvent(new DCCFailedEvent(DCCConnection.this.client, "Netty exception", cause));
                            }
                        });
                        this.firstRemove = true;
                    }
                    if (cause instanceof Exception) {
                        DCCConnection.this.client.getExceptionListener().queue((Exception) cause);
                    }
                }
            });
            channel.pipeline().addLast("[OUTPUT] Exception Handler", new ChannelOutboundHandlerAdapter() {
                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    if (cause instanceof Exception) {
                        DCCConnection.this.client.getExceptionListener().queue((Exception) cause);
                    }
                }
            });

            channel.closeFuture().addListener(future -> {
                this.exchange.setLocalAddress(null);
                this.exchange.setRemoteAddress(null);
                this.exchange.setConnected(false);
                this.client.getEventManager().callEvent(new DCCConnectionClosedEvent(DCCConnection.this.client, Collections.emptyList(), DCCConnection.this.exchange.snapshot()));
                removeDCCConnection(this);
            });
        }
    }

    @Nullable
    private static Bootstrap bootstrap;
    @Nullable
    private static EventLoopGroup eventLoopGroup;
    private static final Set<ClientConnection> connections = new HashSet<>();
    private static final Map<InternalClient, List<DCCConnection>> dccConnections = new HashMap<>();

    private static void addOutputEncoder(Channel channel, InternalClient client, boolean carriageReturn) {
        // Outbound - Processed in pipeline back to front.
        channel.pipeline().addFirst("[OUTPUT] Output listener", new MessageToMessageEncoder<String>() {
            @Override
            protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
                client.getOutputListener().queue(msg);
                out.add(msg);
            }
        });
        final String linebreak = carriageReturn ? "\r\n" : "\n";
        channel.pipeline().addFirst("[OUTPUT] Add line breaks", new MessageToMessageEncoder<String>() {
            @Override
            protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
                out.add(msg + linebreak);
            }
        });
        channel.pipeline().addFirst("[OUTPUT] String encoder", new StringEncoder(CharsetUtil.UTF_8));
    }

    private static void addInputDecoder(int maxLineLength, Channel channel, InternalClient client, Consumer<String> lineProcessor) {
        // Inbound
        channel.pipeline().addLast("[INPUT] Line splitter", new DelimiterBasedFrameDecoder(maxLineLength, Unpooled.wrappedBuffer(new byte[]{(byte) '\r', (byte) '\n'})));
        channel.pipeline().addLast("[INPUT] String decoder", new StringDecoder(CharsetUtil.UTF_8));
        channel.pipeline().addLast("[INPUT] Send to client", new SimpleChannelInboundHandler<String>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                if (msg == null) {
                    return;
                }
                client.getInputListener().queue(msg);
                lineProcessor.accept(msg);
            }
        });
    }

    private NettyManager() {

    }

    private static synchronized void removeClientConnection(@Nonnull ClientConnection connection, boolean reconnecting) {
        connections.remove(connection);
        shutdownIfFinished(reconnecting);
    }

    private static synchronized void removeDCCConnection(@Nonnull DCCConnection connection) {
        List<DCCConnection> connections = dccConnections.get(connection.client);
        connections.remove(connection);
        if (connections.isEmpty()) {
            dccConnections.remove(connection.client);
        }
        shutdownIfFinished(false);
    }

    private static synchronized void shutdownIfFinished(boolean reconnecting) {
        if (!reconnecting && connections.isEmpty() && dccConnections.isEmpty()) {
            if (eventLoopGroup != null) {
                eventLoopGroup.shutdownGracefully();
            }
            eventLoopGroup = null;
            bootstrap = null;
        }
    }

    static synchronized ClientConnection connect(@Nonnull InternalClient client) {

        // STS Override
        if (client.getSTSMachine().isPresent() && !client.isSSL()) {
            String hostname = client.getConfig().getNotNull(Config.SERVER_ADDRESS).getHostName();
            final STSMachine machine = client.getSTSMachine().get();
            if (machine.getStorageManager().hasEntry(hostname)) {
                STSPolicy policy = machine.getStorageManager().getEntry(hostname).get();
                machine.setSTSPolicy(policy);
                machine.setCurrentState(STSClientState.STS_POLICY_CACHED);
            }
        }

        if (bootstrap == null) {
            bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    // NOOP
                }
            });
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            eventLoopGroup = new NioEventLoopGroup();
            bootstrap.group(eventLoopGroup);
        }
        SocketAddress bind = client.getConfig().get(Config.BIND_ADDRESS);
        SocketAddress server = client.getConfig().getNotNull(Config.SERVER_ADDRESS);
        ClientConnection clientConnection;
        if (bind == null) {
            clientConnection = new ClientConnection(client, bootstrap.connect(server));
        } else {
            clientConnection = new ClientConnection(client, bootstrap.connect(server, bind));
        }
        connections.add(clientConnection);
        return clientConnection;
    }

    static Runnable createDCCServer(InternalClient client, ActorProvider.IRCDCCExchange exchange) {
        Sanity.nullCheck(client.getClientConnection(), "A DCC connection cannot be made without a client connection");
        SocketAddress bind = client.getConfig().get(Config.BIND_ADDRESS);
        if (bind == null) {
            // try binding to the client socket
            bind = client.getClientConnection().channel.localAddress();
            Sanity.nullCheck(bind, "The client connection is not bound, a DCC connection cannot be made");
        }
        ChannelFuture future = new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .childHandler(new DCCConnection(exchange, client))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .group(eventLoopGroup)
                .bind(bind);
        future.addListener(ft -> {
            if (ft.isSuccess()) {
                exchange.setLocalAddress(future.channel().localAddress());
                client.getEventManager().callEvent(new DCCSocketBoundEvent(client, Collections.emptyList(), exchange.snapshot()));
                exchange.onSocketBound();
            } else {
                client.getEventManager().callEvent(new DCCFailedEvent(client, "Failed to bind to address " + future.channel().localAddress(), ft.cause()));
            }
        });
        return () -> future.channel().close();
    }

    static Runnable connectToDCCClient(InternalClient client, ActorProvider.IRCDCCExchange exchange, SocketAddress remoteAddress) {
        Sanity.nullCheck(eventLoopGroup, "A DCC connection cannot be made without a client");
        ChannelFuture future = new Bootstrap()
                .channel(NioSocketChannel.class)
                .handler(new DCCConnection(exchange, client))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .group(eventLoopGroup)
                .connect(remoteAddress);
        future.addListener(ft -> {
            if (!ft.isSuccess()) {
                client.getEventManager().callEvent(new DCCFailedEvent(client, "Failed to connect to address " + remoteAddress, ft.cause()));
            }
        });
        return () -> future.channel().close();
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).toString();
    }
}
