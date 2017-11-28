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
import org.kitteh.irc.client.library.event.client.ClientConnectionEstablishedEvent;
import org.kitteh.irc.client.library.event.client.ClientConnectionFailedEvent;
import org.kitteh.irc.client.library.exception.KittehConnectionException;
import org.kitteh.irc.client.library.exception.KittehNagException;
import org.kitteh.irc.client.library.exception.KittehSTSException;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageType;
import org.kitteh.irc.client.library.feature.sts.STSClientState;
import org.kitteh.irc.client.library.feature.sts.STSMachine;
import org.kitteh.irc.client.library.feature.sts.STSPolicy;
import org.kitteh.irc.client.library.util.AcceptingTrustManagerFactory;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

final class NettyManager {
    static final class ClientConnection {
        private static final int MAX_LINE_LENGTH = 4096;

        private final InternalClient client;
        private final Channel channel;
        private boolean reconnect = true;

        @Nullable
        private volatile ChannelFuture channelFuture;

        private ScheduledFuture<?> ping;

        private volatile String lastMessage;

        private ClientConnection(@Nonnull final InternalClient client, @Nonnull ChannelFuture channelFuture) {
            this.client = client;
            this.channel = channelFuture.channel();
            this.channelFuture = channelFuture;

            channelFuture.addListener(future -> {
                ClientConnection.this.channelFuture = null;
                if (future.isSuccess()) {
                    this.buildOurFutureTogether();
                    this.client.getEventManager().callEvent(new ClientConnectionEstablishedEvent(this.client));
                    this.client.beginMessageSendingImmediate(this.channel::writeAndFlush);
                } else {
                    @Nullable final Exception exception = (future.cause() instanceof Exception) ? (Exception) future.cause() : null;
                    ClientConnectionFailedEvent event = new ClientConnectionFailedEvent(this.client, this.reconnect, exception);
                    this.client.getEventManager().callEvent(event);
                    this.client.getExceptionListener().queue(new KittehConnectionException(future.cause(), false));
                    if (event.isTryingReconnection()) {
                        this.scheduleReconnect(event.getReconnectionDelay());
                    }
                }
            });
        }

        private void buildOurFutureTogether() {
            // Outbound - Processed in pipeline back to front.
            this.channel.pipeline().addFirst("[OUTPUT] Output listener", new MessageToMessageEncoder<String>() {
                @Override
                protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
                    ClientConnection.this.client.getOutputListener().queue(msg);
                    out.add(msg);
                }
            });
            this.channel.pipeline().addFirst("[OUTPUT] Add line breaks", new MessageToMessageEncoder<String>() {
                @Override
                protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
                    out.add(msg + "\r\n");
                }
            });
            this.channel.pipeline().addFirst("[OUTPUT] String encoder", new StringEncoder(CharsetUtil.UTF_8));

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

            // Inbound
            this.channel.pipeline().addLast("[INPUT] Line splitter", new DelimiterBasedFrameDecoder(MAX_LINE_LENGTH, Unpooled.wrappedBuffer(new byte[]{(byte) '\r', (byte) '\n'})));
            this.channel.pipeline().addLast("[INPUT] String decoder", new StringDecoder(CharsetUtil.UTF_8));
            this.channel.pipeline().addLast("[INPUT] Send to client", new SimpleChannelInboundHandler<String>() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                    if (msg == null) {
                        return;
                    }
                    ClientConnection.this.client.getInputListener().queue(msg);
                    ClientConnection.this.client.processLine(msg);
                    ClientConnection.this.lastMessage = msg;
                }
            });

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
                    } else if (AcceptingTrustManagerFactory.isInsecure(factory)) {
                        this.client.getExceptionListener().queue(new KittehNagException(String.format("Client '%s' is using an insecure trust manager factory.", this.client)));
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
            this.channel.closeFuture().addListener(future -> {
                if (this.ping != null) {
                    this.ping.cancel(true);
                }
                @Nullable final Exception exception = (future.cause() instanceof Exception) ? (Exception) future.cause() : null;
                ClientConnectionClosedEvent event = new ClientConnectionClosedEvent(ClientConnection.this.client, ClientConnection.this.reconnect, exception, this.lastMessage);
                ClientConnection.this.client.getEventManager().callEvent(event);
                if (event.isTryingReconnection()) {
                    this.scheduleReconnect(event.getReconnectionDelay());
                }
            });
        }

        private void scheduleReconnect(int delay) {
            ClientConnection.this.channel.eventLoop().schedule(ClientConnection.this.client::connect, delay, TimeUnit.MILLISECONDS);
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
            ChannelFuture future = this.channelFuture;
            if (future != null) {
                future.cancel(true);
                removeClientConnection(this.client);
            }
            if (!reconnect) {
                removeClientConnection(this.client);
            }
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("client", this.client).toString();
        }
    }

    @Nullable
    private static Bootstrap bootstrap;
    @Nullable
    private static EventLoopGroup eventLoopGroup;
    private static final Set<InternalClient> clients = new HashSet<>();

    private NettyManager() {

    }

    private static synchronized void removeClientConnection(@Nonnull InternalClient client) {
        clients.remove(client);
        if (clients.isEmpty()) {
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
        clients.add(client);
        return clientConnection;
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).toString();
    }
}
