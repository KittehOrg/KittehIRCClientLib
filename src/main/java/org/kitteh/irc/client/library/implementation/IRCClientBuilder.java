/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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

import org.kitteh.irc.client.library.AuthType;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.ClientBuilder;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

final class IRCClientBuilder implements ClientBuilder, Cloneable {
    private Config config;
    private String bindHost;
    private int bindPort;
    private String serverHost;
    private int serverPort = 6667;

    IRCClientBuilder() {
        this.config = new Config();
    }

    @Nonnull
    @Override
    public IRCClientBuilder auth(@Nonnull AuthType authType, @Nonnull String username, @Nonnull String password) {
        Sanity.nullCheck(authType, "Auth type cannot be null!");
        Sanity.nullCheck(username, "Username cannot be null!");
        Sanity.safeMessageCheck(username, "authentication username");
        Sanity.nullCheck(password, "Password cannot be null!");
        Sanity.safeMessageCheck(password, "authentication password");
        this.config.set(Config.AUTH_TYPE, authType);
        this.config.set(Config.AUTH_NAME, username);
        this.config.set(Config.AUTH_PASS, password);
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder bind(@Nullable String host) {
        this.bindHost = host;
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder bind(int port) {
        this.bindPort = this.validPort(port);
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder listenException(@Nullable Consumer<Exception> listener) {
        this.config.set(Config.LISTENER_EXCEPTION, (listener == null) ? null : new Config.ExceptionConsumerWrapper(listener));
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder listenInput(@Nullable Consumer<String> listener) {
        this.config.set(Config.LISTENER_INPUT, (listener == null) ? null : new Config.StringConsumerWrapper(listener));
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder listenOutput(@Nullable Consumer<String> listener) {
        this.config.set(Config.LISTENER_OUTPUT, (listener == null) ? null : new Config.StringConsumerWrapper(listener));
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder name(@Nonnull String name) {
        Sanity.nullCheck(name, "Name cannot be null");
        Sanity.safeMessageCheck(name, "name");
        this.config.set(Config.NAME, name);
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder nick(@Nonnull String nick) {
        Sanity.nullCheck(nick, "Nick cannot be null");
        Sanity.truthiness(!nick.contains(" "), "Nick cannot contain spaces");
        Sanity.safeMessageCheck(nick, "nick");
        this.config.set(Config.NICK, nick);
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder serverPassword(@Nullable String password) {
        if (password != null) {
            Sanity.safeMessageCheck(password, "server password");
        }
        this.config.set(Config.SERVER_PASSWORD, password);
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder realName(@Nonnull String name) {
        Sanity.nullCheck(name, "Real name cannot be null");
        Sanity.safeMessageCheck(name, "real name");
        this.config.set(Config.REAL_NAME, name);
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder secure(boolean ssl) {
        this.config.set(Config.SSL, ssl);
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder secureKeyCertChain(@Nullable File keyCertChainFile) {
        this.config.set(Config.SSL_KEY_CERT_CHAIN, keyCertChainFile);
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder secureKey(@Nullable File keyFile) {
        this.config.set(Config.SSL_KEY, keyFile);
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder secureKeyPassword(@Nullable String password) {
        this.config.set(Config.SSL_KEY_PASSWORD, password);
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder messageDelay(int delay) {
        Sanity.truthiness(delay > 0, "Delay must be at least 1");
        this.config.set(Config.MESSAGE_DELAY, delay);
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder server(int port) {
        this.serverPort = this.validPort(port);
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder server(@Nonnull String host) {
        Sanity.nullCheck(host, "Host cannot be null");
        this.serverHost = host;
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder user(@Nonnull String user) {
        Sanity.nullCheck(user, "User cannot be null");
        Sanity.truthiness(!user.contains(" "), "User cannot contain spaces");
        Sanity.safeMessageCheck(user, "user");
        this.config.set(Config.USER, user);
        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder webirc(@Nonnull String password, @Nonnull String user, @Nonnull String host, @Nonnull InetAddress ip) {
        Sanity.nullCheck(password, "Password cannot be null");
        Sanity.nullCheck(user, "User cannot be null");
        Sanity.nullCheck(host, "Host cannot be null");
        Sanity.nullCheck(ip, "IP cannot be null");

        Sanity.truthiness(!password.contains(" "), "Password cannot contain spaces");
        Sanity.truthiness(!user.contains(" "), "User cannot contain spaces");
        Sanity.truthiness(!host.contains(" "), "Host cannot contain spaces");

        Sanity.safeMessageCheck(password, "password");
        Sanity.safeMessageCheck(user, "user");
        Sanity.safeMessageCheck(host, "host");

        this.config.set(Config.WEBIRC_PASSWORD, password);
        this.config.set(Config.WEBIRC_USER, user);
        this.config.set(Config.WEBIRC_HOST, host);
        this.config.set(Config.WEBIRC_IP, ip);

        return this;
    }

    @Nonnull
    @Override
    public IRCClientBuilder webircDisable() {
        this.config.set(Config.WEBIRC_PASSWORD, null);
        this.config.set(Config.WEBIRC_USER, null);
        this.config.set(Config.WEBIRC_HOST, null);
        this.config.set(Config.WEBIRC_IP, null);
        return this;
    }

    @Nonnull
    @Override
    public Client build() {
        this.inetSet(Config.BIND_ADDRESS, this.bindHost, this.bindPort);
        this.inetSet(Config.SERVER_ADDRESS, this.serverHost, this.serverPort);
        return new IRCClient(this.config);
    }

    @Nonnull
    @Override
    public IRCClientBuilder clone() {
        try {
            IRCClientBuilder clientBuilder = (IRCClientBuilder) super.clone();
            clientBuilder.config = this.config.clone();
            return clientBuilder;
        } catch (CloneNotSupportedException ignored) {
            throw new IllegalStateException("Something has gone horribly wrong");
        }
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("question", "Why would you toString this?").toString();
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