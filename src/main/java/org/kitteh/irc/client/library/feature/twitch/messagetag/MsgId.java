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
package org.kitteh.irc.client.library.feature.twitch.messagetag;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.TriFunction;

/**
 * Message tag for message IDs.
 */
public class MsgId extends MessageTagManager.DefaultMessageTag {
    /**
     * Name of this message tag.
     */
    public static final String NAME = "msg-id";

    /**
     * Function to create this message tag.
     */
    @SuppressWarnings("ConstantConditions")
    public static final TriFunction<Client, String, String, org.kitteh.irc.client.library.feature.twitch.messagetag.MsgId> FUNCTION = (client, name, value) -> new org.kitteh.irc.client.library.feature.twitch.messagetag.MsgId(name, value);

    /**
     * Known msg id values, according to Twitch documentation.
     */
    public static final class KnownValues {
        private KnownValues() {
        }

        /**
         * Sub
         */
        public static final String SUB = "sub";

        /**
         * Resubbed
         */
        public static final String RESUB = "resub";

        /**
         * &lt;user&gt; is already banned in this room.
         */
        public static final String ALREADY_BANNED = "already_banned";
        /**
         * This room is not in emote-only mode.
         */
        public static final String ALREADY_EMOTE_ONLY_OFF = "already_emote_only_off";
        /**
         * This room is already in emote-only mode.
         */
        public static final String ALREADY_EMOTE_ONLY_ON = "already_emote_only_on";
        /**
         * This room is not in r9k mode.
         */
        public static final String ALREADY_R9K_OFF = "already_r9k_off";
        /**
         * This room is already in r9k mode.
         */
        public static final String ALREADY_R9K_ON = "already_r9k_on";
        /**
         * This room is not in subscribers-only mode.
         */
        public static final String ALREADY_SUBS_OFF = "already_subs_off";
        /**
         * This room is already in subscribers-only mode.
         */
        public static final String ALREADY_SUBS_ON = "already_subs_on";
        /**
         * This channel is hosting &lt;channel&gt;.
         */
        public static final String BAD_HOST_HOSTING = "bad_host_hosting";
        /**
         * &lt;user&gt; is not banned from this room.
         */
        public static final String BAD_UNBAN_NO_BAN = "bad_unban_no_ban";
        /**
         * &lt;user&gt; is banned from this room.
         */
        public static final String BAN_SUCCESS = "ban_success";
        /**
         * This room is no longer in emote-only mode.
         */
        public static final String EMOTE_ONLY_OFF = "emote_only_off";
        /**
         * This room is now in emote-only mode.
         */
        public static final String EMOTE_ONLY_ON = "emote_only_on";
        /**
         * Exited host mode.
         */
        public static final String HOST_OFF = "host_off";
        /**
         * Now hosting &lt;channel&gt;.
         */
        public static final String HOST_ON = "host_on";
        /**
         * There are &lt;number&gt; host commands remaining this half hour.
         */
        public static final String HOSTS_REMAINING = "hosts_remaining";
        /**
         * This channel is suspended.
         */
        public static final String MSG_CHANNEL_SUSPENDED = "msg_channel_suspended";
        /**
         * This room is no longer in r9k mode.
         */
        public static final String R9K_OFF = "r9k_off";
        /**
         * This room is now in r9k mode.
         */
        public static final String R9K_ON = "r9k_on";
        /**
         * This room is no longer in slow mode.
         */
        public static final String SLOW_OFF = "slow_off";
        /**
         * This room is now in slow mode. You may send messages every &lt;slow seconds&gt; seconds.
         */
        public static final String SLOW_ON = "slow_on";
        /**
         * This room is no longer in subscribers-only mode.
         */
        public static final String SUBS_OFF = "subs_off";
        /**
         * This room is now in subscribers-only mode.
         */
        public static final String SUBS_ON = "subs_on";
        /**
         * &lt;user&gt; has been timed out for &lt;duration&gt; seconds.
         */
        public static final String TIMEOUT_SUCCESS = "timeout_success";
        /**
         * &lt;user&gt; is no longer banned from this chat room.
         */
        public static final String UNBAN_SUCCESS = "unban_success";
        /**
         * Unrecognized command: &lt;command&gt;.
         */
        public static final String UNRECOGNIZED_CMD = "unrecognized_cmd";
    }

    private MsgId(@NonNull String name, @NonNull String value) {
        super(name, value);
    }
}
