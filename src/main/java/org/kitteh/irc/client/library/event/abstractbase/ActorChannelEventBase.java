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
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.helper.ActorEvent;
import org.kitteh.irc.client.library.event.helper.ChannelEvent;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.List;

/**
 * Abstract base class for events involving an Actor and Channel. Use the
 * helper events if you want to listen to events involving either.
 *
 * @param <A> actor involved
 * @see ActorEvent
 * @see ChannelEvent
 */
public abstract class ActorChannelEventBase<A extends Actor> extends ActorEventBase<A> implements ChannelEvent {
    private final Channel channel;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param actor the actor
     * @param channel the channel
     */
    protected ActorChannelEventBase(@NonNull Client client, @NonNull List<ServerMessage> originalMessages, @NonNull A actor, @NonNull Channel channel) {
        super(client, originalMessages, actor);
        Sanity.nullCheck(channel, "Channel cannot be null");
        Sanity.truthiness(channel.getClient() == client, "Channel must be from given Client");
        this.channel = channel;
    }

    @Override
    @NonNull
    public final Channel getChannel() {
        return this.channel;
    }

    @Override
    @NonNull
    protected ToStringer toStringer() {
        return super.toStringer().add("channel", this.channel);
    }
}
