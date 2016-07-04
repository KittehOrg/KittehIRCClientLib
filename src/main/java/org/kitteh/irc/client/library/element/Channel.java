/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.command.KickCommand;
import org.kitteh.irc.client.library.command.ChannelModeCommand;
import org.kitteh.irc.client.library.command.TopicCommand;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.event.channel.ChannelUsersUpdatedEvent;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Represents an IRC channel.
 */
public interface Channel extends MessageReceiver, Staleable {
    /**
     * Information about the channel's topic.
     */
    interface Topic {
        /**
         * Gets who set the channel topic.
         *
         * @return topic setter if known
         */
        @Nonnull
        Optional<Actor> getSetter();

        /**
         * Gets the time the topic was set.
         *
         * @return the time of setting if known
         */
        @Nonnull
        Optional<Instant> getTime();

        /**
         * Gets the channel topic.
         *
         * @return the topic if known
         */
        @Nonnull
        Optional<String> getValue();
    }

    /**
     * Gets the latest snapshot of this channel.
     *
     * @return an updated snapshot if the channel is currently tracked by
     * the client
     */
    @Nonnull
    default Optional<Channel> getLatest() {
        return this.getClient().getChannel(this.getName());
    }

    /**
     * Gets the channel's current known modes.
     *
     * @return known modes
     */
    @Nonnull
    ModeStatusList getModes();

    /**
     * Gets the nicknames of users in the channel, if the client is in the
     * channel.
     *
     * @return nicks in the channel
     */
    @Nonnull
    List<String> getNicknames();

    /**
     * Gets the channel's topic.
     *
     * @return channel topic
     */
    @Nonnull
    Topic getTopic();

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
    @Nonnull
    Optional<User> getUser(@Nonnull String nick);

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
    @Nonnull
    List<User> getUsers();

    /**
     * Gets the user modes of a given nickname in the channel.
     *
     * @param nick user's nick
     * @return a set of modes the user is known to have, if the user is known
     */
    @Nonnull
    Optional<Set<ChannelUserMode>> getUserModes(@Nonnull String nick);

    /**
     * Gets the user modes of a given user in the channel.
     *
     * @param user user
     * @return a set of modes the user is known to have, if the user is known
     */
    @Nonnull
    default Optional<Set<ChannelUserMode>> getUserModes(@Nonnull User user) {
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
    default void join(@Nonnull String key) {
        this.getClient().addKeyProtectedChannel(this.getName(), key);
    }

    /**
     * Kicks the given user from this channel for the given reason.
     *
     * @param user user to kick
     * @param reason reason for the kick
     */
    default void kick(@Nonnull User user, @Nonnull String reason) {
        this.newKickCommand().target(user).reason(reason).execute();
    }

    /**
     * Kicks the given user from this channel without a reason.
     *
     * @param user user to kick
     */
    default void kick(@Nonnull User user) {
        this.newKickCommand().target(user).execute();
    }

    /**
     * Provides a new KICK command.
     *
     * @return new kick command
     */
    @Nonnull
    default KickCommand newKickCommand() {
        return new KickCommand(this.getClient(), this.getName());
    }

    /**
     * Provides a new MODE command.
     *
     * @return new mode command
     */
    @Nonnull
    default ChannelModeCommand newModeCommand() {
        return new ChannelModeCommand(this.getClient(), this.getName());
    }

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
    default void part(@Nonnull String reason) {
        this.getClient().removeChannel(this.getName(), reason);
    }

    /**
     * Attempts to set the topic of the channel.
     *
     * @param topic new topic
     */
    default void setTopic(@Nonnull String topic) {
        new TopicCommand(this.getClient(), this.getName()).topic(topic).execute();
    }
}
