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
package org.kitteh.irc.client.library.element;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.Pair;

import java.util.Map;
import java.util.Set;

/**
 * Represents an IRC channel.
 */
public interface Channel extends MessageReceiver {
    /**
     * Gets the latest snapshot of this channel.
     *
     * @return an updated snapshot
     */
    Channel getLatest();

    /**
     * Gets the users in the channel, if the client is in the channel.
     *
     * @return users and their modes
     */
    Map<User, Set<ChannelUserMode>> getUsers();

    /**
     * Gets a user by their nick.
     *
     * @param nick user's nick
     * @return a pair of the user and their channel modes
     */
    Pair<User, Set<ChannelUserMode>> getUser(String nick);

    /**
     * Joins the channel.
     *
     * @see Client#addChannel(Channel...)
     */
    void join();

    /**
     * Parts the channel.
     *
     * @param reason leaving reason
     * @see Client#removeChannel(Channel, String)
     */
    void part(String reason);
}