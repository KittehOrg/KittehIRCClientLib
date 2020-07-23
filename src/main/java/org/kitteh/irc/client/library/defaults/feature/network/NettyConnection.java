/*
 * * Copyright (C) 2013-2020 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.defaults.feature.network;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.connection.ClientConnectionClosedEvent;
import org.kitteh.irc.client.library.event.connection.ClientConnectionEndedEvent;
import org.kitteh.irc.client.library.event.connection.ClientConnectionEstablishedEvent;
import org.kitteh.irc.client.library.event.connection.ClientConnectionFailedEvent;
import org.kitteh.irc.client.library.exception.KittehConnectionException;
import org.kitteh.irc.client.library.exception.KittehNagException;
import org.kitteh.irc.client.library.exception.KittehStsException;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageType;
import org.kitteh.irc.client.library.feature.network.ClientConnection;
import org.kitteh.irc.client.library.feature.sts.StsClientState;
import org.kitteh.irc.client.library.feature.sts.StsMachine;
import org.kitteh.irc.client.library.util.HostWithPort;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.SslUtil;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class NettyConnection implements ClientConnection {
    private static final int MAX_LINE_LENGTH = 9001; // 8703 is the limit with IRCv3 latest message tags

    private final Client.WithManagement client;
    private final Channel channel;
    private final Consumer<Client.WithManagement> shutdownHook;

    private boolean reconnect = true;

    private volatile @Nullable ChannelFuture channelFuture;

    private @Nullable ScheduledFuture<?> ping;

    private volatile String lastMessage;
    private volatile Throwable lastCause;

    private boolean alive = true;

    /**
     * Constructs a Netty connection.
     *
     * @param client client for which this exists
     * @param channelFuture channel future from which to handle the connection
     * @param shutdownHook consumer to call when shut down
     */
    public NettyConnection(final Client.@NonNull WithManagement client, @NonNull ChannelFuture channelFuture, @NonNull Consumer<Client.WithManagement> shutdownHook) {
        this.client = Sanity.nullCheck(client, "Client");
        this.channelFuture = Sanity.nullCheck(channelFuture, "Channel future cannot be null");
        this.channel = channelFuture.channel();
        this.shutdownHook = Sanity.nullCheck(shutdownHook, "Shutdown hook cannot be null");

        channelFuture.addListener(future -> {
            NettyConnection.this.channelFuture = null;
            if (future.isSuccess()) {
                this.buildOurFutureTogether();
                this.client.getEventManager().callEvent(new ClientConnectionEstablishedEvent(this.client));
                this.client.beginMessageSendingImmediate(this.channel::writeAndFlush);
            } else {
                NettyConnection.this.alive = false;
                ClientConnectionFailedEvent event = new ClientConnectionFailedEvent(this.client, this.reconnect, future.cause());
                this.client.getEventManager().callEvent(event);
                this.client.getExceptionListener().queue(new KittehConnectionException(future.cause(), false));
                if (event.willAttemptReconnect()) {
                    this.scheduleReconnect(event.getReconnectionDelay());
                }
            }
        });
    }

    private void buildOurFutureTogether() {
        // Outbound - Processed in pipeline back to front.
        this.channel.pipeline().addFirst("[OUTPUT] Output listener", new MessageToMessageEncoder<String>() {
            @Override
            protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) {
                NettyConnection.this.client.getOutputListener().queue(msg);
                out.add(msg);
            }
        });
        this.channel.pipeline().addFirst("[OUTPUT] Add line breaks", new MessageToMessageEncoder<String>() {
            @Override
            protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) {
                out.add(msg + "\r\n");
            }
        });
        this.channel.pipeline().addFirst("[OUTPUT] String encoder", new StringEncoder(CharsetUtil.UTF_8));

        // Handle timeout
        this.channel.pipeline().addLast("[INPUT] Idle state handler", new IdleStateHandler(250, 0, 0));
        this.channel.pipeline().addLast("[INPUT] Catch idle", new ChannelDuplexHandler() {
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                if (evt instanceof IdleStateEvent) {
                    IdleStateEvent e = (IdleStateEvent) evt;
                    if ((e.state() == IdleState.READER_IDLE) && e.isFirst()) {
                        NettyConnection.this.shutdown(DefaultMessageType.QUIT_PING_TIMEOUT, true);
                    }
                }
            }
        });

        // Inbound
        this.channel.pipeline().addLast("[INPUT] Line splitter", new DelimiterBasedFrameDecoder(NettyConnection.MAX_LINE_LENGTH, Unpooled.wrappedBuffer(new byte[]{(byte) '\r', (byte) '\n'})));
        this.channel.pipeline().addLast("[INPUT] String decoder", new StringDecoder(CharsetUtil.UTF_8));
        this.channel.pipeline().addLast("[INPUT] Send to client", new SimpleChannelInboundHandler<String>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                if (msg == null) {
                    return;
                }
                NettyConnection.this.client.getInputListener().queue(msg);
                NettyConnection.this.client.processLine(msg);
                NettyConnection.this.lastMessage = msg;
            }
        });

        // TLS
        if (this.client.isSecureConnection()) {
            try {
                Path keyCertChain = this.client.getSecureKeyCertChain();
                File keyCertChainFile = (keyCertChain == null) ? null : keyCertChain.toFile();
                Path key = this.client.getSecureKey();
                File keyFile = (key == null) ? null : key.toFile();
                String keyPassword = this.client.getSecureKeyPassword();
                TrustManagerFactory factory = this.client.getSecureTrustManagerFactory();
                if (factory == null) {
                    factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    factory.init((KeyStore) null);
                } else if (SslUtil.isInsecure(factory)) {
                    this.client.getExceptionListener().queue(new KittehNagException(String.format("Client '%s' is using an insecure trust manager factory.", this.client)));
                }
                SslContext sslContext = SslContextBuilder.forClient().trustManager(factory).keyManager(keyCertChainFile, keyFile, keyPassword).build();
                HostWithPort addr = this.client.getServerAddress();
                // The presence of the two latter arguments enables SNI.
                final SslHandler sslHandler = sslContext.newHandler(this.channel.alloc(), addr.getHost(), addr.getPort());
                sslHandler.handshakeFuture().addListener(handshakeFuture -> {
                    this.lastCause = null;
                    if (!handshakeFuture.isSuccess()) {
                        if (NettyConnection.this.client.getStsMachine().isPresent()) {
                            StsMachine machine = NettyConnection.this.client.getStsMachine().get();
                            if (machine.getCurrentState() == StsClientState.STS_PRESENT_RECONNECTING) {
                                NettyConnection.this.shutdown(DefaultMessageType.STS_FAILURE, false);
                                machine.setCurrentState(StsClientState.STS_PRESENT_CANNOT_CONNECT);
                                throw new KittehStsException("Handshake failure, aborting STS-protected connection attempt.", handshakeFuture.cause());
                            }
                        } else {
                            this.lastCause = handshakeFuture.cause();
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
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                if (cause instanceof Exception) {
                    NettyConnection.this.handleException((Exception) cause);
                }
            }
        });
        this.channel.pipeline().addFirst("[OUTPUT] Exception handler", new ChannelOutboundHandlerAdapter() {
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                if (cause instanceof Exception) {
                    NettyConnection.this.handleException((Exception) cause);
                }
            }
        });

        // Clean up on disconnect
        this.channel.closeFuture().addListener(future -> {
            if (this.ping != null) {
                this.ping.cancel(true);
            }
            NettyConnection.this.alive = false;
            ClientConnectionEndedEvent event;
            if (this.lastCause == null ) {
                event = new ClientConnectionClosedEvent(this.client, this.reconnect, future.cause(), this.lastMessage);
            } else {
                event = new ClientConnectionFailedEvent(this.client, this.reconnect, this.lastCause);
            }
            NettyConnection.this.client.getEventManager().callEvent(event);
            if (event.willAttemptReconnect()) {
                this.scheduleReconnect(event.getReconnectionDelay());
            }
        });
    }

    private void scheduleReconnect(int delay) {
        NettyConnection.this.channel.eventLoop().schedule(NettyConnection.this.client::connect, delay, TimeUnit.MILLISECONDS);
    }

    private void handleException(Exception thrown) {
        this.client.getExceptionListener().queue(thrown);
        if (thrown instanceof IOException) {
            this.shutdown(DefaultMessageType.QUIT_INTERNAL_EXCEPTION, true);
        }
    }

    @Override
    public boolean isAlive() {
        return this.alive;
    }

    @Override
    public void startPing() {
        this.ping = this.channel.eventLoop().scheduleWithFixedDelay(this.client::ping, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown(DefaultMessageType messageType, boolean reconnect) {
        this.shutdown(this.client.getDefaultMessageMap().getDefault(messageType).orElse(null), reconnect);
    }

    @Override
    public void shutdown(@Nullable String message, boolean reconnect) {
        this.reconnect = reconnect;

        this.client.pauseMessageSending();
        this.channel.writeAndFlush("QUIT" + ((message != null) ? (" :" + message) : ""));
        this.channel.close();
        ChannelFuture future = this.channelFuture;
        if (future != null) {
            future.cancel(true);
            this.shutdownHook.accept(this.client);
        }
        if (!reconnect) {
            this.shutdownHook.accept(this.client);
        }
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).add("client", this.client).toString();
    }
}
