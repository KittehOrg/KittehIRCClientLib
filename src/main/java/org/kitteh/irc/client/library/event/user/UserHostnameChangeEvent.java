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
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.abstractbase.ActorEventBase;
import org.kitteh.irc.client.library.event.helper.Change;
import org.kitteh.irc.client.library.event.helper.UserInfoChange;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A {@link User} has changed hostname.
 */
public class UserHostnameChangeEvent extends ActorEventBase<User> implements UserInfoChange<String> {
    private final User newUser;
    private final Change<String> change;

    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param originalMessages original messages
     * @param oldUser user changing hostname
     * @param newUser the new user instance
     */
    public UserHostnameChangeEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull User oldUser, @Nonnull User newUser) {
        super(client, originalMessages, oldUser);
        Sanity.nullCheck(newUser, "User cannot be null");
        this.newUser = newUser;
        this.change = new Change<>(oldUser.getHost(), newUser.getHost());
    }

    @Nonnull
    @Override
    public User getOldUser() {
        return this.getActor();
    }

    @Nonnull
    @Override
    public User getNewUser() {
        return this.newUser;
    }

    @Nonnull
    @Override
    public Change<String> getChange() {
        return this.change;
    }
}
