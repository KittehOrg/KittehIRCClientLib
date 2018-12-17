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
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.event.helper.ActorEvent;
import org.kitteh.irc.client.library.event.helper.ChannelEvent;
import org.kitteh.irc.client.library.event.helper.ChannelTargetedEvent;
import org.kitteh.irc.client.library.event.helper.MessageEvent;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.List;

/**
 * Abstract base class for events involving an Actor and Channel and have a
 * message while being targeted at a specific subset of users to that
 * Channel. Use the helper events if you want to listen to events involving
 * either.
 *
 * @see ActorEvent
 * @see ChannelEvent
 * @see MessageEvent
 */
public abstract class TargetedUserChannelMessageEventBase extends ActorChannelMessageEventBase<User> implements ChannelTargetedEvent, MessageEvent {
    private final ChannelUserMode prefix;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param user the user
     * @param channel the channel
     * @param prefix the targeted prefix
     * @param message the message
     */
    protected TargetedUserChannelMessageEventBase(@NonNull Client client, @NonNull List<ServerMessage> originalMessages, @NonNull User user, @NonNull Channel channel, @NonNull ChannelUserMode prefix, @NonNull String message) {
        super(client, originalMessages, user, channel, message);
        this.prefix = Sanity.nullCheck(prefix, "Prefix cannot be null");
    }

    /**
     * Gets the prefix to which the message was sent.
     *
     * @return the prefix targeted
     */
    @Override
    public final @NonNull ChannelUserMode getPrefix() {
        return this.prefix;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("channelPrefix", this.prefix);
    }
}
