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
package org.kitteh.irc.client.library.event.channel;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.event.abstractbase.TargetedUserChannelMessageEventBase;
import org.kitteh.irc.client.library.event.helper.ActorMessageEvent;
import org.kitteh.irc.client.library.event.helper.ReplyableEvent;

/**
 * Fires when a notice is sent to a subset of users in a channel. Note that
 * the sender may be the client itself if the capability "echo-message" is
 * enabled.
 */
public class ChannelTargetedNoticeEvent extends TargetedUserChannelMessageEventBase implements ActorMessageEvent<User>, ReplyableEvent {
    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param originalMessage original message
     * @param sender who sent it
     * @param channel channel receiving
     * @param prefix targeted prefix
     * @param message message sent
     */
    public ChannelTargetedNoticeEvent(@NonNull Client client, @NonNull ServerMessage originalMessage, @NonNull User sender, @NonNull Channel channel, @NonNull ChannelUserMode prefix, @NonNull String message) {
        super(client, originalMessage, sender, channel, prefix, message);
    }

    @Override
    public void sendReply(@NonNull String message) {
        this.getClient().sendNotice(this.getTargetedName(), message);
    }
}
