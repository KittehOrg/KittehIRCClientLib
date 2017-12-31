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
package org.kitteh.irc.client.library.defaults.feature;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.command.ChannelModeCommand;
import org.kitteh.irc.client.library.defaults.element.DefaultActor;
import org.kitteh.irc.client.library.defaults.element.DefaultChannel;
import org.kitteh.irc.client.library.defaults.element.DefaultChannelTopic;
import org.kitteh.irc.client.library.defaults.element.DefaultServer;
import org.kitteh.irc.client.library.defaults.element.DefaultUser;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.Staleable;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.ModeInfo;
import org.kitteh.irc.client.library.element.mode.ModeStatus;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.feature.ActorTracker;
import org.kitteh.irc.client.library.util.CIKeyMap;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultActorTracker implements ActorTracker {
    private class IrcActor {
        private String name;

        private IrcActor(@Nonnull String name) {
            this.name = name;
        }

        @Nonnull
        String getName() {
            return this.name;
        }

        void setName(@Nonnull String name) {
            this.name = name;
        }

        @Nonnull
        DefaultActor snapshot() {
            return new DefaultActor(DefaultActorTracker.this.client, this.name);
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).toString();
        }
    }

    private class IrcStaleable<T extends Staleable> extends IrcActor {
        @Nullable
        private T snapshot;

        IrcStaleable(@Nonnull String name) {
            super(name);
        }

        boolean isStale(@Nonnull Object potentiallyStale) {
            return this.snapshot != potentiallyStale;
        }

        void markStale() {
            this.snapshot = null;
        }

        @Nonnull
        synchronized T snapshot(@Nonnull Supplier<T> supplier) {
            if (this.snapshot != null) {
                return this.snapshot;
            }
            return this.snapshot = supplier.get();
        }
    }

    class IrcChannel extends IrcStaleable<DefaultChannel> {
        private final Map<Character, ModeStatus<ChannelMode>> channelModes = new HashMap<>();
        private final Map<Character, List<ModeInfo>> modeInfoLists = new HashMap<>();
        private final Set<Character> trackedModes = new HashSet<>();
        private final Map<String, Set<ChannelUserMode>> modes;
        private final DefaultChannel.DefaultChannelCommands commands;
        private volatile boolean fullListReceived;
        private long lastWho = System.currentTimeMillis();
        private String topic;
        @Nullable
        private Actor topicSetter;
        @Nullable
        private Instant topicTime;
        private volatile boolean tracked;

        private IrcChannel(@Nonnull String channel) {
            super(channel);
            this.modes = new CIKeyMap<>(DefaultActorTracker.this.client);
            this.commands = new DefaultChannel.DefaultChannelCommands(DefaultActorTracker.this.client, channel);
            DefaultActorTracker.this.trackedChannels.put(channel, this);
        }

        void setListReceived() {
            this.fullListReceived = true;
            this.markStale();
        }

        private void setTracked(boolean tracked) {
            this.tracked = tracked;
            this.modes.keySet().forEach(DefaultActorTracker.this::staleUser);
            this.markStale();
        }

        void setTopic(@Nonnull String topic) {
            this.topic = topic;
            this.topicTime = null;
            this.topicSetter = null;
            this.markStale();
        }

        void setTopic(long time, @Nonnull Actor actor) {
            this.topicTime = Instant.ofEpochMilli(time);
            this.topicSetter = actor;
            this.markStale();
        }

        @Override
        @Nonnull
        DefaultChannel snapshot() {
            if (DefaultActorTracker.this.queryChannelInformation) {
                synchronized (this.modes) {
                    if (this.tracked && !this.fullListReceived) {
                        long now = System.currentTimeMillis();
                        if ((now - this.lastWho) > 5000) {
                            this.lastWho = now;
                            DefaultActorTracker.this.client.sendRawLineAvoidingDuplication("WHO " + this.getName() + (DefaultActorTracker.this.client.getServerInfo().hasWhoXSupport() ? " %cuhsnfar" : ""));
                        }
                    }
                }
            }
            ModeStatusList<ChannelMode> channelModes = ModeStatusList.of(this.channelModes.values());
            Map<Character, List<ModeInfo>> modeInfoLists = new HashMap<>();
            for (Map.Entry<Character, List<ModeInfo>> entry : this.modeInfoLists.entrySet()) {
                modeInfoLists.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
            }
            this.trackedModes.stream()
                    .filter(character -> !modeInfoLists.containsKey(character))
                    .forEach(character -> this.modeInfoLists.put(character, Collections.unmodifiableList(new ArrayList<>())));
            Map<String, SortedSet<ChannelUserMode>> newModes = new CIKeyMap<>(DefaultActorTracker.this.client);
            Optional<ISupportParameter.Prefix> prefix = DefaultActorTracker.this.client.getServerInfo().getISupportParameter("PREFIX", ISupportParameter.Prefix.class);
            Comparator<ChannelUserMode> comparator = prefix
                    .<Comparator<ChannelUserMode>>map(prefix1 -> Comparator.comparingInt(prefix1.getModes()::indexOf))
                    .orElseGet(() -> Comparator.comparing(ChannelUserMode::getChar));
            for (Map.Entry<String, Set<ChannelUserMode>> entry : this.modes.entrySet()) {
                SortedSet<ChannelUserMode> newSet = new TreeSet<>(comparator);
                newSet.addAll(entry.getValue());
                newModes.put(entry.getKey(), newSet);
            }
            Map<String, User> nickMap = this.modes.keySet().stream()
                    .map(DefaultActorTracker.this.trackedUsers::get)
                    .filter(Objects::nonNull)
                    .map(IrcUser::snapshot)
                    .collect(Collectors.toMap(User::getNick, Function.identity()));
            return super.snapshot(() -> new DefaultChannel(DefaultActorTracker.this.client, this.getName(),
                    new DefaultChannelTopic(IrcChannel.this.topicTime, IrcChannel.this.topic, IrcChannel.this.topicSetter),
                    channelModes, modeInfoLists, newModes, new ArrayList<>(this.modes.keySet()),
                    nickMap, new ArrayList<>(nickMap.values()), this.fullListReceived, this.commands));
        }

        void trackMode(@Nonnull ChannelMode mode, boolean track) {
            if (track && this.trackedModes.add(mode.getChar())) {
                new ChannelModeCommand(DefaultActorTracker.this.client, this.getName()).add(true, mode).execute();
            } else if (!track) {
                this.trackedModes.remove(mode.getChar());
            }
        }

        void setModeInfoList(char character, @Nonnull List<ModeInfo> modeInfoList) {
            if (!this.trackedModes.contains(character)) {
                return;
            }
            this.modeInfoLists.put(character, modeInfoList);
            this.markStale();
        }

        void trackModeInfo(boolean add, @Nonnull ModeInfo modeInfo) {
            if (!this.trackedModes.contains(modeInfo.getMode().getChar())) {
                return;
            }
            if (add) {
                this.modeInfoLists.get(modeInfo.getMode().getChar()).add(modeInfo);
            } else {
                Iterator<ModeInfo> iterator = this.modeInfoLists.get(modeInfo.getMode().getChar()).iterator();
                while (iterator.hasNext()) {
                    if (modeInfo.getMask().equals(iterator.next().getMask())) {
                        iterator.remove();
                        return;
                    }
                }
            }
        }

        void trackUser(@Nonnull User user, @Nonnull Set<ChannelUserMode> modes) {
            DefaultActorTracker.this.trackUser(user);
            this.setModes(user.getNick(), modes);
            this.markStale();
            DefaultActorTracker.this.staleUser(user.getNick());
        }

        void trackNick(@Nonnull String nick, @Nonnull Set<ChannelUserMode> modes) {
            String nickname = nick;
            int index;
            if ((index = nick.indexOf('!')) >= 0) { // userhost-in-names
                nickname = nick.substring(0, index);
                if (!DefaultActorTracker.this.trackedUsers.containsKey(nickname)) {
                    IrcUser user = DefaultActorTracker.this.getUserByName(nick);
                    if (user != null) {
                        DefaultActorTracker.this.trackUser(user.snapshot());
                    }
                }
            }
            if (!this.modes.containsKey(nickname) || this.modes.get(nickname).isEmpty()) {
                this.setModes(nickname, modes);
            }
            this.markStale();
        }

        void trackUserModeAdd(@Nonnull String nick, @Nonnull ChannelUserMode mode) {
            this.getModes(nick).add(mode);
            this.markStale();
        }

        void trackUserModeRemove(@Nonnull String nick, @Nonnull ChannelUserMode mode) {
            this.getModes(nick).remove(mode);
            this.markStale();
        }

        private void trackUserNick(@Nonnull String oldNick, @Nonnull String newNick) {
            Set<ChannelUserMode> modes = this.modes.remove(oldNick);
            if (modes != null) {
                this.setModes(newNick, modes);
            }
            this.markStale();
        }

        void trackUserPart(@Nonnull String nick) {
            this.modes.remove(nick);
            DefaultActorTracker.this.checkUserForTracking(nick);
            DefaultActorTracker.this.staleUser(nick);
            this.markStale();
        }

        @Nonnull
        private Set<ChannelUserMode> getModes(@Nonnull String nick) {
            return this.modes.computeIfAbsent(nick, k -> new HashSet<>());
        }

        private void setModes(@Nonnull String nick, @Nonnull Set<ChannelUserMode> modes) {
            this.modes.put(nick, new HashSet<>(modes));
            this.markStale();
        }

        void updateChannelModes(ModeStatusList<ChannelMode> statusList) {
            statusList.getStatuses().stream().filter(status -> (status.getMode() instanceof ChannelUserMode) && (status.getParameter().isPresent())).forEach(status -> {
                if (status.isSetting()) {
                    this.trackUserModeAdd(status.getParameter().get(), (ChannelUserMode) status.getMode());
                } else {
                    this.trackUserModeRemove(status.getParameter().get(), (ChannelUserMode) status.getMode());
                }
            });
            statusList.getStatuses().stream().filter(status -> !(status.getMode() instanceof ChannelUserMode) && (status.getMode().getType() != ChannelMode.Type.A_MASK)).forEach(status -> {
                if (status.isSetting()) {
                    this.channelModes.put(status.getMode().getChar(), status);
                } else {
                    this.channelModes.remove(status.getMode().getChar());
                }
            });
            this.markStale();
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).toString();
        }
    }

    class IrcUser extends IrcStaleable<DefaultUser> {
        private String account;
        @Nullable
        private String awayMessage;
        private String host;
        private String nick;
        private String user;
        private boolean isAway;
        private String operString;
        private String realName;
        private String server;

        private IrcUser(@Nonnull String mask, @Nonnull String nick, @Nonnull String user, @Nonnull String host) {
            super(mask);
            this.nick = nick;
            this.user = user;
            this.host = host;
        }

        @Nonnull
        String getNick() {
            return this.nick;
        }

        private void setNick(@Nonnull String newNick) {
            this.nick = newNick;
            this.updateName();
        }

        void setAccount(@Nullable String account) {
            this.account = account;
            this.markStale();
        }

        void setAway(String awayMessage) {
            this.awayMessage = awayMessage;
            if (awayMessage != null) {
                this.isAway = true;
            }
            this.markStale();
        }

        void setAway(boolean isAway) {
            this.isAway = isAway;
            if (!isAway) {
                this.awayMessage = null;
            }
            this.markStale();
        }

        void setOperString(@Nonnull String operString) {
            this.operString = operString;
        }

        void setRealName(@Nonnull String realName) {
            this.realName = realName;
            this.markStale();
        }

        void setHost(@Nonnull String host) {
            this.host = host;
            this.updateName();
        }

        void setUser(@Nonnull String user) {
            this.user = user;
            this.updateName();
        }

        void setServer(@Nonnull String server) {
            this.server = server;
            this.markStale();
        }

        private void updateName() {
            this.setName(this.nick + '!' + this.user + '@' + this.host);
            this.markStale();
        }

        @Override
        @Nonnull
        DefaultUser snapshot() {
            Set<String> chanSet = new HashSet<>();
            for (IrcChannel channel : DefaultActorTracker.this.trackedChannels.values()) {
                if (channel.modes.containsKey(this.nick)) {
                    chanSet.add(channel.getName());
                }
            }
            return super.snapshot(() -> new DefaultUser(DefaultActorTracker.this.client, this.getName(), this.account,
                    this.awayMessage, this.nick, this.user, this.host, this.isAway, this.operString, this.realName, this.server, chanSet));
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).toString();
        }
    }


    class IrcServer extends IrcActor {
        private IrcServer(@Nonnull String name) {
            super(name);
        }

        @Override
        @Nonnull
        DefaultServer snapshot() {
            return new DefaultServer(DefaultActorTracker.this.client, this.getName());
        }
    }

    // Valid nick chars: \w\[]^`{}|-_
    // Pattern unescaped: ([\w\\\[\]\^`\{\}\|\-_]+)!([~\w]+)@([\w\.\-:]+)
    // You know what? Screw it.
    // Let's just do it assuming no IRCD can handle following the rules.
    // New pattern: ([^!@]+)!([^!@]+)@([^!@]+)
    private static final Pattern NICK_PATTERN = Pattern.compile("([^!@]+)!([^!@]+)@([^!@]+)");
    private static final Pattern SERVER_PATTERN = Pattern.compile("(?!-)(?:[a-zA-Z\\d\\-]{0,62}[a-zA-Z\\d]\\.){1,126}(?!\\d+)[a-zA-Z\\d]{1,63}");

    private final Client.WithManagement client;

    private final Map<String, IrcChannel> trackedChannels;
    private final Map<String, IrcUser> trackedUsers;

    private boolean queryChannelInformation = true;

    public DefaultActorTracker(@Nonnull Client.WithManagement client) {
        this.client = client;
        this.trackedChannels = new CIKeyMap<>(this.client);
        this.trackedUsers = new CIKeyMap<>(this.client);
    }

    @Nonnull
    @Override
    public Actor getActor(@Nonnull String name) {
        IrcUser user = this.getUserByName(name);
        if (user != null) {
            return user.snapshot();
        }
        Optional<Channel> channel = this.getTrackedChannel(name);
        if (channel.isPresent()) {
            return channel.get();
        } else if (this.client.getServerInfo().isValidChannel(name)) {
            return new IrcChannel(name).snapshot();
        }
        if (name.isEmpty() || SERVER_PATTERN.matcher(name).matches()) {
            return new IrcServer(name).snapshot();
        }
        return new IrcActor(name).snapshot();
    }

    private IrcUser getUserByName(@Nonnull String name) {
        Matcher nickMatcher = NICK_PATTERN.matcher(name);
        if (nickMatcher.matches()) {
            String nick = nickMatcher.group(1);
            IrcUser user = this.trackedUsers.get(nick);
            if (user != null) {
                return user;
            }
            return new IrcUser(name, nick, nickMatcher.group(2), nickMatcher.group(3));
        }
        return null;
    }

    @Nonnull
    @Override
    public Optional<Channel> getTrackedChannel(@Nonnull String channel) {
        IrcChannel ch = this.trackedChannels.get(channel);
        return (ch == null) ? Optional.empty() : Optional.of(ch.snapshot());
    }

    @Nonnull
    @Override
    public Set<Channel> getTrackedChannels() {
        return this.trackedChannels.values().stream().map(IrcChannel::snapshot).collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public Optional<User> getTrackedUser(@Nonnull String nick) {
        IrcUser u = this.trackedUsers.get(nick);
        return (u == null) ? Optional.empty() : Optional.of(u.snapshot());
    }

    @Override
    public boolean isStale(@Nonnull Staleable staleable) {
        if (staleable instanceof Channel) {
            IrcChannel channel = this.trackedChannels.get(((Channel) staleable).getName());
            return (channel == null) || channel.isStale(staleable);
        } else if (staleable instanceof User) {
            IrcUser user = this.trackedUsers.get(((User) staleable).getName());
            return (user == null) || user.isStale(staleable);
        }
        return true;
    }

    @Override
    public void setChannelListReceived(@Nonnull String channel) {
        IrcChannel ch = this.trackedChannels.get(channel);
        if (ch != null) {
            ch.setListReceived();
        }
    }

    @Override
    public void setChannelModeInfoList(@Nonnull String channel, char mode, List<ModeInfo> modeInfo) {
        IrcChannel ch = this.trackedChannels.get(channel);
        if (ch != null) {
            ch.setModeInfoList(mode, modeInfo);
        }
    }

    @Override
    public void setChannelTopic(@Nonnull String channel, @Nonnull String topic) {
        IrcChannel ch = this.trackedChannels.get(channel);
        if (ch != null) {
            ch.setTopic(topic);
        }
    }

    @Override
    public void setChannelTopicInfo(@Nonnull String channel, long time, @Nonnull Actor actor) {
        IrcChannel ch = this.trackedChannels.get(channel);
        if (ch != null) {
            ch.setTopic(time, actor);
        }
    }

    @Override
    public void setQueryChannelInformation(boolean query) {
        this.queryChannelInformation = query;
    }

    @Override
    public void setUserAccount(@Nonnull String nick, @Nullable String account) {
        IrcUser u = this.trackedUsers.get(nick);
        if (u != null) {
            u.setAccount(account);
        }
    }

    @Override
    public void setUserAway(@Nonnull String nick, @Nullable String message) {
        IrcUser u = this.trackedUsers.get(nick);
        if (u != null) {
            u.setAway(message);
        }
    }

    @Override
    public void setUserAway(@Nonnull String nick, boolean away) {
        IrcUser u = this.trackedUsers.get(nick);
        if (u != null) {
            u.setAway(away);
        }
    }

    @Override
    public void setUserOperString(@Nonnull String nick, @Nonnull String operString) {
        IrcUser u = this.trackedUsers.get(nick);
        if (u != null) {
            u.setOperString(operString);
        }
    }

    @Override
    public void setUserRealName(@Nonnull String nick, @Nonnull String realName) {
        IrcUser u = this.trackedUsers.get(nick);
        if (u != null) {
            u.setRealName(realName);
        }
    }

    @Override
    public void setUserServer(@Nonnull String nick, @Nonnull String server) {
        IrcUser u = this.trackedUsers.get(nick);
        if (u != null) {
            u.setServer(server);
        }
    }

    @Override
    public boolean shouldQueryChannelInformation() {
        return this.queryChannelInformation;
    }

    @Override
    public void trackChannel(@Nonnull String channel) {
        if (!this.trackedChannels.containsKey(channel)) {
            IrcChannel ch = new IrcChannel(channel);
            this.trackedChannels.put(channel, ch);
            ch.setTracked(true);
        }
    }

    @Override
    public void trackChannelMode(@Nonnull String channel, @Nonnull ChannelMode mode, boolean track) {
        IrcChannel ch = this.trackedChannels.get(channel);
        if (ch != null) {
            ch.trackMode(mode, track);
        }
    }

    @Override
    public void trackChannelModeInfo(@Nonnull String channel, boolean add, @Nonnull ModeInfo modeInfo) {
        IrcChannel ch = this.trackedChannels.get(channel);
        if (ch != null) {
            ch.trackModeInfo(add, modeInfo);
        }
    }

    @Override
    public void trackChannelNick(@Nonnull String channel, @Nonnull String nick, @Nonnull Set<ChannelUserMode> modes) {
        IrcChannel ch = this.trackedChannels.get(channel);
        if (ch != null) {
            ch.trackNick(nick, modes);
        }
    }

    @Override
    public void trackChannelUser(@Nonnull String channel, @Nonnull User user, @Nonnull Set<ChannelUserMode> modes) {
        IrcChannel ch = this.trackedChannels.get(channel);
        if (ch != null) {
            ch.trackUser(user, modes);
        }
    }

    @Override
    public void trackUser(@Nonnull User user) {
        if (!this.trackedUsers.containsKey(user.getNick())) {
            this.trackedUsers.put(user.getNick(), new IrcUser(user.getName(), user.getNick(), user.getUserString(), user.getHost()));
        }
    }

    @Override
    public void trackUserHostnameChange(@Nonnull String nick, @Nonnull String newHostname) {
        IrcUser u = this.trackedUsers.get(nick);
        if (u != null) {
            u.setHost(newHostname);
        }
    }

    @Override
    public void trackUserNickChange(@Nonnull String oldNick, @Nonnull String newNick) {
        IrcUser user = this.trackedUsers.remove(oldNick);
        user.setNick(newNick);
        this.trackedUsers.put(newNick, user);
        this.trackedChannels.values().forEach(channel -> channel.trackUserNick(oldNick, newNick));
    }

    @Override
    public void trackUserPart(@Nonnull String channel, @Nonnull String nick) {
        IrcChannel ch = this.trackedChannels.remove(channel);
        if (ch != null) {
            ch.trackUserPart(nick);
        }
    }

    @Override
    public void trackUserQuit(@Nonnull String nick) {
        this.trackedUsers.remove(nick);
        this.trackedChannels.values().forEach(channel -> channel.trackUserPart(nick));
    }

    @Override
    public void trackUserUserStringChange(@Nonnull String nick, @Nonnull String newUserString) {
        IrcUser u = this.trackedUsers.get(nick);
        if (u != null) {
            u.setUser(newUserString);
        }
    }

    @Override
    public void unTrackChannel(@Nonnull String channel) {
        IrcChannel ch = this.trackedChannels.remove(channel);
        if (ch != null) {
            ch.setTracked(false);
        }
    }

    @Override
    public void updateChannelModes(@Nonnull String channel, @Nonnull ModeStatusList<ChannelMode> statusList) {
        IrcChannel ch = this.trackedChannels.get(channel);
        if (ch != null) {
            ch.updateChannelModes(statusList);
        }
    }

    @Override
    public void reset() {
        this.trackedChannels.forEach((name, channel) -> channel.markStale());
        this.trackedUsers.forEach((name, user) -> user.markStale());
    }

    /**
     * Considers a user for being removed from tracking.
     *
     * @param nick nickname
     */
    private void checkUserForTracking(@Nonnull String nick) {
        if (!this.client.getServerInfo().getCaseMapping().areEqualIgnoringCase(nick, this.client.getNick())
                && this.trackedChannels.values().stream().noneMatch(channel -> channel.modes.containsKey(nick))) {
            IrcUser removed = this.trackedUsers.remove(nick);
            if (removed != null) {
                removed.markStale();
            }
        }
    }

    private void staleUser(String nick) {
        IrcUser user = this.trackedUsers.get(nick);
        if (user != null) {
            user.markStale();
        }
    }
}
