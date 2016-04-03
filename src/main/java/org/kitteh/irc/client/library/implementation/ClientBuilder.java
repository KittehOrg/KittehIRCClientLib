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

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

final class ClientBuilder implements Client.Builder, Cloneable {
    private static final int DEFAULT_SERVER_PORT = 6697;
    private Config config;
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
    public Client.Builder afterBuildConsumerRemove() {
        this.after = null;
        return this;
    }

    @Override
    @Nonnull
    public Client.Builder afterBuildConsumer(@Nonnull Consumer<Client> consumer) {
        Sanity.nullCheck(consumer, "Consumer cannot be null");
        this.after = consumer;
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder bindHostRemove() {
        this.bindHost = null;
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder bindHost(@Nonnull String host) {
        Sanity.nullCheck(host, "Host cannot be null");
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
    public ClientBuilder listenExceptionRemove() {
        this.config.set(Config.LISTENER_EXCEPTION, null);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder listenException(@Nonnull Consumer<Exception> listener) {
        Sanity.nullCheck(listener, "Listener cannot be null");
        this.config.set(Config.LISTENER_EXCEPTION, new Config.ExceptionConsumerWrapper(listener));
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder listenInputRemove() {
        this.config.set(Config.LISTENER_INPUT, null);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder listenInput(@Nonnull Consumer<String> listener) {
        Sanity.nullCheck(listener, "Listener cannot be null");
        this.config.set(Config.LISTENER_INPUT, new Config.StringConsumerWrapper(listener));
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder listenOutputRemove() {
        this.config.set(Config.LISTENER_OUTPUT, null);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder listenOutput(@Nonnull Consumer<String> listener) {
        Sanity.nullCheck(listener, "Listener cannot be null");
        this.config.set(Config.LISTENER_OUTPUT, new Config.StringConsumerWrapper(listener));
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder name(@Nonnull String name) {
        Sanity.safeMessageCheck(name, "Name");
        this.config.set(Config.NAME, name);
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
    public ClientBuilder serverPasswordRemove() {
        this.config.set(Config.SERVER_PASSWORD, null);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder serverPassword(@Nonnull String password) {
        Sanity.safeMessageCheck(password, "Server password");
        this.config.set(Config.SERVER_PASSWORD, password);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder realName(@Nonnull String name) {
        Sanity.safeMessageCheck(name, "Real name");
        this.config.set(Config.REAL_NAME, name);
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
    public ClientBuilder secureKeyCertChainRemove() {
        this.config.set(Config.SSL_KEY_CERT_CHAIN, null);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder secureKeyCertChain(@Nonnull File keyCertChainFile) {
        Sanity.nullCheck(keyCertChainFile, "Key certificate chain file cannot be null");
        this.config.set(Config.SSL_KEY_CERT_CHAIN, keyCertChainFile);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder secureKeyRemove() {
        this.config.set(Config.SSL_KEY, null);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder secureKey(@Nonnull File keyFile) {
        Sanity.nullCheck(keyFile, "Key file cannot be null");
        this.config.set(Config.SSL_KEY, keyFile);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder secureKeyPasswordRemove() {
        this.config.set(Config.SSL_KEY_PASSWORD, null);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder secureKeyPassword(@Nonnull String password) {
        Sanity.nullCheck(password, "Password cannot be null");
        this.config.set(Config.SSL_KEY_PASSWORD, password);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder secureTrustManagerFactoryRemove() {
        this.config.set(Config.SSL_TRUST_MANAGER_FACTORY, null);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder secureTrustManagerFactory(@Nonnull TrustManagerFactory factory) {
        Sanity.nullCheck(factory, "Factory cannot be null");
        this.config.set(Config.SSL_TRUST_MANAGER_FACTORY, factory);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder messageDelay(int delay) {
        Sanity.truthiness(delay > 0, "Delay must be at least 1");
        this.config.set(Config.MESSAGE_DELAY, delay);
        return this;
    }

    @Nonnull
    @Override
    public ClientBuilder serverHost(@Nonnull String host) {
        Sanity.nullCheck(host, "Host cannot be null");
        this.serverHost = host;
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
        this.config.set(Config.WEBIRC_PASSWORD, null);
        this.config.set(Config.WEBIRC_USER, null);
        this.config.set(Config.WEBIRC_HOST, null);
        this.config.set(Config.WEBIRC_IP, null);
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
        this.updateInetEntries();
        IRCClient client = new IRCClient(this.config.clone());
        if (this.after != null) {
            this.after.accept(client);
        }
        client.connect();
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
