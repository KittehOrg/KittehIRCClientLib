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
import org.kitteh.irc.client.library.event.channel.ChannelTagMessageEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTargetedTagMessageEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.user.PrivateTagMessageEvent;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;

/**
 * Default TAGMSG listener, producing events using default classes.
 */
public class DefaultTagmsgListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultTagmsgListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @CommandFilter("TAGMSG")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void tagmsg(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 1) {
            this.trackException(event, "TAGMSG message too short");
            return;
        }
        MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().get(0));
        if (messageTargetInfo instanceof MessageTargetInfo.Private) {
            this.fire(new PrivateTagMessageEvent(this.getClient(), event.getSource(), event.getActor(), event.getParameters().get(0)));
        } else if (messageTargetInfo instanceof MessageTargetInfo.ChannelInfo channelInfo) {
            this.fire(new ChannelTagMessageEvent(this.getClient(), event.getSource(), event.getActor(), channelInfo.getChannel()));
        } else if (messageTargetInfo instanceof MessageTargetInfo.TargetedChannel channelInfo) {
            this.fire(new ChannelTargetedTagMessageEvent(this.getClient(), event.getSource(), event.getActor(), channelInfo.getChannel(), channelInfo.getPrefix()));
        }
    }
}
