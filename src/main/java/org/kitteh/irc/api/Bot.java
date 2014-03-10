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
package org.kitteh.irc.api;

import org.kitteh.irc.AuthType;

public interface Bot {
    /**
     * Adds channels to the bot!
     * <p/>
     * Joins the channel if already connected.
     *
     * @param channel channel(s) to add
     */
    void addChannel(String... channel);

    /**
     * Gets the nickname the bot intends to possess. May not reflect the
     * current nickname if it's taken. The bot will automatically attempt to
     * take back this nickname.
     * <p/>
     * Use {@link #getNick()} for the current nick.
     *
     * @return the nickname the bot tries to maintain
     */
    String getIntendedNick();

    /**
     * Gets the bot name. This name is just an internal name for reference
     * and is not visible from IRC.
     *
     * @return the bot name
     */
    String getName();

    /**
     * Gets the current nickname the bot has.
     *
     * @return the current nick
     */
    public String getNick();

    /**
     * Sets the authentication method, user and password.
     *
     * @param type authentication type
     * @param nick nickname
     * @param pass password
     */
    void setAuth(AuthType type, String nick, String pass);

    /**
     * Sets the nick the bot wishes to use.
     *
     * @param nick new nickname
     */
    void setNick(String nick);

    /**
     * Sends a raw IRC message at low priority.
     * <p/>
     * If you feel you must, use {@link #sendRawLine(String, boolean)}.
     *
     * @param message message to send
     */
    void sendRawLine(String message);

    /**
     * Sends a raw IRC message at specified priority. High priority takes
     * precedence over any currently queued low priority messages.
     *
     * @param message message to send
     * @param priority if true, use high priority
     */
    void sendRawLine(String message, boolean priority);

    /**
     * Shuts down the bot.
     *
     * @param reason quit message to send
     */
    void shutdown(String reason);
}