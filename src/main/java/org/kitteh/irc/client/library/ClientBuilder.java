/*
 * * Copyright (C) 2013-2014 Matt Baxter http://kitteh.org
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

import org.kitteh.irc.client.library.util.Consumer;
import org.kitteh.irc.client.library.util.Sanity;

import java.net.InetSocketAddress;

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
     * Binds the client to a host or IP locally. Null for wildcard binding.
     * <p>
     * By default, the host is null for wildcard binding.
     *
     * @param host host to bind to or null for wildcard
     * @return this builder
     */
    public ClientBuilder bind(String host) {
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
    public ClientBuilder bind(int port) {
        this.bindPort = this.validPort(port);
        return this;
    }

    /**
     * Sets a listener for all thrown exceptions on this client.
     * <p>
     * All exceptions are passed from a single, separate thread.
     *
     * @param listener catcher of throwables
     * @return this builder
     */
    public ClientBuilder listenException(Consumer<Exception> listener) {
        this.config.set(Config.LISTENER_EXCEPTION, new Config.ExceptionConsumerWrapper(listener));
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
    public ClientBuilder listenInput(Consumer<String> listener) {
        this.config.set(Config.LISTENER_INPUT, new Config.StringConsumerWrapper(listener));
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
    public ClientBuilder listenOutput(Consumer<String> listener) {
        this.config.set(Config.LISTENER_OUTPUT, new Config.StringConsumerWrapper(listener));
        return this;
    }

    /**
     * Names the client, for internal labeling.
     *
     * @param name a name to label the client internally
     * @return this builder
     */
    public ClientBuilder name(String name) {
        Sanity.nullCheck(name, "Name cannot be null");
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
     * @throws IllegalArgumentException for null nick
     */
    public ClientBuilder nick(String nick) {
        Sanity.nullCheck(nick, "Nick cannot be null");
        Sanity.truthiness(!nick.contains(" "), "Nick cannot contain spaces");
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
     * @throws IllegalArgumentException for null password
     */
    public ClientBuilder serverPassword(String password) {
        Sanity.nullCheck(password, "Server password cannot be null");
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
    public ClientBuilder realName(String name) {
        Sanity.nullCheck(name, "Real name cannot be null");
        this.config.set(Config.REAL_NAME, name);
        return this;
    }

    /**
     * Sets whether the client connects via SSL.
     *
     * @param ssl true for ssl
     * @return this builder
     */
    public ClientBuilder secure(boolean ssl) {
        this.config.set(Config.SSL, ssl);
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
    public ClientBuilder server(String host) {
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
    public ClientBuilder user(String user) {
        Sanity.nullCheck(user, "User cannot be null");
        Sanity.truthiness(!user.contains(" "), "User cannot contain spaces");
        this.config.set(Config.USER, user);
        return this;
    }

    /**
     * Clientmaker, clientmaker, make me a client!
     *
     * @return a client designed to your liking
     */
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
    @Override
    public ClientBuilder clone() {
        ClientBuilder clientBuilder = null;
        try {
            clientBuilder = (ClientBuilder) super.clone();
            clientBuilder.config = this.config.clone();
        } catch (CloneNotSupportedException ignored) {
        }
        return clientBuilder;

    }

    private void inetSet(Config.Entry<InetSocketAddress> entry, String host, int port) {
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
        return (port > 65535 || port < 0) ? 0 : port;
    }
}