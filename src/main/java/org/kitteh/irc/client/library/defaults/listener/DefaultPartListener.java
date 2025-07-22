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
package org.kitteh.irc.client.library.defaults.listener;

import net.engio.mbassy.listener.Handler;
import org.jspecify.annotations.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.channel.ChannelPartEvent;
import org.kitteh.irc.client.library.event.channel.UnexpectedChannelLeaveViaPartEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;

/**
 * Default PART listener, producing events using default classes.
 */
public class DefaultPartListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultPartListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @CommandFilter("PART")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void part(ClientReceiveCommandEvent event) {
        if (event.getParameters().isEmpty()) {
            this.trackException(event, "PART message too short");
            return;
        }
        Channel channel = this.getTracker().getChannel(event.getParameters().getFirst()).orElse(null);
        if (channel == null) {
            this.trackException(event, "PART message sent for invalid channel name");
            return;
        }
        if (!(event.getActor() instanceof User user)) {
            this.trackException(event, "PART message sent for non-user");
            return;
        }
        boolean isSelf = user.getNick().equals(this.getClient().getNick());
        String partReason = (event.getParameters().size() > 1) ? event.getParameters().get(1) : "";
        ChannelPartEvent partEvent;
        if (isSelf && this.getClient().getIntendedChannels().contains(channel.getName())) {
            partEvent = new UnexpectedChannelLeaveViaPartEvent(this.getClient(), event.getSource(), channel, user, partReason);
        } else {
            partEvent = new ChannelPartEvent(this.getClient(), event.getSource(), channel, user, partReason);
        }
        this.fire(partEvent);
        this.getTracker().trackUserPart(channel.getName(), user.getNick());
        if (isSelf) {
            this.getTracker().unTrackChannel(channel.getName());
        }
    }
}
