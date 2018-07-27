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
package org.kitteh.irc.client.library.element;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.command.ChannelModeCommand;
import org.kitteh.irc.client.library.command.Command;
import org.kitteh.irc.client.library.command.KickCommand;
import org.kitteh.irc.client.library.command.TopicCommand;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.ModeInfo;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.event.channel.ChannelModeInfoListEvent;
import org.kitteh.irc.client.library.event.channel.ChannelUsersUpdatedEvent;
import org.kitteh.irc.client.library.event.channel.RequestedChannelJoinCompleteEvent;
import org.kitteh.irc.client.library.util.Sanity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

/**
 * Represents an IRC channel.
 */
public interface Channel extends MessageReceiver, Staleable {
    /**
     * Provides commands.
     */
    interface Commands {
        /**
         * Provides a new MODE command.
         *
         * @return new mode command
         */
        @NonNull ChannelModeCommand mode();

        /**
         * Provides a new KICK command.
         *
         * @return new kick command
         */
        @NonNull KickCommand kick();

        /**
         * Provides a new TOPIC command.
         *
         * @return new topic command
         */
        @NonNull TopicCommand topic();
    }

    /**
     * Information about the channel's topic.
     */
    interface Topic {
        /**
         * Gets who set the channel topic.
         *
         * @return topic setter if known
         */
        @NonNull Optional<Actor> getSetter();

        /**
         * Gets the time the topic was set.
         *
         * @return the time of setting if known
         */
        @NonNull Optional<Instant> getTime();

        /**
         * Gets the channel topic.
         *
         * @return the topic if known
         */
        @NonNull Optional<String> getValue();
    }

    /**
     * Gets the latest snapshot of this channel.
     *
     * @return an updated snapshot if the channel is currently tracked by
     * the client
     */
    default @NonNull Optional<Channel> getLatest() {
        return this.getClient().getChannel(this.getName());
    }

    /**
     * Gets the tracked mode info for the channel, if tracked.
     *
     * @param mode type A mode to acquire
     * @return list of mode info if tracked, empty if not tracked
     * @throws IllegalArgumentException for null or non-type-A mode
     */
    @NonNull Optional<List<ModeInfo>> getModeInfoList(@NonNull ChannelMode mode);

    /**
     * Gets the channel's current known modes.
     *
     * @return known modes
     */
    @NonNull ModeStatusList<ChannelMode> getModes();

    /**
     * Gets the nicknames of users in the channel, if the client is in the
     * channel.
     *
     * @return nicks in the channel
     */
    @NonNull List<String> getNicknames();

    /**
     * Gets the channel's topic.
     *
     * @return channel topic
     */
    @NonNull Topic getTopic();

    /**
     * Gets a user by their nick, if they are known to the client. Note that
     * the server may not have sent the User data over, while the nickname
     * may be known to the channel.
     *
     * @param nick user's nick
     * @return the user object, if known
     * @throws IllegalArgumentException if nick is null
     * @see #hasCompleteUserData()
     * @see ChannelUsersUpdatedEvent
     */
    @NonNull Optional<User> getUser(@NonNull String nick);

    /**
     * Gets all Users known to be in the channel. Note that the server may
     * not have sent all User data over, while the nickname may be known to
     * the channel. If you just want a list of nicknames, see {@link
     * #getNicknames()}.
     *
     * @return users in the channel
     * @see #hasCompleteUserData()
     * @see ChannelUsersUpdatedEvent
     */
    @NonNull List<User> getUsers();

    /**
     * Gets the user modes of a given nickname in the channel.
     *
     * @param nick user's nick
     * @return a set of modes the user is known to have, if the user is known
     */
    @NonNull Optional<SortedSet<ChannelUserMode>> getUserModes(@NonNull String nick);

    /**
     * Gets the user modes of a given user in the channel.
     *
     * @param user user
     * @return a set of modes the user is known to have, if the user is known
     */
    default @NonNull Optional<SortedSet<ChannelUserMode>> getUserModes(@NonNull User user) {
        return this.getUserModes(Sanity.nullCheck(user, "User cannot be null").getNick());
    }

    /**
     * Gets if this Channel has complete user data available, only possible
     * if the Client is in the channel and the WHO list has sent.
     *
     * @return true if Client is in channel and WHO has finished
     * @see ChannelUsersUpdatedEvent
     */
    boolean hasCompleteUserData();

    /**
     * Joins the channel.
     *
     * @see Client#addChannel(String...)
     */
    default void join() {
        this.getClient().addChannel(this.getName());
    }

    /**
     * Joins the channel with a key.
     *
     * @param key channel key
     * @see Client#addKeyProtectedChannel(String, String)
     */
    default void join(@NonNull String key) {
        this.getClient().addKeyProtectedChannel(this.getName(), key);
    }

    /**
     * Kicks the given user from this channel for the given reason.
     *
     * @param user user to kick
     * @param reason reason for the kick
     */
    default void kick(@NonNull User user, @NonNull String reason) {
        this.commands().kick().target(user).reason(reason).execute();
    }

    /**
     * Kicks the given user from this channel without a reason.
     *
     * @param user user to kick
     */
    default void kick(@NonNull User user) {
        this.commands().kick().target(user).execute();
    }

    /**
     * Provides access to {@link Command}s.
     *
     * @return commands
     */
    @NonNull Commands commands();

    /**
     * Parts the channel without stating a reason.
     *
     * @see Client#removeChannel(String)
     */
    default void part() {
        this.getClient().removeChannel(this.getName());
    }

    /**
     * Parts the channel.
     *
     * @param reason leaving reason
     * @see Client#removeChannel(String, String)
     */
    default void part(@NonNull String reason) {
        this.getClient().removeChannel(this.getName(), reason);
    }

    /**
     * Sets whether a particular type A mode should be tracked for this
     * channel, and sends a request for the full list. Currently supports
     * modes b, e, I, and q. The best time to request this would be in
     * {@link RequestedChannelJoinCompleteEvent} to ensure it's always set.
     *
     * @param mode mode to track
     * @param track true to track, false to stop tracking
     * @throws IllegalArgumentException for null or non-type-A mode or
     * non-supported type A mode
     * @throws IllegalStateException if not in channel
     * @see ChannelModeInfoListEvent
     */
    void setModeInfoTracking(@NonNull ChannelMode mode, boolean track);

    /**
     * Attempts to set the topic of the channel.
     *
     * @param topic new topic
     */
    default void setTopic(@NonNull String topic) {
        this.commands().topic().topic(topic).execute();
    }
}
