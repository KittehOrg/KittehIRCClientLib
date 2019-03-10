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
package org.kitteh.irc.client.library.defaults.listener;

import net.engio.mbassy.listener.Handler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.event.channel.ChannelTopicEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;

import java.util.Optional;

/**
 * Default TOPIC listener, producing events using default classes.
 */
public class DefaultTopicListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultTopicListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @NumericFilter(332) // Topic
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void topic(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "Topic message too short");
            return;
        }
        Optional<Channel> topicChannel = this.getTracker().getChannel(event.getParameters().get(1));
        if (topicChannel.isPresent()) {
            this.getTracker().setChannelTopic(topicChannel.get().getName(), event.getParameters().get(2));
        } else {
            this.trackException(event, "Topic message sent for invalid channel name");
        }
    }

    @NumericFilter(333) // Topic info
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void topicInfo(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 4) {
            this.trackException(event, "Topic message too short");
            return;
        }
        Optional<Channel> topicSetChannel = this.getTracker().getChannel(event.getParameters().get(1));
        if (topicSetChannel.isPresent()) {
            this.getTracker().setChannelTopicInfo(topicSetChannel.get().getName(), Long.parseLong(event.getParameters().get(3)) * 1000, this.getTracker().getActor(event.getParameters().get(2)));
            this.fire(new ChannelTopicEvent(this.getClient(), event.getSource(), topicSetChannel.get(), false));
        } else {
            this.trackException(event, "Topic message sent for invalid channel name");
        }
    }

    @CommandFilter("TOPIC")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void topic(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "TOPIC message too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getChannel(event.getParameters().get(0));
        if (channel.isPresent()) {
            this.getTracker().setChannelTopic(channel.get().getName(), event.getParameters().get(1));
            this.getTracker().setChannelTopicInfo(channel.get().getName(), System.currentTimeMillis(), event.getActor());
            this.fire(new ChannelTopicEvent(this.getClient(), event.getSource(), channel.get(), true));
        } else {
            this.trackException(event, "TOPIC message sent for invalid channel name");
        }
    }
}
