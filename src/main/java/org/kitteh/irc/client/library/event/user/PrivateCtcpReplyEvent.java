/*
 * * Copyright (C) 2013-2025 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.abstractbase.ActorPrivateMessageEventBase;
import org.kitteh.irc.client.library.event.helper.ActorMessageEvent;
import org.kitteh.irc.client.library.event.helper.CtcpEvent;

/**
 * The client has received a reply to a CTCP query! The method
 * {@link #getMessage()} returns the unescaped message with the delimiter
 * removed.
 */
public class PrivateCtcpReplyEvent extends ActorPrivateMessageEventBase<User> implements ActorMessageEvent<User>, CtcpEvent {
    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param sourceMessage source message
     * @param sender sender of the reply
     * @param target target of the reply
     * @param message message sent
     */
    public PrivateCtcpReplyEvent(@NonNull Client client, @NonNull ServerMessage sourceMessage, @NonNull User sender, @NonNull String target, @NonNull String message) {
        super(client, sourceMessage, sender, target, message);
    }
}
