/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
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

import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.Staleable;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.ModeInfo;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.util.Resettable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ActorTracker extends Resettable {
    @Nonnull
    Actor getActor(@Nonnull String name);

    @Nonnull
    Optional<Channel> getTrackedChannel(@Nonnull String channel);

    @Nonnull
    Set<Channel> getTrackedChannels();

    @Nonnull
    Optional<User> getTrackedUser(@Nonnull String nick);

    boolean isStale(@Nonnull Staleable staleable);

    void setChannelListReceived(@Nonnull String channel);

    void setChannelModeInfoList(@Nonnull String channel, char mode, List<ModeInfo> modeInfo);

    void setChannelTopic(@Nonnull String channel, @Nonnull String topic);

    void setChannelTopicInfo(@Nonnull String channel, long time, @Nonnull Actor actor);

    void setUserAccount(@Nonnull String nick, @Nullable String account);

    void setUserAway(@Nonnull String nick, @Nullable String message);

    void setUserAway(@Nonnull String nick, boolean away);

    void setUserOperString(@Nonnull String nick, @Nonnull String operString);

    void setUserRealName(@Nonnull String nick, @Nonnull String realName);

    void setUserServer(@Nonnull String nick, @Nonnull String server);

    void trackChannel(@Nonnull String channel);

    void trackChannelMode(@Nonnull String channel, @Nonnull ChannelMode mode, boolean track);

    void trackChannelModeInfo(@Nonnull String channel, boolean add, @Nonnull ModeInfo modeInfo);

    void trackChannelNick(@Nonnull String channel, @Nonnull String nick, @Nonnull Set<ChannelUserMode> modes);

    void trackChannelUser(@Nonnull String channel, @Nonnull User user, @Nonnull Set<ChannelUserMode> modes);

    void trackUser(@Nonnull User user);

    void trackUserHostnameChange(@Nonnull String nick, @Nonnull String newHostname);

    void trackUserNickChange(@Nonnull String oldNick, @Nonnull String newNick);

    void trackUserPart(@Nonnull String channel, @Nonnull String nick);

    void trackUserQuit(@Nonnull String nick);

    void trackUserUserStringChange(@Nonnull String nick, @Nonnull String newUserString);

    void unTrackChannel(@Nonnull String channel);

    void updateChannelModes(@Nonnull String channel, @Nonnull ModeStatusList<ChannelMode> statusList);
}
