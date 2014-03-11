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

import java.net.InetSocketAddress;

/**
 * Build ALL the bots!
 */
public final class BotBuilder {
    private String botName = "Kitteh";
    private String bindHost = null;
    private int bindPort = 0;
    private String serverHost = "localhost";
    private int serverPort = 6667;
    private String nick = "Kitteh";
    private String user = "Kitteh";
    private String realName = "Kitteh";

    /**
     * Creates a BotBuilder!
     *
     * @param name a name to label the bot internally
     */
    public BotBuilder(String name) {
        this.botName = name;
    }

    /**
     * Binds the bot to a host or IP locally. Null for wildcard binding.
     * <p/>
     * By default, the host is null for wildcard binding.
     *
     * @param host host to bind to
     * @return this builder
     */
    public BotBuilder bind(String host) {
        this.bindHost = host;
        return this;
    }

    /**
     * Binds the bot to the specified port. Invalid ports are set to 0.
     * <p/>
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
     * Sets the bot's nick.
     * <p/>
     * By default, the nick is Kitteh.
     *
     * @param nick nick for the bot to use
     * @return this builder
     */
    public BotBuilder nick(String nick) {
        this.nick = nick;
        return this;
    }

    /**
     * Sets the realname the bot uses.
     * <p/>
     * By default, the realname is Kitteh.
     *
     * @param name realname to use
     * @return this builder
     */
    public BotBuilder realName(String name) {
        this.realName = name;
        return this;
    }

    /**
     * Sets the server IP to which the bot will connect.
     * <p/>
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
     * <p/>
     * By default, the host is localhost.
     *
     * @param host IRC server host
     * @return this builder
     */
    public BotBuilder server(String host) {
        this.serverHost = host;
        return this;
    }

    /**
     * Sets the user the bot connects as.
     * <p/>
     * By default, the user is Kitteh.
     *
     * @param user user to connect as
     * @return this builder
     */
    public BotBuilder user(String user) {
        this.user = user;
        return this;
    }

    /**
     * Botmaker, botmaker, make me a bot!
     *
     * @return a bot designed to your liking
     */
    public Bot build() {
        InetSocketAddress bind;
        if (this.bindHost == null) {
            if (this.bindPort == 0) {
                bind = null;
            } else {
                bind = new InetSocketAddress(this.bindPort);
            }
        } else {
            bind = new InetSocketAddress(this.bindHost, this.bindPort);
        }
        return new IRCBot(this.botName, bind, this.serverHost, this.serverPort, this.nick, this.user, this.realName);
    }

    /**
     * Gets a valid port number from a potentially invalid port number.
     * <p/>
     * Returns the valid port, or 0 if invalid.
     *
     * @param port port provided
     * @return valid port
     */
    private int validPort(int port) {
        return (port > 65535 || port < 0) ? 0 : port;
    }
}