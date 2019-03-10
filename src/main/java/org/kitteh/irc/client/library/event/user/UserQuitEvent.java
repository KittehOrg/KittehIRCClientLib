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
package org.kitteh.irc.client.library.event.user;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.abstractbase.ActorMessageEventBase;
import org.kitteh.irc.client.library.event.helper.ChannelUserListChangeEvent;

import java.util.Optional;

/**
 * A {@link User} has quit the server!
 */
public class UserQuitEvent extends ActorMessageEventBase<User> implements ChannelUserListChangeEvent {
    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param sourceMessage source message
     * @param user user quitting
     * @param message message the user left
     */
    public UserQuitEvent(@NonNull Client client, @NonNull ServerMessage sourceMessage, @NonNull User user, @NonNull String message) {
        super(client, sourceMessage, user, message);
    }

    @Override
    public @NonNull Optional<Channel> getAffectedChannel() {
        return Optional.empty();
    }

    @Override
    public @NonNull Change getChange() {
        return Change.LEAVE;
    }

    @Override
    public @NonNull User getUser() {
        return this.getActor();
    }
}
