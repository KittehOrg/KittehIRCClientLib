/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.feature.twitch.event;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ChannelEventBase;

import java.util.List;
import java.util.Optional;

/**
 * An event for when Twitch sends a USERNOTICE message, which is when a user
 * subscribes or re-subscribes.
 *
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.Badges
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.Color
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.DisplayName
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.Emotes
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.Mod
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.MsgId
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.MsgParamMonths
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.MsgParamSubPlan
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.MsgParamSubPlanName
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.RoomId
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.Subscriber
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.SystemMsg
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.Turbo
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.User
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.UserId
 * @see org.kitteh.irc.client.library.feature.twitch.messagetag.UserType
 */
public class UserNoticeEvent extends ChannelEventBase implements TwitchSingleMessageEvent {
    private final String message;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param channel channel
     * @param message message from user
     */
    public UserNoticeEvent(@NonNull Client client, @NonNull List<ServerMessage> originalMessages, @NonNull Channel channel, @Nullable String message) {
        super(client, originalMessages, channel);
        this.message = message;
    }

    /**
     * Message from the user, if they sent one.
     *
     * @return message or {@link Optional#empty()} if no message
     */
    @NonNull
    public Optional<String> getMessage() {
        return Optional.ofNullable(this.message);
    }
}
