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
package org.kitteh.irc;

import org.kitteh.irc.util.Sanity;

import java.net.InetSocketAddress;

/**
 * Builds {@link Bot}s.
 */
public final class BotBuilder {
    private final Config config;
    private String bindHost;
    private int bindPort;
    private String serverHost;
    private int serverPort = 6667;

    /**
     * Creates a BotBuilder!
     */
    public BotBuilder() {
        this.config = new Config();
    }

    private BotBuilder(Config config) {
        this.config = config.clone();
    }

    /**
     * Binds the bot to a host or IP locally. Null for wildcard binding.
     * <p>
     * By default, the host is null for wildcard binding.
     *
     * @param host host to bind to or null for wildcard
     * @return this builder
     */
    public BotBuilder bind(String host) {
        this.bindHost = host;
        return this;
    }

    /**
     * Binds the bot to the specified port. Invalid ports are set to 0.
     * <p>
     * By default, the port is 0.
     *
     * @param port port to bind to
     * @return this builder
     */
    public BotBuilder bind(int port) {
        this.bindPort = this.validPort(port);
        return this;
    }

    /**
     * Names the bot, for internal labeling.
     *
     * @param name a name to label the bot internally
     * @return this builder
     */
    public BotBuilder name(String name) {
        Sanity.nullCheck(name, "Name cannot be null");
        this.config.set(Config.BOT_NAME, name);
        return this;
    }

    /**
     * Sets the bot's nick.
     * <p>
     * By default, the nick is Kitteh.
     *
     * @param nick nick for the bot to use
     * @return this builder
     * @throws java.lang.IllegalArgumentException for null nick
     */
    public BotBuilder nick(String nick) {
        Sanity.nullCheck(nick, "Nick cannot be null");
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
     * @throws java.lang.IllegalArgumentException for null password
     */
    public BotBuilder serverPassword(String password) {
        Sanity.nullCheck(password, "Server password cannot be null");
        this.config.set(Config.SERVER_PASSWORD, password);
        return this;
    }

    /**
     * Sets the realname the bot uses.
     * <p>
     * By default, the realname is Kitteh.
     *
     * @param name realname to use
     * @return this builder
     * @throws java.lang.IllegalArgumentException for null realname
     */
    public BotBuilder realName(String name) {
        Sanity.nullCheck(name, "Real name cannot be null");
        this.config.set(Config.REAL_NAME, name);
        return this;
    }

    /**
     * Sets the server IP to which the bot will connect.
     * <p>
     * By default, the port is 6667.
     *
     * @param port IRC server port
     * @return this builder
     */
    public BotBuilder server(int port) {
        this.serverPort = this.validPort(port);
        return this;
    }

    /**
     * Sets the server host to which the bot will connect.
     * <p>
     * By default, the host is localhost.
     *
     * @param host IRC server host
     * @return this builder
     * @throws java.lang.IllegalArgumentException for null host
     */
    public BotBuilder server(String host) {
        Sanity.nullCheck(host, "Host cannot be null");
        this.serverHost = host;
        return this;
    }

    /**
     * Sets the user the bot connects as.
     * <p>
     * By default, the user is Kitteh.
     *
     * @param user user to connect as
     * @return this builder
     * @throws java.lang.IllegalArgumentException for null user
     */
    public BotBuilder user(String user) {
        Sanity.nullCheck(user, "User cannot be null");
        this.config.set(Config.USER, user);
        return this;
    }

    /**
     * Botmaker, botmaker, make me a bot!
     *
     * @return a bot designed to your liking
     */
    public Bot build() {
        this.inetSet(Config.BIND_ADDRESS, this.bindHost, this.bindPort);
        this.inetSet(Config.SERVER_ADDRESS, this.serverHost, this.serverPort);
        return new IRCBot(this.config);
    }

    /**
     * Clones this builder.
     *
     * @return a clone of this builder
     */
    public BotBuilder clone() {
        BotBuilder botBuilder = new BotBuilder(this.config);
        botBuilder.bindHost = this.bindHost;
        botBuilder.bindPort = this.bindPort;
        botBuilder.serverHost = this.serverHost;
        botBuilder.serverPort = this.serverPort;
        return botBuilder;
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