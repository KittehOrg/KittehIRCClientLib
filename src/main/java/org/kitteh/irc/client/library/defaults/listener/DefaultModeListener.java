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
import org.kitteh.irc.client.library.defaults.element.mode.DefaultModeInfo;
import org.kitteh.irc.client.library.defaults.element.mode.DefaultModeStatusList;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ModeStatus;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.event.channel.ChannelModeEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.user.UserModeEvent;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;
import org.kitteh.irc.client.library.util.StringUtil;

import java.time.Instant;

/**
 * Default MODE listener, producing events using default classes.
 */
public class DefaultModeListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultModeListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @NumericFilter(324)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void channelMode(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "Channel mode info message too short");
            return;
        }
        Channel channel = this.getTracker().getChannel(event.getParameters().get(1)).orElse(null);
        if (channel == null) {
            this.trackException(event, "Channel mode info message sent for invalid channel name");
            return;
        }
        ModeStatusList<ChannelMode> statusList;
        try {
            statusList = DefaultModeStatusList.fromChannel(this.getClient(), StringUtil.combineSplit(event.getParameters().toArray(new String[0]), 2));
        } catch (IllegalArgumentException e) {
            this.trackException(event, e.getMessage());
            return;
        }
        this.getTracker().updateChannelModes(channel.getName(), statusList);
    }

    @CommandFilter("MODE")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void mode(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "MODE message too short");
            return;
        }
        MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().getFirst());
        if (messageTargetInfo instanceof MessageTargetInfo.Private) {
            ModeStatusList<UserMode> statusList;
            try {
                statusList = DefaultModeStatusList.fromUser(this.getClient(), StringUtil.combineSplit(event.getParameters().toArray(new String[0]), 1));
            } catch (IllegalArgumentException e) {
                this.trackException(event, e.getMessage());
                return;
            }
            this.fire(new UserModeEvent(this.getClient(), event.getSource(), event.getActor(), event.getParameters().getFirst(), statusList));
            this.getClient().updateUserModes(statusList);
        } else if (messageTargetInfo instanceof MessageTargetInfo.ChannelInfo) {
            Channel channel = ((MessageTargetInfo.ChannelInfo) messageTargetInfo).getChannel();
            ModeStatusList<ChannelMode> statusList;
            try {
                statusList = DefaultModeStatusList.fromChannel(this.getClient(), StringUtil.combineSplit(event.getParameters().toArray(new String[0]), 1));
            } catch (IllegalArgumentException e) {
                this.trackException(event, e.getMessage());
                return;
            }
            this.fire(new ChannelModeEvent(this.getClient(), event.getSource(), event.getActor(), channel, statusList));
            statusList.getAll().stream()
                    .filter(status -> status.getMode().getType() == ChannelMode.Type.A_MASK)
                    .forEach(status -> this.getTracker().trackChannelModeInfo(channel.getName(), status.getAction() == ModeStatus.Action.ADD,
                            new DefaultModeInfo(this.getClient(), channel, status.getMode(), status.getParameter().get(), event.getActor().getName(), Instant.now())
                    ));
            this.getTracker().updateChannelModes(channel.getName(), statusList);
        } else {
            this.trackException(event, "MODE message sent for invalid target");
        }
    }
}
