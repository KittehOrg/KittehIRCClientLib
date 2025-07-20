/*
 * * Copyright (C) 2013-2025 Matt Baxter https://kitteh.org
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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.network.NetworkHandler;
import org.kitteh.irc.client.library.feature.network.Resolver;
import org.kitteh.irc.client.library.feature.sts.StsClientState;
import org.kitteh.irc.client.library.feature.sts.StsMachine;
import org.kitteh.irc.client.library.feature.sts.StsPolicy;
import org.kitteh.irc.client.library.util.HostWithPort;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Netty connectivity management.
 */
public class NettyNetworkHandler implements NetworkHandler {
    private static final NettyNetworkHandler instance = new NettyNetworkHandler();

    /**
     * Gets the single instance of this class.
     *
     * @return instance
     */
    public static @NonNull NettyNetworkHandler getInstance() {
        return NettyNetworkHandler.instance;
    }

    private @Nullable EventLoopGroup eventLoopGroup;
    private final Set<Client.WithManagement> clients = new HashSet<>();
    private Resolver resolver = new JavaResolver();

    private NettyNetworkHandler() {
        // NOOP
    }

    /**
     * Removes a client connection, and should be called by the connection.
     *
     * @param client client for whom to remove the connection
     */
    private synchronized void removeClientConnection(Client.@NonNull WithManagement client) {
        this.clients.remove(client);
        if (this.clients.isEmpty()) {
            if (this.eventLoopGroup != null) {
                this.eventLoopGroup.shutdownGracefully();
            }
            this.eventLoopGroup = null;
        }
    }

    @Override
    public synchronized @NonNull NettyConnection connect(Client.@NonNull WithManagement client) {
        // STS Override
        if (client.getStsMachine().isPresent() && !client.isSecureConnection()) {
            String hostname = client.getServerAddress().getHost();
            final StsMachine machine = client.getStsMachine().get();
            Optional<StsPolicy> policy = machine.getStorageManager().getEntry(hostname);
            if (policy.isPresent()) {
                machine.setStsPolicy(policy.get());
                machine.setCurrentState(StsClientState.STS_POLICY_CACHED);
            }
        }

        if (this.eventLoopGroup == null) {
            this.eventLoopGroup = new NioEventLoopGroup();
        }

        final Bootstrap bootstrap = new Bootstrap()
                .channel(NioSocketChannel.class)
                .group(this.eventLoopGroup)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) {
                        if (client.getProxyType().isPresent() && client.getProxyAddress().isPresent()) {
                            ChannelPipeline pipe = channel.pipeline();
                            HostWithPort proxyHostWithPort = client.getProxyAddress().get();
                            switch (client.getProxyType().get()) {
                                case SOCKS_5:
                                    pipe.addLast(new Socks5ProxyHandler(new InetSocketAddress(proxyHostWithPort.getHost(), proxyHostWithPort.getPort())));
                                    break;
                                case SOCKS_4:
                                    pipe.addLast(new Socks4ProxyHandler(new InetSocketAddress(proxyHostWithPort.getHost(), proxyHostWithPort.getPort())));
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unsupported proxy type: " + client.getProxyType());
                            }
                        }
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true);

        SocketAddress bind = client.getBindAddress();

        final String host = client.getServerAddress().getHost();
        SocketAddress server;
        try {
            InetAddress address = this.resolver.getAddress(host);
            server = new InetSocketAddress(address, client.getServerAddress().getPort());
        } catch (UnknownHostException e) {
            server = InetSocketAddress.createUnresolved(host, client.getServerAddress().getPort());
        }

        NettyConnection clientConnection = new NettyConnection(client, bootstrap.connect(server, bind), this::removeClientConnection);
        this.clients.add(client);
        return clientConnection;
    }

    @Override
    public @NonNull Resolver getResolver() {
        return this.resolver;
    }

    @Override
    public void setResolver(@NonNull Resolver resolver) {
        this.resolver = Sanity.nullCheck(resolver, "Resolver");
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).toString();
    }
}
