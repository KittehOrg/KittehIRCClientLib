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
package org.kitteh.irc.client.library.event.abstractbase;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.helper.UserInfoChangeEvent;
import org.kitteh.irc.client.library.util.Change;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.function.Function;

/**
 * Abstract base class for user info changing.
 *
 * @param <Type> type of change
 * @see UserInfoChangeEvent
 */
public class UserInfoChangeEventBase<Type> extends ActorEventBase<User> implements UserInfoChangeEvent<Type> {
    private final User newUser;
    private final Change<Type> change;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param sourceMessage source message
     * @param oldUser the actor
     * @param newUser the new actor
     * @param changedInfoGetter getter for the changed info
     */
    protected UserInfoChangeEventBase(@NonNull Client client, @NonNull ServerMessage sourceMessage, @NonNull User oldUser, @NonNull User newUser, @NonNull Function<User, Type> changedInfoGetter) {
        super(client, sourceMessage, oldUser);
        this.newUser = Sanity.nullCheck(newUser, "New user cannot be null");
        this.change = new Change<>(changedInfoGetter.apply(oldUser), changedInfoGetter.apply(newUser));
    }

    @Override
    public @NonNull User getOldUser() {
        return this.getActor();
    }

    @Override
    public @NonNull User getNewUser() {
        return this.newUser;
    }

    @Override
    public @NonNull Change<Type> getChange() {
        return this.change;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("newUser", this.newUser).add("change", this.change);
    }
}
