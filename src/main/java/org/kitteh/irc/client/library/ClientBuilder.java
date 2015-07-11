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
package org.kitteh.irc.client.library;

import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

/**
 * Builds {@link Client}s.
 */
public final class ClientBuilder implements Cloneable {
    private Config config;
    private String bindHost;
    private int bindPort;
    private String serverHost;
    private int serverPort = 6667;

    /**
     * Creates a ClientBuilder!
     */
    public ClientBuilder() {
        this.config = new Config();
    }

    /**
     * Sets values for authentication with services on the server.
     *
     * @param authType type of authentication (See {@link AuthType})
     * @param username username
     * @param password password
     * @return this builder
     * @throws IllegalArgumentException for null parameters
     * @see AuthManager for managing authentication later
     */
    @Nonnull
    public ClientBuilder auth(@Nonnull AuthType authType, @Nonnull String username, @Nonnull String password) {
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

    /**
     * Binds the client to a host or IP locally. Null for wildcard binding.
     * <p>
     * By default, the host is null for wildcard binding.
     *
     * @param host host to bind to or null for wildcard
     * @return this builder
     */
    @Nonnull
    public ClientBuilder bind(@Nullable String host) {
        this.bindHost = host;
        return this;
    }

    /**
     * Binds the client to the specified port. Invalid ports are set to 0.
     * <p>
     * By default, the port is 0.
     *
     * @param port port to bind to
     * @return this builder
     */
    @Nonnull
    public ClientBuilder bind(int port) {
        this.bindPort = this.validPort(port);
        return this;
    }

    /**
     * Sets a listener for all thrown exceptions on this client.
     * <p>
     * All exceptions are passed from a single, separate thread.
     *
     * @param listener catcher of throwable objects
     * @return this builder
     */
    @Nonnull
    public ClientBuilder listenException(@Nullable Consumer<Exception> listener) {
        this.config.set(Config.LISTENER_EXCEPTION, (listener == null) ? null : new Config.ExceptionConsumerWrapper(listener));
        return this;
    }

    /**
     * Sets a listener for all incoming messages from the server.
     * <p>
     * All messages are passed from a single, separate thread.
     *
     * @param listener input listener
     * @return this builder
     */
    @Nonnull
    public ClientBuilder listenInput(@Nullable Consumer<String> listener) {
        this.config.set(Config.LISTENER_INPUT, (listener == null) ? null : new Config.StringConsumerWrapper(listener));
        return this;
    }

    /**
     * Sets a listener for all outgoing messages to the server.
     * <p>
     * All messages are passed from a single, separate thread.
     *
     * @param listener output listener
     * @return this builder
     */
    @Nonnull
    public ClientBuilder listenOutput(@Nullable Consumer<String> listener) {
        this.config.set(Config.LISTENER_OUTPUT, (listener == null) ? null : new Config.StringConsumerWrapper(listener));
        return this;
    }

    /**
     * Names the client, for internal labeling.
     *
     * @param name a name to label the client internally
     * @return this builder
     * @throws IllegalArgumentException if name is null
     */
    @Nonnull
    public ClientBuilder name(@Nonnull String name) {
        Sanity.nullCheck(name, "Name cannot be null");
        Sanity.safeMessageCheck(name, "name");
        this.config.set(Config.NAME, name);
        return this;
    }

    /**
     * Sets the client's nick.
     * <p>
     * By default, the nick is Kitteh.
     *
     * @param nick nick for the client to use
     * @return this builder
     * @throws IllegalArgumentException if nick is null
     */
    @Nonnull
    public ClientBuilder nick(@Nonnull String nick) {
        Sanity.nullCheck(nick, "Nick cannot be null");
        Sanity.truthiness(!nick.contains(" "), "Nick cannot contain spaces");
        Sanity.safeMessageCheck(nick, "nick");
        this.config.set(Config.NICK, nick);
        return this;
    }

    /**
     * Sets the server password.
     * <p>
     * If not set, no password is sent
     *
     * @param password server password
     * @return this builder
     */
    @Nonnull
    public ClientBuilder serverPassword(@Nullable String password) {
        if (password != null) {
            Sanity.safeMessageCheck(password, "server password");
        }
        this.config.set(Config.SERVER_PASSWORD, password);
        return this;
    }

    /**
     * Sets the realname the client uses.
     * <p>
     * By default, the realname is Kitteh.
     *
     * @param name realname to use
     * @return this builder
     * @throws IllegalArgumentException for null realname
     */
    @Nonnull
    public ClientBuilder realName(@Nonnull String name) {
        Sanity.nullCheck(name, "Real name cannot be null");
        Sanity.safeMessageCheck(name, "real name");
        this.config.set(Config.REAL_NAME, name);
        return this;
    }

    /**
     * Sets whether the client connects via SSL.
     *
     * @param ssl true for ssl
     * @return this builder
     */
    @Nonnull
    public ClientBuilder secure(boolean ssl) {
        this.config.set(Config.SSL, ssl);
        return this;
    }

    /**
     * Sets the public key for SSL connection.
     *
     * @param keyCertChainFile X.509 certificate chain file in PEM format
     * @return this builder
     * @see #secure(boolean)
     */
    @Nonnull
    public ClientBuilder secureKeyCertChain(@Nullable File keyCertChainFile) {
        this.config.set(Config.SSL_KEY_CERT_CHAIN, keyCertChainFile);
        return this;
    }

    /**
     * Sets the private key for SSL connection.
     *
     * @param keyFile PKCS#8 private key file in PEM format
     * @return this builder
     * @see #secure(boolean)
     */
    @Nonnull
    public ClientBuilder secureKey(@Nullable File keyFile) {
        this.config.set(Config.SSL_KEY, keyFile);
        return this;
    }

    /**
     * Sets the private key password for SSL connection.
     *
     * @param password password for private key
     * @return this builder
     * @see #secure(boolean)
     */
    @Nonnull
    public ClientBuilder secureKeyPassword(@Nullable String password) {
        this.config.set(Config.SSL_KEY_PASSWORD, password);
        return this;
    }

    /**
     * Sets the delay between messages being sent to the server
     *
     * @param delay the delay in milliseconds
     * @return this builder
     */
    @Nonnull
    public ClientBuilder messageDelay(int delay) {
        Sanity.truthiness(delay > 0, "Delay must be at least 1");
        this.config.set(Config.MESSAGE_DELAY, delay);
        return this;
    }

    /**
     * Sets the server IP to which the client will connect.
     * <p>
     * By default, the port is 6667.
     *
     * @param port IRC server port
     * @return this builder
     */
    @Nonnull
    public ClientBuilder server(int port) {
        this.serverPort = this.validPort(port);
        return this;
    }

    /**
     * Sets the server host to which the client will connect.
     * <p>
     * By default, the host is localhost.
     *
     * @param host IRC server host
     * @return this builder
     * @throws IllegalArgumentException for null host
     */
    @Nonnull
    public ClientBuilder server(@Nonnull String host) {
        Sanity.nullCheck(host, "Host cannot be null");
        this.serverHost = host;
        return this;
    }

    /**
     * Sets the user the client connects as.
     * <p>
     * By default, the user is Kitteh.
     *
     * @param user user to connect as
     * @return this builder
     * @throws IllegalArgumentException for null user
     */
    @Nonnull
    public ClientBuilder user(@Nonnull String user) {
        Sanity.nullCheck(user, "User cannot be null");
        Sanity.truthiness(!user.contains(" "), "User cannot contain spaces");
        Sanity.safeMessageCheck(user, "user");
        this.config.set(Config.USER, user);
        return this;
    }

    /**
     * Sets all the information for, and enables, WebIRC.
     * <p>
     * By default, WebIRC is disabled.
     *
     * @param password password as defined in the IRCd config
     * @param user username part of the client's address
     * @param host hostname part of the client's address
     * @param ip client's IP address
     * @return this builder
     * @throws IllegalArgumentException for any null parameters
     * @see #webircDisable()
     */
    public ClientBuilder webirc(@Nonnull String password, @Nonnull String user, @Nonnull String host, @Nonnull InetAddress ip) {
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

    /**
     * Disables WebIRC.
     *
     * @return this builder
     * @see #webirc(String, String, String, InetAddress)
     */
    @Nonnull
    public ClientBuilder webircDisable() {
        this.config.set(Config.WEBIRC_PASSWORD, null);
        this.config.set(Config.WEBIRC_USER, null);
        this.config.set(Config.WEBIRC_HOST, null);
        this.config.set(Config.WEBIRC_IP, null);
        return this;
    }

    /**
     * Clientmaker, clientmaker, make me a client!
     *
     * @return a client designed to your liking
     */
    @Nonnull
    public Client build() {
        this.inetSet(Config.BIND_ADDRESS, this.bindHost, this.bindPort);
        this.inetSet(Config.SERVER_ADDRESS, this.serverHost, this.serverPort);
        return new IRCClient(this.config);
    }

    /**
     * Clones this builder.
     *
     * @return a clone of this builder
     */
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