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
package org.kitteh.irc.client.library.event.abstractbase;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.helper.ActorEvent;
import org.kitteh.irc.client.library.event.helper.MessageEvent;
import org.kitteh.irc.client.library.event.helper.PrivateEvent;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.List;

/**
 * Abstract base class for events involving an Actor and have a message and
 * are private messages. Use the helper events if you want to listen to
 * events involving either.
 *
 * @param <A> actor involved
 * @see ActorEvent
 * @see MessageEvent
 * @see PrivateEvent
 */
public abstract class ActorPrivateMessageEventBase<A extends Actor> extends ActorMessageEventBase<A> implements PrivateEvent {
    private final boolean isToClient;
    private final String target;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param actor the actor
     * @param target target of the message
     * @param message the message
     */
    protected ActorPrivateMessageEventBase(@NonNull Client client, @NonNull List<ServerMessage> originalMessages, @NonNull A actor, @NonNull String target, @NonNull String message) {
        super(client, originalMessages, actor, message);
        this.target = target;
        this.isToClient = client.getServerInfo().getCaseMapping().areEqualIgnoringCase(client.getNick(), target);
    }

    @Override
    @NonNull
    public String getTarget() {
        return this.target;
    }

    @Override
    public boolean isToClient() {
        return this.isToClient;
    }

    @Override
    @NonNull
    protected ToStringer toStringer() {
        return super.toStringer().add("isToClient", this.isToClient).add("target", this.target);
    }
}
