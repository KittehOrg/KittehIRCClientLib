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

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.abstractbase.ActorEventBase;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * A {@link User} has changed away status, informed via AWAY message. Note
 * that two events may fire in a row while the user remains away, just with
 * a changed message.
 */
public class UserAwayMessageEvent extends ActorEventBase<User> {
    private final boolean isAway;
    private final String message;

    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param originalMessages original messages
     * @param user user
     * @param message message the user left
     */
    public UserAwayMessageEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull User user, @Nullable String message) {
        super(client, originalMessages, user);
        this.isAway = message != null;
        this.message = message;
    }

    /**
     * Gets if the user is away. If true, {@link #getAwayMessage()} is not
     * empty.
     *
     * @return true if away
     */
    public boolean isAway() {
        return this.isAway;
    }

    /**
     * Gets the away message.
     *
     * @return away message or empty if no longer away
     */
    @Nonnull
    public Optional<String> getAwayMessage() {
        return Optional.ofNullable(this.message);
    }

    @Override
    @Nonnull
    protected ToStringer toStringer() {
        return super.toStringer().add("awayMessage", this.message).add("isAway", this.isAway);
    }
}
