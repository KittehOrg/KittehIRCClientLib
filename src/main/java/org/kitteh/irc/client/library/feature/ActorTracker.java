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
package org.kitteh.irc.client.library.feature;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.Staleable;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.ModeInfo;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.util.Resettable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Tracker of users and channels, provider of all actors.
 */
public interface ActorTracker extends Resettable {
    /**
     * Gets an Actor based on the name provided.
     * <p>
     * For {@link User} the name needs the full mask and for channels the
     * name needs the prefix.
     *
     * @param name name
     * @return actor based on input
     */
    @NonNull Actor getActor(@NonNull String name);

    /**
     * Gets a channel.
     *
     * @param channel channel name
     * @return channel if a valid channel name
     */
    @NonNull Optional<Channel> getChannel(@NonNull String channel);

    /**
     * Gets a tracked channel.
     *
     * @param channel channel name
     * @return channel if tracked
     */
    @NonNull Optional<Channel> getTrackedChannel(@NonNull String channel);

    /**
     * Gets all tracked channels.
     *
     * @return tracked channels
     */
    @NonNull Set<Channel> getTrackedChannels();

    /**
     * Gets a tracked user.
     *
     * @param nick nickname of the user
     * @return user if tracked
     */
    @NonNull Optional<User> getTrackedUser(@NonNull String nick);

    /**
     * Gets if the given staleable object is considered stale by the tracker.
     *
     * @param staleable potentially stale object
     * @return true if stale
     */
    boolean isStale(@NonNull Staleable staleable);

    /**
     * Sets a channel is having had the full user list received.
     *
     * @param channel channel
     */
    void setChannelListReceived(@NonNull String channel);

    /**
     * Sets a channel's mode info list for a given mode, such as bans.
     *
     * @param channel channel
     * @param mode mode
     * @param modeInfo mode info
     */
    void setChannelModeInfoList(@NonNull String channel, char mode, List<ModeInfo> modeInfo);

    /**
     * Sets a channel's topic.
     *
     * @param channel channel
     * @param topic topic
     */
    void setChannelTopic(@NonNull String channel, @NonNull String topic);

    /**
     * Sets info on a channel's topic.
     *
     * @param channel channel
     * @param time topic set time
     * @param actor topic setter
     */
    void setChannelTopicInfo(@NonNull String channel, long time, @NonNull Actor actor);

    /**
     * Sets if channel information should be queried (WHO, MODE)
     * automatically on join. Defaults to true.
     *
     * @param query true to query, false to not
     */
    void setQueryChannelInformation(boolean query);

    /**
     * Sets the account for a tracked user.
     *
     * @param nick nick
     * @param account account or null if signed out
     */
    void setUserAccount(@NonNull String nick, @Nullable String account);

    /**
     * Sets a user away message, setting them to be recorded as away.
     *
     * @param nick nick
     * @param message away message or null for not away
     */
    void setUserAway(@NonNull String nick, @Nullable String message);

    /**
     * Sets a user as away without knowing the away message.
     *
     * @param nick nick
     * @param away true for away, false for not away
     */
    void setUserAway(@NonNull String nick, boolean away);

    /**
     * Sets OPER information known about a user.
     *
     * @param nick nick
     * @param operString oper info
     */
    void setUserOperString(@NonNull String nick, @NonNull String operString);

    /**
     * Sets the real name of the user.
     *
     * @param nick nick
     * @param realName real name
     */
    void setUserRealName(@NonNull String nick, @NonNull String realName);

    /**
     * Sets the server of a user.
     *
     * @param nick nick
     * @param server server
     */
    void setUserServer(@NonNull String nick, @NonNull String server);

    /**
     * Gets if channel information should be queried (WHO, MODE)
     * automatically on join.
     *
     * @return true if set to query automatically
     */
    boolean shouldQueryChannelInformation();

    /**
     * Tracks a channel.
     *
     * @param channel channel to track
     */
    void trackChannel(@NonNull String channel);

    /**
     * Sets tracking status of a mode (such as bans).
     *
     * @param channel channel
     * @param mode mode to track
     * @param track true to track false to stop tracking
     */
    void trackChannelMode(@NonNull String channel, @NonNull ChannelMode mode, boolean track);

    /**
     * Tracks mode info.
     *
     * @param channel channel
     * @param add true to add false to remove
     * @param modeInfo info
     */
    void trackChannelModeInfo(@NonNull String channel, boolean add, @NonNull ModeInfo modeInfo);

    /**
     * Tracks a user based on likely nickname input. Will process as full
     * name mask if that is provided instead.
     *
     * @param channel channel
     * @param nick nick
     * @param modes modes on the user
     */
    void trackChannelNick(@NonNull String channel, @NonNull String nick, @NonNull Set<ChannelUserMode> modes);

    /**
     * Tracks a user.
     *
     * @param channel channel
     * @param user user
     * @param modes modes on user
     */
    void trackChannelUser(@NonNull String channel, @NonNull User user, @NonNull Set<ChannelUserMode> modes);

    /**
     * Tracks a user.
     *
     * @param user user to track
     */
    void trackUser(@NonNull User user);

    /**
     * Tracks a user's hostname changing.
     *
     * @param nick nick
     * @param newHostname new hostname
     */
    void trackUserHostnameChange(@NonNull String nick, @NonNull String newHostname);

    /**
     * Tracks a user's nick change.
     *
     * @param oldNick old nick
     * @param newNick new nick
     */
    void trackUserNickChange(@NonNull String oldNick, @NonNull String newNick);

    /**
     * Tracks a user parting a channel, potentially untracking them overall
     * if they are no longer in any tracked channel.
     *
     * @param channel channel
     * @param nick nick parting
     */
    void trackUserPart(@NonNull String channel, @NonNull String nick);

    /**
     * Tracks a user quitting, removing them from all channels in which they
     * were tracked.
     *
     * @param nick nick quitting
     */
    void trackUserQuit(@NonNull String nick);

    /**
     * Tracks a users's user string (ident) changing.
     *
     * @param nick nick
     * @param newUserString new user string
     */
    void trackUserUserStringChange(@NonNull String nick, @NonNull String newUserString);

    /**
     * Stops tracking a channel.
     *
     * @param channel channel to untrack
     */
    void unTrackChannel(@NonNull String channel);

    /**
     * Updates status modes.
     *
     * @param channel channel
     * @param statusList mode status list
     */
    void updateChannelModes(@NonNull String channel, @NonNull ModeStatusList<ChannelMode> statusList);
}
