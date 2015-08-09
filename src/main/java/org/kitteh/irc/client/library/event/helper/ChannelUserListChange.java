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
package org.kitteh.irc.client.library.event.helper;

import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A {@link Channel} is gaining or losing a {@link User}
 */
public interface ChannelUserListChange extends ClientEvent {
    /**
     * Describes the change occurring.
     */
    enum Change {
        /**
         * A user is joining.
         */
        JOIN,
        /**
         * A user is changing their nickname.
         */
        NICK_CHANGE,
        /**
         * A user is leaving.
         */
        LEAVE
    }

    /**
     * Gets the channel affected or empty if affecting all channels the user
     * is present in.
     *
     * @return channel or empty if all channels affected
     */
    @Nonnull
    Optional<Channel> getAffectedChannel();

    /**
     * Gets the type of change occurring.
     *
     * @return type of change
     */
    @Nonnull
    Change getChange();

    /**
     * Gets the current user affected.
     *
     * @return user
     */
    @Nonnull
    User getUser();
}