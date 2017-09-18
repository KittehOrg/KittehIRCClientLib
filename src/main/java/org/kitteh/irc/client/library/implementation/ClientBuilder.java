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

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.AuthManager;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageMap;
import org.kitteh.irc.client.library.feature.sending.MessageSendingQueue;
import org.kitteh.irc.client.library.feature.sts.STSStorageManager;
import org.kitteh.irc.client.library.util.AcceptingTrustManagerFactory;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.function.Consumer;
import java.util.function.Function;

final class ClientBuilder implements Client.Builder, Cloneable {
    private static final int DEFAULT_SERVER_PORT = 6697;
    private Config config;
    @Deprecated
    private boolean connectWhenBuilt = true;
    @Nullable
    private Consumer<Client> after;
    @Nullable
    private String bindHost;
    private int bindPort;
    private String serverHost;
    private int serverPort = DEFAULT_SERVER_PORT;

    ClientBuilder() {
        this.config = new Config();
    }

    @Override
    @Nonnull
    public Client.Builder afterBuildConsumer(@Nullable Consumer<Client> consumer) {
        this.after = consumer;
        return this;
    }

    @Override
    @Nonnull
    public Client.Builder authManagerSupplier(@Nonnull Function<Client, ? extends AuthManager> supplier) {
        this.config.set(Config.MANAGER_AUTH, Sanity.nullCheck(supplier, "Supplier cannot be null"));
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder bindHost(@Nullable String host) {
        this.bindHost = host;
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder bindPort(int port) {
        this.bindPort = this.validPort(port);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder capabilityManagerSupplier(@Nonnull Function<Client, ? extends CapabilityManager.WithManagement> supplier) {
        this.config.set(Config.MANAGER_CAPABILITY, Sanity.nullCheck(supplier, "Supplier cannot be null"));
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder defaultMessageMap(@Nonnull DefaultMessageMap defaultMessageMap) {
        this.config.set(Config.DEFAULT_MESSAGE_MAP, defaultMessageMap);
        return this;
    }

    @Nonnull
    @Override
    public Client.Builder connectWhenBuilt(boolean connect) {
        this.connectWhenBuilt = connect;
        return this;
    }

    @Override
    @Nonnull
    public Client.Builder eventManagerSupplier(@Nonnull Function<Client, ? extends EventManager> supplier) {
        this.config.set(Config.MANAGER_EVENT, Sanity.nullCheck(supplier, "Supplier cannot be null"));
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder listenException(@Nullable Consumer<Exception> listener) {
        this.config.set(Config.LISTENER_EXCEPTION, (listener == null) ? null : new Config.ExceptionConsumerWrapper(listener));
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder listenInput(@Nullable Consumer<String> listener) {
        this.config.set(Config.LISTENER_INPUT, (listener == null) ? null : new Config.StringConsumerWrapper(listener));
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder listenOutput(@Nullable Consumer<String> listener) {
        this.config.set(Config.LISTENER_OUTPUT, (listener == null) ? null : new Config.StringConsumerWrapper(listener));
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder messageSendingQueueSupplier(@Nonnull Function<Client, ? extends MessageSendingQueue> supplier) {
        this.config.set(Config.MESSAGE_DELAY, Sanity.nullCheck(supplier, "Supplier cannot be null"));
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder name(@Nonnull String name) {
        this.config.set(Config.NAME, Sanity.safeMessageCheck(name, "Name"));
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder nick(@Nonnull String nick) {
        Sanity.safeMessageCheck(nick, "Nick");
        Sanity.truthiness(!nick.contains(" "), "Nick cannot contain spaces");
        this.config.set(Config.NICK, nick);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder queryChannelInformation(boolean query) {
        this.config.set(Config.QUERY_CHANNEL_INFO, query);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder serverPassword(@Nullable String password) {
        this.config.set(Config.SERVER_PASSWORD, password);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder realName(@Nonnull String name) {
        this.config.set(Config.REAL_NAME, Sanity.safeMessageCheck(name, "Real name"));
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder secure(boolean ssl) {
        this.config.set(Config.SSL, ssl);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder secureKeyCertChain(@Nullable File keyCertChainFile) {
        this.config.set(Config.SSL_KEY_CERT_CHAIN, keyCertChainFile);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder secureKey(@Nullable File keyFile) {
        this.config.set(Config.SSL_KEY, keyFile);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder secureKeyPassword(@Nullable String password) {
        this.config.set(Config.SSL_KEY_PASSWORD, password);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder secureTrustManagerFactory(@Nullable TrustManagerFactory factory) {
        this.config.set(Config.SSL_TRUST_MANAGER_FACTORY, factory);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder serverHost(@Nonnull String host) {
        this.serverHost = Sanity.nullCheck(host, "Host cannot be null");
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder serverPort(int port) {
        this.serverPort = this.validPort(port);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder user(@Nonnull String user) {
        Sanity.safeMessageCheck(user, "User");
        Sanity.truthiness(!user.contains(" "), "User cannot contain spaces");
        this.config.set(Config.USER, user);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder webirc(@Nonnull String password, @Nonnull String user, @Nonnull String host, @Nonnull InetAddress ip) {
        Sanity.safeMessageCheck(password, "Password");
        Sanity.safeMessageCheck(user, "User");
        Sanity.safeMessageCheck(host, "Host");
        Sanity.nullCheck(ip, "IP cannot be null");

        Sanity.truthiness(!password.contains(" "), "Password cannot contain spaces");
        Sanity.truthiness(!user.contains(" "), "User cannot contain spaces");
        Sanity.truthiness(!host.contains(" "), "Host cannot contain spaces");

        this.config.set(Config.WEBIRC_PASSWORD, password);
        this.config.set(Config.WEBIRC_USER, user);
        this.config.set(Config.WEBIRC_HOST, host);
        this.config.set(Config.WEBIRC_IP, ip);

        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder webircRemove() {
        this.config.reset(Config.WEBIRC_PASSWORD);
        this.config.reset(Config.WEBIRC_USER);
        this.config.reset(Config.WEBIRC_HOST);
        this.config.reset(Config.WEBIRC_IP);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder stsStorageManager(@Nullable STSStorageManager storageManager) {
        this.config.set(Config.STS_STORAGE_MANAGER, storageManager);
        return this;
    }

    @Override
    public ClientBuilder reset() {
        this.after = null;
        this.bindHost = null;
        this.bindPort = 0;
        this.serverHost = null;
        this.serverPort = DEFAULT_SERVER_PORT;
        this.config.reset();
        return this;
    }

    @Nonnull
    @Override
    public Client build() {
        final Client client = this.clientPlease();
        if (this.connectWhenBuilt) {
            client.connect();
        }
        return client;
    }

    @Nonnull
    @Override
    public Client buildAndConnect() {
        final Client client = this.clientPlease();
        client.connect();
        return client;
    }

    @Nonnull
    private Client clientPlease() {
        if (this.config.get(Config.STS_STORAGE_MANAGER) != null) {
            final TrustManagerFactory factory = this.config.get(Config.SSL_TRUST_MANAGER_FACTORY);
            Sanity.truthiness(!AcceptingTrustManagerFactory.isInsecure(factory), "Cannot use STS with an insecure trust manager.");
        }

        this.updateInetEntries();
        final Client client = new IRCClient(this.config.clone());
        if (this.after != null) {
            this.after.accept(client);
        }
        return client;
    }

    @Nonnull
    @Override
    public ClientBuilder clone() {
        try {
            ClientBuilder clientBuilder = (ClientBuilder) super.clone();
            clientBuilder.config = this.config.clone();
            return clientBuilder;
        } catch (CloneNotSupportedException ignored) {
            throw new IllegalStateException("Something has gone horribly wrong");
        }
    }

    @Nonnull
    @Override
    public String toString() {
        this.updateInetEntries();
        return new ToStringer(this).add("afterBuildConsumer", this.after).add("config", this.config).toString();
    }

    private void updateInetEntries() {
        this.inetSet(Config.BIND_ADDRESS, this.bindHost, this.bindPort);
        this.inetSet(Config.SERVER_ADDRESS, this.serverHost, this.serverPort);
    }

    private void inetSet(@Nonnull Config.Entry<InetSocketAddress> entry, @Nullable String host, int port) {
        if (host != null) {
            this.config.set(entry, new InetSocketAddress(host, port));
        } else if (port > 0) {
            this.config.set(entry, new InetSocketAddress(port));
        }
    }

    /**
     * Gets a valid port number from a potentially invalid port number.
     * <p>
     * Returns the valid port, or 0 if invalid.
     *
     * @param port port provided
     * @return valid port
     */
    private int validPort(int port) {
        return ((port > 65535) || (port < 0)) ? 0 : port;
    }
}
