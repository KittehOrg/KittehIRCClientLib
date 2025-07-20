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
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.channel.ChannelCtcpEvent;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTargetedCtcpEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTargetedMessageEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.user.PrivateCtcpQueryEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;
import org.kitteh.irc.client.library.util.CtcpUtil;

import java.util.Date;
import java.util.Optional;

/**
 * Default PRIVMSG listener, producing events using default classes.
 */
public class DefaultPrivmsgListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultPrivmsgListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @CommandFilter("PRIVMSG")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void privmsg(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "PRIVMSG message too short");
            return;
        }
        if (!(event.getActor() instanceof User)) {
            this.trackException(event, "Message from something other than a user");
            return;
        }
        if (CtcpUtil.isCtcp(event.getParameters().get(1))) {
            final String ctcpMessage = CtcpUtil.fromCtcp(event.getParameters().get(1));
            final MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().get(0));
            User user = (User) event.getActor();

            if (messageTargetInfo instanceof MessageTargetInfo.Private) {
                String reply = null; // Message to send as CTCP reply (NOTICE). Send nothing if null.
                switch (ctcpMessage) {
                    case "VERSION":
                        reply = "VERSION I am Kitteh!";
                        break;
                    case "TIME":
                        reply = "TIME " + new Date().toString();
                        break;
                    case "FINGER":
                        reply = "FINGER om nom nom tasty finger";
                        break;
                }
                if (ctcpMessage.startsWith("PING ")) {
                    reply = ctcpMessage;
                }
                PrivateCtcpQueryEvent ctcpEvent = new PrivateCtcpQueryEvent(this.getClient(), event.getSource(), user, event.getParameters().get(0), ctcpMessage, reply);
                this.fire(ctcpEvent);
                Optional<String> replyMessage = ctcpEvent.getReply();
                if (ctcpEvent.isToClient()) {
                    replyMessage.ifPresent(message -> this.getClient().sendRawLine("NOTICE " + user.getNick() + " :" + CtcpUtil.toCtcp(message)));
                }
            } else if (messageTargetInfo instanceof MessageTargetInfo.ChannelInfo) {
                MessageTargetInfo.ChannelInfo channelInfo = (MessageTargetInfo.ChannelInfo) messageTargetInfo;
                this.fire(new ChannelCtcpEvent(this.getClient(), event.getSource(), user, channelInfo.getChannel(), ctcpMessage));
            } else if (messageTargetInfo instanceof MessageTargetInfo.TargetedChannel) {
                MessageTargetInfo.TargetedChannel channelInfo = (MessageTargetInfo.TargetedChannel) messageTargetInfo;
                this.fire(new ChannelTargetedCtcpEvent(this.getClient(), event.getSource(), user, channelInfo.getChannel(), channelInfo.getPrefix(), ctcpMessage));
            }
            return;
        }
        User user = (User) event.getActor();
        MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().get(0));
        if (messageTargetInfo instanceof MessageTargetInfo.Private) {
            this.fire(new PrivateMessageEvent(this.getClient(), event.getSource(), user, event.getParameters().get(0), event.getParameters().get(1)));
        } else if (messageTargetInfo instanceof MessageTargetInfo.ChannelInfo) {
            MessageTargetInfo.ChannelInfo channelInfo = (MessageTargetInfo.ChannelInfo) messageTargetInfo;
            this.fire(new ChannelMessageEvent(this.getClient(), event.getSource(), user, channelInfo.getChannel(), event.getParameters().get(1)));
        } else if (messageTargetInfo instanceof MessageTargetInfo.TargetedChannel) {
            MessageTargetInfo.TargetedChannel channelInfo = (MessageTargetInfo.TargetedChannel) messageTargetInfo;
            this.fire(new ChannelTargetedMessageEvent(this.getClient(), event.getSource(), user, channelInfo.getChannel(), channelInfo.getPrefix(), event.getParameters().get(1)));
        }
    }
}
