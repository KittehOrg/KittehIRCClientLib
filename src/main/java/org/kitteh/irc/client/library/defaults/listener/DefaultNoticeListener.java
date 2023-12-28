/*
 * * Copyright (C) 2013-2023 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.element.Server;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.channel.ChannelNoticeEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTargetedNoticeEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.user.PrivateCtcpReplyEvent;
import org.kitteh.irc.client.library.event.user.PrivateNoticeEvent;
import org.kitteh.irc.client.library.event.user.ServerNoticeEvent;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;
import org.kitteh.irc.client.library.util.CtcpUtil;

/**
 * Default NOTICE listener, producing events using default classes.
 */
public class DefaultNoticeListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultNoticeListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @CommandFilter("NOTICE")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void notice(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "NOTICE message too short");
            return;
        }
        String message = event.getParameters().get(1);
        // Not a user
        if (!(event.getActor() instanceof User)) {
            if (event.getActor() instanceof Server) {
                if (CtcpUtil.isCtcp(message)) {
                    this.trackException(event, "Server sent a CTCP message and I panicked");
                    return;
                }
                this.fire(new ServerNoticeEvent(this.getClient(), event.getSource(), (Server) event.getActor(), message));
            } else {
                this.trackException(event, "Message from neither server nor user");
            }
            return;
        }
        if (CtcpUtil.isCtcp(message)) {
            final String ctcpMessage = CtcpUtil.fromCtcp(event.getParameters().get(1));
            final MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().get(0));
            User user = (User) event.getActor();
            if (messageTargetInfo instanceof MessageTargetInfo.Private) {
                this.fire(new PrivateCtcpReplyEvent(this.getClient(), event.getSource(), user, event.getParameters().get(0), ctcpMessage));
            }
            return;
        }
        User user = (User) event.getActor();
        MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().get(0));
        if (messageTargetInfo instanceof MessageTargetInfo.Private) {
            this.fire(new PrivateNoticeEvent(this.getClient(), event.getSource(), user, event.getParameters().get(0), message));
        } else if (messageTargetInfo instanceof MessageTargetInfo.ChannelInfo) {
            MessageTargetInfo.ChannelInfo channelInfo = (MessageTargetInfo.ChannelInfo) messageTargetInfo;
            this.fire(new ChannelNoticeEvent(this.getClient(), event.getSource(), user, channelInfo.getChannel(), message));
        } else if (messageTargetInfo instanceof MessageTargetInfo.TargetedChannel) {
            MessageTargetInfo.TargetedChannel channelInfo = (MessageTargetInfo.TargetedChannel) messageTargetInfo;
            this.fire(new ChannelTargetedNoticeEvent(this.getClient(), event.getSource(), user, channelInfo.getChannel(), channelInfo.getPrefix(), message));
        }
    }
}
