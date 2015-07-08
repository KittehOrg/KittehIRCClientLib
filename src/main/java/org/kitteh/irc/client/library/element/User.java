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

import org.kitteh.irc.client.library.ServerInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Represents an IRC user.
 */
public interface User extends MessageReceiver {
    /**
     * Gets the Services account this user is signed into. Only available if
     * the server supports WHOX and the user and client share a channel.
     *
     * @return the account name or null if no account or WHOX not supported
     * @see ServerInfo#hasWhoXSupport() for if this can return a value
     */
    @Nullable
    String getAccount();

    /**
     * Gets the user's channels.
     *
     * @return channel names the user is in
     */
    @Nonnull
    Set<String> getChannels();

    /**
     * Gets the user's host.
     *
     * @return user host
     */
    @Nonnull
    String getHost();

    /**
     * Gets the user's nick.
     *
     * @return user nick
     */
    @Nonnull
    String getNick();

    /**
     * Gets the user's real name
     *
     * @return real name or null if this user does not share any channels
     * with the client
     */
    @Nullable
    String getRealName();

    /**
     * Gets the user's user string.
     *
     * @return user
     */
    @Nonnull
    String getUser();

    /**
     * Gets if the user is away.
     *
     * @return true if away
     */
    boolean isAway();
}