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
package org.kitteh.irc.client.library.event.channel;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ChannelEventBase;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.List;

/**
 * The server has informed us of a channel's topic.
 * <p>
 * Either the topic has changed or we requested to know what the topic was.
 */
public class ChannelTopicEvent extends ChannelEventBase {
    private final boolean updated;

    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param originalMessages original messages
     * @param channel channel the topic is about
     * @param updated if this is a new change
     * @see Channel#getTopic()
     */
    public ChannelTopicEvent(@NonNull Client client, @NonNull List<ServerMessage> originalMessages, @NonNull Channel channel, boolean updated) {
        super(client, originalMessages, channel);
        this.updated = updated;
    }

    /**
     * Gets the channel's topic.
     *
     * @return the channel topic
     * @see Channel#getTopic()
     */
    public Channel.@NonNull Topic getTopic() {
        return this.getChannel().getTopic();
    }

    /**
     * Gets if this is a new topic update, or just the server informing us of
     * a change from the past.
     *
     * @return true if a new topic change
     */
    public boolean isNew() {
        return this.updated;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("isNew", this.updated);
    }
}
