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
package org.kitteh.irc.client.library.event.channel;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.event.abstractbase.ActorChannelEventBase;
import org.kitteh.irc.client.library.event.helper.ChannelTargetedEvent;
import org.kitteh.irc.client.library.event.helper.TagMessageEvent;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.List;

/**
 * Fires when a tag message is sent to a subset of users in a channel. Note
 * that the sender may be the client itself if the capability
 * "echo-message" is enabled.
 */
public class ChannelTargetedTagMessageEvent extends ActorChannelEventBase<Actor> implements TagMessageEvent, ChannelTargetedEvent {
    private final ChannelUserMode prefix;

    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param originalMessages original messages
     * @param sender who sent it
     * @param channel channel receiving
     * @param prefix targeted prefix
     */
    public ChannelTargetedTagMessageEvent(@NonNull Client client, @NonNull List<ServerMessage> originalMessages, @NonNull Actor sender, @NonNull Channel channel, @NonNull ChannelUserMode prefix) {
        super(client, originalMessages, sender, channel);
        this.prefix = Sanity.nullCheck(prefix, "Prefix cannot be null");
    }

    @Override
    public final @NonNull ChannelUserMode getPrefix() {
        return this.prefix;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("channelPrefix", this.prefix);
    }
}
