/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.abstractbase.ActorEventBase;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.List;
import java.util.Optional;

/**
 * A {@link User} has changed account status, either signing into one or
 * signing out of one. Fired if the server supports account-notify.
 */
public class UserAccountStatusEvent extends ActorEventBase<User> {
    private final String account;

    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param originalMessages original messages
     * @param user user
     * @param account account the user is signed into
     */
    public UserAccountStatusEvent(@NonNull Client client, @NonNull List<ServerMessage> originalMessages, @NonNull User user, @Nullable String account) {
        super(client, originalMessages, user);
        this.account = account;
    }

    /**
     * Gets the account, if signed into one, or empty if signed out.
     *
     * @return account or empty if no account
     */
    @NonNull
    public Optional<String> getAccount() {
        return Optional.ofNullable(this.account);
    }

    @Override
    @NonNull
    protected ToStringer toStringer() {
        return super.toStringer().add("account", this.account);
    }
}
