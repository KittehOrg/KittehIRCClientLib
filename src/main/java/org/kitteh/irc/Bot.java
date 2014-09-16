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

/**
 * An IRC bot.
 */
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
     * Gets the bot's event manager.
     *
     * @return the event manager for this bot
     */
    EventManager getEventManager();

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
    String getNick();

    /**
     * Sends a CTCP message to a target user or channel. Automagically adds
     * the CTCP delimiter around the message and escapes the characters that
     * need escaping when sending a CTCP message.
     * <p/>
     * <i>Note: CTCP replies should not be sent this way. Catch the message
     * with the {@link org.kitteh.irc.event.user.PrivateCTCPQueryEvent}</i>
     *
     * @param target the destination of the message
     * @param message the message to send
     */
    void sendCTCPMessage(String target, String message);

    /**
     * Sends a message to a target user or channel.
     *
     * @param target the destination of the message
     * @param message the message to send
     */
    void sendMessage(String target, String message);

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
     * Shuts down the bot.
     *
     * @param reason quit message to send, null for blank message
     */
    void shutdown(String reason);
}