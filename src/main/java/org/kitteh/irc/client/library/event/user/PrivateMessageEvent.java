/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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
import org.kitteh.irc.client.library.event.abstractbase.ActorPrivateMessageEventBase;
import org.kitteh.irc.client.library.event.helper.ActorMessageEvent;
import org.kitteh.irc.client.library.event.helper.Replyable;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Fires when a message is sent to the client.
 */
public class PrivateMessageEvent extends ActorPrivateMessageEventBase<User> implements ActorMessageEvent<User>, Replyable {
    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param originalMessages original messages
     * @param sender who sent it
     * @param target who received it
     * @param message message sent
     */
    public PrivateMessageEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull User sender, @Nonnull String target, @Nonnull String message) {
        super(client, originalMessages, sender, target, message);
    }

    @Override
    public void sendReply(@Nonnull String message) {
        this.getActor().sendMessage(message);
    }
}
