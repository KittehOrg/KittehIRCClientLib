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
import org.kitteh.irc.client.library.event.channel.ChannelJoinEvent;
import org.kitteh.irc.client.library.event.channel.RequestedChannelJoinCompleteEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;

import java.util.HashSet;

/**
 * Default JOIN listener, producing events using default classes.
 */
public class DefaultJoinListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultJoinListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @CommandFilter("JOIN")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void join(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 1) {
            this.trackException(event, "JOIN message too short");
            return;
        }
        String channelName = event.getParameters().get(0);
        if (this.getClient().getServerInfo().isValidChannel(channelName)) {
            if (event.getActor() instanceof User) {
                this.getTracker().trackChannel(channelName);
                Channel channel = this.getTracker().getTrackedChannel(channelName).get();
                User user = (User) event.getActor();
                this.getTracker().trackChannelUser(channelName, user, new HashSet<>());
                ChannelJoinEvent joinEvent = null;
                if (user.getNick().equals(this.getClient().getNick())) {
                    if (this.getClient().getActorTracker().shouldQueryChannelInformation()) {
                        this.getClient().sendRawLine("MODE " + channelName);
                        this.getClient().sendRawLine("WHO " + channelName + (this.getClient().getServerInfo().hasWhoXSupport() ? " %cuhsnfar" : ""));
                    }
                    if (this.getClient().getIntendedChannels().contains(channelName)) {
                        joinEvent = new RequestedChannelJoinCompleteEvent(this.getClient(), event.getSource(), channel, user);
                    }
                }
                if (event.getParameters().size() > 2) {
                    if (!"*".equals(event.getParameters().get(1))) {
                        this.getTracker().setUserAccount(user.getNick(), event.getParameters().get(1));
                    }
                    this.getTracker().setUserRealName(user.getNick(), event.getParameters().get(2));
                }
                if (joinEvent == null) {
                    joinEvent = new ChannelJoinEvent(this.getClient(), event.getSource(), channel, user);
                }
                this.fire(joinEvent);
            } else {
                this.trackException(event, "JOIN message sent for non-user");
            }
        } else {
            this.trackException(event, "JOIN message sent for invalid channel name");
        }
    }
}
