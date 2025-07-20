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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.event.channel.ChannelInviteEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;

/**
 * Default INVITE listener, producing events using default classes.
 */
public class DefaultInviteListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultInviteListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @CommandFilter("INVITE")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void invite(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "INVITE message too short");
            return;
        }
        Channel channel = this.getTracker().getChannel(event.getParameters().get(1)).orElse(null);
        if (channel == null) {
            this.trackException(event, "INVITE message sent for invalid channel name");
            return;
        }
        if (this.getClient().getNick().equalsIgnoreCase(event.getParameters().get(0)) && this.getClient().getIntendedChannels().contains(channel.getName())) {
            this.getClient().sendRawLine("JOIN " + channel.getName());
        }
        this.fire(new ChannelInviteEvent(this.getClient(), event.getSource(), channel, event.getActor(), event.getParameters().get(0)));
    }
}
