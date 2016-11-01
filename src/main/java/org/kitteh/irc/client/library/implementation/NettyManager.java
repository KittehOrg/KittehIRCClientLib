/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.ScheduledFuture;
import org.kitteh.irc.client.library.event.client.ClientConnectionClosedEvent;
import org.kitteh.irc.client.library.exception.KittehConnectionException;
import org.kitteh.irc.client.library.exception.KittehSTSException;
import org.kitteh.irc.client.library.feature.sts.STSClientState;
import org.kitteh.irc.client.library.feature.sts.STSMachine;
import org.kitteh.irc.client.library.feature.sts.STSPolicy;
import org.kitteh.irc.client.library.util.QueueProcessingThread;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

final class NettyManager {
    static final class ClientConnection {
        private static final int MAX_LINE_LENGTH = 2048;

        private final InternalClient client;
        private final Channel channel;
        private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        private boolean reconnect = true;
        private Future<?> scheduledSending;
        private ScheduledFuture<?> scheduledPing;
        private final Object scheduledSendingLock = new Object();
        private final Object immediateSendingLock = new Object();
        private boolean immediateSendingReady = false;
        private final QueueProcessingThread<String> immediateSending;

        private ClientConnection(@Nonnull final InternalClient client, @Nonnull ChannelFuture channelFuture) {
            this.client = client;
            this.channel = channelFuture.channel();

            this.immediateSending = new QueueProcessingThread<String>("Kitteh IRC Client Immediate Sending Queue (" + client.getName() + ')') {
                @Override
                protected void processElement(String message) {
                    synchronized (ClientConnection.this.immediateSendingLock) {
                        if (!ClientConnection.this.immediateSendingReady) {
                            try {
                                ClientConnection.this.immediateSendingLock.wait();
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                        ClientConnection.this.channel.writeAndFlush(message);
                    }
                }
            };

            channelFuture.addListener(future -> {
                if (future.isSuccess()) {
                    this.buildOurFutureTogether();
                    synchronized (ClientConnection.this.immediateSendingLock) {
                        this.immediateSendingReady = true;
                        this.immediateSendingLock.notify();
                    }
                } else {
                    this.client.getExceptionListener().queue(new KittehConnectionException(future.cause(), false));
                    this.scheduleReconnect();
                    removeClientConnection(ClientConnection.this, ClientConnection.this.reconnect);
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
                            ClientConnection.this.shutdown("Reconnecting...", true);
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
                }
            });

            // SSL
            if (this.client.getConfig().getNotNull(Config.SSL)) {
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
                    final SslHandler sslHandler = sslContext.newHandler(this.channel.alloc(), addr.getHostString(),
                            addr.getPort());
                    sslHandler.handshakeFuture().addListener(new FutureListener<Channel>(){

                        @Override
                        public void operationComplete(Future<Channel> handshakeFuture) throws Exception {
                            if (!handshakeFuture.isSuccess() && ClientConnection.this.client.getSTSMachine().isPresent()) {
                                STSMachine machine = ClientConnection.this.client.getSTSMachine().get();
                                if (machine.getCurrentState() == STSClientState.STS_PRESENT_RECONNECTING) {
                                    ClientConnection.this.shutdown("Cannot connect securely", false);
                                    machine.setCurrentState(STSClientState.STS_PRESENT_CANNOT_CONNECT);
                                    throw new KittehSTSException("Handshake failure, aborting STS-protected connection attempt.", handshakeFuture.cause());

                                }
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
                this.immediateSending.interrupt();
                ClientConnection.this.client.getEventManager().callEvent(new ClientConnectionClosedEvent(ClientConnection.this.client, ClientConnection.this.reconnect));
                removeClientConnection(ClientConnection.this, ClientConnection.this.reconnect);
            });
        }

        private void scheduleReconnect() {
            ClientConnection.this.channel.eventLoop().schedule(ClientConnection.this.client::connect, 5, TimeUnit.SECONDS);
        }

        void sendMessage(@Nonnull String message, boolean priority) {
            this.sendMessage(message, priority, false);
        }

        void sendMessage(@Nonnull String message, boolean priority, boolean avoidDuplicates) {
            if (priority) {
                this.immediateSending.queue(message);
            } else if (!avoidDuplicates || !this.queue.contains(message)) {
                this.queue.add(message);
            }
        }

        void shutdown(@Nullable String message) {
            this.shutdown(message, false);
        }

        void startSending() {
            this.schedule(true);
        }

        void updateScheduling() {
            this.schedule(false);
        }

        private void handleException(Exception thrown) {
            this.client.getExceptionListener().queue(thrown);
            if (thrown instanceof IOException) {
                this.shutdown("IO Error. Reconnecting...", true);
            }
        }

        private void schedule(boolean force) {
            synchronized (this.scheduledSendingLock) {
                if (!force && (this.scheduledSending == null)) {
                    return;
                }
                if (this.scheduledPing == null) {
                    this.scheduledPing = this.channel.eventLoop().scheduleWithFixedDelay(this.client::ping, 60, 60, TimeUnit.SECONDS);
                }
                if (this.scheduledSending == null) {
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            String message;
                            try {
                                message = ClientConnection.this.queue.take();
                            } catch (InterruptedException e) {
                                return;
                            }
                            if (message != null) {
                                ClientConnection.this.channel.writeAndFlush(message);
                            }
                            int delay = ClientConnection.this.client.getMessageDelay();
                            if (delay == 0) {
                                ClientConnection.this.scheduledSending = ClientConnection.this.channel.eventLoop().submit(this);
                            } else {
                                ClientConnection.this.scheduledSending = ClientConnection.this.channel.eventLoop().schedule(this, delay, TimeUnit.MILLISECONDS);
                            }
                        }
                    };
                    int delay = ClientConnection.this.client.getMessageDelay();
                    ClientConnection.this.channel.eventLoop().schedule(runnable, delay, TimeUnit.MILLISECONDS);
                }
            }
        }

        void shutdown(@Nullable String message, boolean reconnect) {
            this.reconnect = reconnect;

            this.sendMessage("QUIT" + ((message != null) ? (" :" + message) : ""), true);
            this.channel.close();
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
    private static final Set<ClientConnection> connections = new HashSet<>();

    private NettyManager() {

    }

    private static synchronized void removeClientConnection(@Nonnull ClientConnection connection, boolean reconnecting) {
        connections.remove(connection);
        if (!reconnecting && connections.isEmpty()) {
            if (eventLoopGroup != null) {
                eventLoopGroup.shutdownGracefully();
            }
            eventLoopGroup = null;
            bootstrap = null;
        }
    }

    static synchronized ClientConnection connect(@Nonnull InternalClient client) {

        // STS Override
        if (client.getSTSMachine().isPresent() && !client.getConfig().getNotNull(Config.SSL)) {
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

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).toString();
    }
}
