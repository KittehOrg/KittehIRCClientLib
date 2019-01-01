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
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.channel.ChannelKickEvent;
import org.kitteh.irc.client.library.event.channel.UnexpectedChannelLeaveViaKickEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.helper.ClientEvent;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;

import java.util.Optional;

/**
 * Default KICK listener, producing events using default classes.
 */
public class DefaultKickListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultKickListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @CommandFilter("KICK")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void kick(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "KICK message too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getTrackedChannel(event.getParameters().get(0));
        if (channel.isPresent()) {
            Optional<User> kickedUser = this.getTracker().getTrackedUser(event.getParameters().get(1));
            if (kickedUser.isPresent()) {
                boolean isSelf = event.getParameters().get(1).equals(this.getClient().getNick());
                ClientEvent kickEvent;
                String kickReason = (event.getParameters().size() > 2) ? event.getParameters().get(2) : "";
                if (isSelf && this.getClient().getIntendedChannels().contains(channel.get().getName())) {
                    kickEvent = new UnexpectedChannelLeaveViaKickEvent(this.getClient(), event.getOriginalMessages(), channel.get(), event.getActor(), kickedUser.get(), kickReason);
                } else {
                    kickEvent = new ChannelKickEvent(this.getClient(), event.getOriginalMessages(), channel.get(), event.getActor(), kickedUser.get(), kickReason);
                }
                this.fire(kickEvent);
                this.getTracker().trackUserPart(channel.get().getName(), event.getParameters().get(1));
                if (isSelf) {
                    this.getTracker().unTrackChannel(channel.get().getName());
                }
            } else {
                this.trackException(event, "KICK message sent for non-user");
            }
        } else {
            this.trackException(event, "KICK message sent for invalid channel name");
        }
    }
}
