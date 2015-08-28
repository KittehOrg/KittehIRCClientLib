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
package org.kitteh.irc.client.library.event.user;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.abstractbase.ActorEventBase;
import org.kitteh.irc.client.library.event.helper.ChannelUserListChange;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * A {@link User} has changed nickname!
 */
public class UserNickChangeEvent extends ActorEventBase<User> implements ChannelUserListChange {
    private final User newUser;

    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param user user changing name
     * @param newUser the new nickname
     */
    public UserNickChangeEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull User user, @Nonnull User newUser) {
        super(client, originalMessages, user);
        Sanity.nullCheck(newUser, "User cannot be null");
        this.newUser = newUser;
    }

    @Nonnull
    @Override
    public Optional<Channel> getAffectedChannel() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Change getChange() {
        return Change.NICK_CHANGE;
    }

    /**
     * Gets the new user with the new nickname.
     *
     * @return the user with new nickname
     */
    @Nonnull
    public User getNewUser() {
        return this.newUser;
    }

    @Nonnull
    @Override
    public User getUser() {
        return this.getActor();
    }
}