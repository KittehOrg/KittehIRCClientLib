/*
 * * Copyright (C) 2013-2019 Matt Baxter https://kitteh.org
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.feature.ServerInfo;

import java.util.Optional;
import java.util.Set;

/**
 * Represents an IRC user.
 */
public interface User extends MessageReceiver, Staleable {
    /**
     * Gets the Services account this user is signed into. Only fully
     * functional if the server supports the capabilities account-notify,
     * extended-join, and WHOX and the user and client share a channel.
     *
     * @return the account name if known
     * @see CapabilityManager#getCapabilities()
     * @see ServerInfo#hasWhoXSupport()
     */
    @NonNull Optional<String> getAccount();

    /**
     * Gets, if one is set and known, the away message for the user.
     *
     * @return away message if known
     */
    @NonNull Optional<String> getAwayMessage();

    /**
     * Gets the user's channels.
     *
     * @return channel names the user is in
     */
    @NonNull Set<String> getChannels();

    /**
     * Gets the user's host.
     *
     * @return user host
     */
    @NonNull String getHost();

    /**
     * Gets the user's nick.
     *
     * @return user nick
     */
    @NonNull String getNick();

    /**
     * Gets a user's operator information, if they have any privileges, noting
     * that this information may not imply actual IRC Operator status but some
     * other IRC network status as well.
     * <p>
     * Example: "is an Operator".
     *
     * @return operator info, or empty if none known
     */
    @NonNull Optional<String> getOperatorInformation();

    /**
     * Gets the user's real name
     *
     * @return real name if known
     */
    @NonNull Optional<String> getRealName();

    /**
     * Gets the name of the server the user is on.
     *
     * @return user's server if known
     */
    @NonNull Optional<String> getServer();

    /**
     * Gets the user's user string.
     *
     * @return user
     */
    @NonNull String getUserString();

    /**
     * Gets if the user is away.
     *
     * @return true if away
     */
    boolean isAway();
}
