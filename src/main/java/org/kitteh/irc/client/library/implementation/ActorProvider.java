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
package org.kitteh.irc.client.library.implementation;


import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.command.ChannelModeCommand;
import org.kitteh.irc.client.library.command.KickCommand;
import org.kitteh.irc.client.library.command.TopicCommand;
import org.kitteh.irc.client.library.command.ChannelModeCommand;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.Server;
import org.kitteh.irc.client.library.element.Staleable;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.ModeInfo;
import org.kitteh.irc.client.library.element.mode.ModeStatus;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.util.CIKeyMap;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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

class ActorProvider implements Resettable {
    class IRCActor {
        private String name;

        private IRCActor(@Nonnull String name) {
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
        IRCActorSnapshot snapshot() {
            return new IRCActorSnapshot(this);
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).toString();
        }
    }

    class IRCActorSnapshot implements Actor {
        private final Client client;
        private final long creationTime = System.currentTimeMillis();
        private final String name;

        private IRCActorSnapshot(@Nonnull IRCActor actor) {
            this.client = ActorProvider.this.client;
            this.name = actor.name;
        }

        @Nonnull
        @Override
        public Client getClient() {
            return this.client;
        }

        @Override
        public long getCreationTime() {
            return this.creationTime;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Nonnull
        String toLowerCase(@Nonnull String input) { // Shortcut
            return this.client.getServerInfo().getCaseMapping().toLowerCase(input);
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("client", this.client).add("name", this.name).toString();
        }
    }

    private class IRCStaleable<T extends Staleable> extends IRCActor {
        @Nullable
        private T snapshot;

        IRCStaleable(@Nonnull String name) {
            super(name);
        }

        boolean isStale(@Nonnull T potentiallyStale) {
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

    class IRCChannel extends IRCStaleable<IRCChannelSnapshot> {
        private final Map<Character, ModeStatus<ChannelMode>> channelModes = new HashMap<>();
        private final Map<Character, List<ModeInfo>> modeInfoLists = new HashMap<>();
        private final Set<Character> trackedModes = new HashSet<>();
        private final Map<String, Set<ChannelUserMode>> modes;
        private final IRCChannelCommands commands;
        private volatile boolean fullListReceived;
        private long lastWho = System.currentTimeMillis();
        private String topic;
        private Actor topicSetter;
        private Instant topicTime;
        private volatile boolean tracked;

        private IRCChannel(@Nonnull String channel) {
            super(channel);
            this.modes = new CIKeyMap<>(ActorProvider.this.client);
            this.commands = new IRCChannelCommands(channel);
            ActorProvider.this.trackedChannels.put(channel, this);
        }

        void setListReceived() {
            this.fullListReceived = true;
            this.markStale();
        }

        private void setTracked(boolean tracked) {
            this.tracked = tracked;
            this.modes.keySet().forEach(ActorProvider.this::staleUser);
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
        IRCChannelSnapshot snapshot() {
            synchronized (this.modes) {
                if (this.tracked && !this.fullListReceived) {
                    long now = System.currentTimeMillis();
                    if ((now - this.lastWho) > 5000) {
                        this.lastWho = now;
                        ActorProvider.this.client.sendRawLineAvoidingDuplication("WHO " + this.getName() + (ActorProvider.this.client.getServerInfo().hasWhoXSupport() ? " %cuhsnfar" : ""));
                    }
                }
            }
            return super.snapshot(() -> new IRCChannelSnapshot(IRCChannel.this, new IRCChannelTopicSnapshot(IRCChannel.this.topicTime, IRCChannel.this.topic, IRCChannel.this.topicSetter)));
        }

        void trackMode(@Nonnull ChannelMode mode, boolean track) {
            if (track && this.trackedModes.add(mode.getChar())) {
                new ChannelModeCommand(ActorProvider.this.client, this.getName()).add(true, mode).execute();
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

        void trackUser(@Nonnull IRCUser user, @Nonnull Set<ChannelUserMode> modes) {
            ActorProvider.this.trackUser(user);
            this.setModes(user.getNick(), modes);
            this.markStale();
            user.markStale();
        }

        void trackNick(@Nonnull String nick, @Nonnull Set<ChannelUserMode> modes) {
            String nickname = nick;
            int index;
            if ((index = nick.indexOf('!')) >= 0) { // userhost-in-names
                nickname = nick.substring(0, index);
                if (!ActorProvider.this.trackedUsers.containsKey(nick)) {
                    IRCActor actor = ActorProvider.this.getActor(nick);
                    if (actor instanceof IRCUser) {
                        IRCUser user = (IRCUser) actor;
                        ActorProvider.this.trackUser(user);
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
            ActorProvider.this.checkUserForTracking(nick);
            ActorProvider.this.staleUser(nick);
            this.markStale();
        }

        @Nonnull
        private Set<ChannelUserMode> getModes(@Nonnull String nick) {
            Set<ChannelUserMode> set = this.modes.get(nick);
            if (set == null) {
                set = new HashSet<>();
                this.modes.put(nick, set);
            }
            return set;
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

    class IRCChannelTopicSnapshot implements Channel.Topic {
        private final Optional<Actor> setter;
        private final Optional<Instant> time;
        private final Optional<String> topic;

        private IRCChannelTopicSnapshot(@Nullable Instant time, @Nullable String topic, @Nullable Actor setter) {
            this.time = Optional.ofNullable(time);
            this.topic = Optional.ofNullable(topic);
            this.setter = Optional.ofNullable(setter);
        }

        @Nonnull
        @Override
        public Optional<Actor> getSetter() {
            return this.setter;
        }

        @Nonnull
        @Override
        public Optional<Instant> getTime() {
            return this.time;
        }

        @Nonnull
        @Override
        public Optional<String> getValue() {
            return this.topic;
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("topic", this.topic).add("setter", this.setter).add("time", this.time).toString();
        }
    }

    class IRCChannelSnapshot extends IRCActorSnapshot implements Channel {
        private final ModeStatusList<ChannelMode> channelModes;
        private final Map<Character, List<ModeInfo>> modeInfoLists;
        private final Map<String, SortedSet<ChannelUserMode>> modes;
        private final List<String> names;
        private final Map<String, User> nickMap;
        private final List<User> users;
        private final boolean complete;
        private final Topic topic;
        private final IRCChannelCommands commands;

        private IRCChannelSnapshot(@Nonnull IRCChannel channel, @Nonnull Topic topic) {
            super(channel);
            this.complete = channel.fullListReceived;
            this.channelModes = ModeStatusList.of(channel.channelModes.values());
            this.topic = topic;
            this.commands = channel.commands;
            this.modeInfoLists = new HashMap<>();
            for (Map.Entry<Character, List<ModeInfo>> entry : channel.modeInfoLists.entrySet()) {
                this.modeInfoLists.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
            }
            channel.trackedModes.stream().filter(character -> !this.modeInfoLists.containsKey(character)).forEach(character -> this.modeInfoLists.put(character, Collections.unmodifiableList(new ArrayList<>())));
            Map<String, SortedSet<ChannelUserMode>> newModes = new CIKeyMap<>(ActorProvider.this.client);
            Optional<ISupportParameter.Prefix> prefix = ActorProvider.this.client.getServerInfo().getISupportParameter("PREFIX", ISupportParameter.Prefix.class);
            Comparator<ChannelUserMode> comparator = prefix.isPresent() ? Comparator.comparingInt(prefix.get().getModes()::indexOf) : Comparator.comparing(ChannelUserMode::getChar);
            for (Map.Entry<String, Set<ChannelUserMode>> entry : channel.modes.entrySet()) {
                SortedSet<ChannelUserMode> newSet = new TreeSet<>(comparator);
                newSet.addAll(entry.getValue());
                newModes.put(entry.getKey(), newSet);
            }
            this.modes = Collections.unmodifiableMap(newModes);
            this.names = Collections.unmodifiableList(new ArrayList<>(this.modes.keySet()));
            this.nickMap = Collections.unmodifiableMap(channel.modes.keySet().stream().map(ActorProvider.this.trackedUsers::get).filter(Objects::nonNull).map(IRCUser::snapshot).collect(Collectors.toMap(User::getNick, Function.identity())));
            this.users = Collections.unmodifiableList(new ArrayList<>(this.nickMap.values()));
        }

        @Override
        public boolean equals(Object o) {
            // RFC 2812 section 1.3 'Channel names are case insensitive.'
            return (o instanceof IRCChannelSnapshot) && (((IRCChannelSnapshot) o).getClient() == this.getClient()) && this.toLowerCase(((Channel) o).getName()).equals(this.toLowerCase((this.getName())));
        }

        @Nonnull
        @Override
        public String getMessagingName() {
            return this.getName();
        }

        @Nonnull
        @Override
        public Optional<List<ModeInfo>> getModeInfoList(@Nonnull ChannelMode mode) {
            Sanity.nullCheck(mode, "Mode cannot be null");
            Sanity.truthiness(mode.getType() == ChannelMode.Type.A_MASK, "Mode type must be A, found " + mode.getType());
            return Optional.ofNullable(this.modeInfoLists.get(mode.getChar()));
        }

        @Override
        @Nonnull
        public ModeStatusList<ChannelMode> getModes() {
            return this.channelModes;
        }

        @Nonnull
        @Override
        public List<String> getNicknames() {
            return this.names;
        }

        @Nonnull
        @Override
        public Topic getTopic() {
            return this.topic;
        }

        @Nonnull
        @Override
        public Optional<User> getUser(@Nonnull String nick) {
            Sanity.nullCheck(nick, "Nick cannot be null");
            return Optional.ofNullable(this.nickMap.get(nick));
        }

        @Nonnull
        @Override
        public Optional<SortedSet<ChannelUserMode>> getUserModes(@Nonnull String nick) {
            Sanity.nullCheck(nick, "Nick cannot be null");
            return Optional.ofNullable(this.modes.get(nick));
        }

        @Nonnull
        @Override
        public List<User> getUsers() {
            return this.users;
        }

        @Override
        public boolean hasCompleteUserData() {
            return this.complete;
        }

        @Override
        public void setModeInfoTracking(@Nonnull ChannelMode mode, boolean track) {
            Sanity.nullCheck(mode, "Mode cannot be null");
            Sanity.truthiness(mode.getType() == ChannelMode.Type.A_MASK, "Mode type must be A, found " + mode.getType());
            Sanity.truthiness((mode.getChar() == 'b') || (mode.getChar() == 'e') || (mode.getChar() == 'I') || (mode.getChar() == 'q'), "Only modes b, e, I, and q supported");
            IRCChannel channel = ActorProvider.this.getTrackedChannel(this.getName());
            if (channel == null) {
                throw new IllegalStateException("Not currently in channel " + this.getName());
            }
            channel.trackMode(mode, track);
        }

        @Nonnull
        @Override
        public Commands commands() {
            return this.commands;
        }

        @Override
        public int hashCode() {
            // RFC 2812 section 1.3 'Channel names are case insensitive.'
            return (this.toLowerCase(this.getName()).hashCode() * 2) + this.getClient().hashCode();
        }

        @Override
        public boolean isStale() {
            IRCChannel channel = ActorProvider.this.getTrackedChannel(this.getName());
            return (channel == null) || channel.isStale(this);
        }

        @Override
        @Nonnull
        public String toString() {
            return new ToStringer(this).add("client", this.getClient()).add("name", this.getName()).add("complete", this.complete).add("users", this.users.size()).toString();
        }
    }

    class IRCChannelCommands implements Channel.Commands {
        private String channel;

        IRCChannelCommands(@Nonnull String channel) {
            this.channel = channel;
        }

        @Nonnull
        @Override
        public ChannelModeCommand mode() {
            return new ChannelModeCommand(ActorProvider.this.client, this.channel);
        }

        @Nonnull
        @Override
        public KickCommand kick() {
            return new KickCommand(ActorProvider.this.client, this.channel);
        }

        @Nonnull
        @Override
        public TopicCommand topic() {
            return new TopicCommand(ActorProvider.this.client, this.channel);
        }
    }

    class IRCUser extends IRCStaleable<IRCUserSnapshot> {
        private String account;
        private String host;
        private String nick;
        private String user;
        private boolean isAway;
        private String realName;
        private String server;

        private IRCUser(@Nonnull String mask, @Nonnull String nick, @Nonnull String user, @Nonnull String host) {
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

        void setAway(boolean isAway) {
            this.isAway = isAway;
            this.markStale();
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
        IRCUserSnapshot snapshot() {
            return super.snapshot(() -> new IRCUserSnapshot(this));
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).toString();
        }
    }

    class IRCUserSnapshot extends IRCActorSnapshot implements User {
        private final Optional<String> account;
        private final Set<String> channels;
        private final boolean isAway;
        private final String host;
        private final String nick;
        private final Optional<String> realName;
        private final Optional<String> server;
        private final String user;

        private IRCUserSnapshot(@Nonnull IRCUser user) {
            super(user);
            this.account = Optional.ofNullable(user.account);
            this.isAway = user.isAway;
            this.nick = user.nick;
            this.user = user.user;
            this.host = user.host;
            this.realName = Optional.ofNullable(user.realName);
            this.server = Optional.ofNullable(user.server);
            this.channels = Collections.unmodifiableSet(ActorProvider.this.trackedChannels.values().stream().filter(channel -> channel.modes.containsKey(this.nick)).map(IRCChannel::getName).collect(Collectors.toSet()));
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof IRCUserSnapshot) && (((IRCUserSnapshot) o).getClient() == this.getClient()) && this.toLowerCase(((IRCUserSnapshot) o).getName()).equals(this.toLowerCase((this.getName())));
        }

        @Nonnull
        @Override
        public Optional<String> getAccount() {
            return this.account;
        }

        @Nonnull
        @Override
        public Set<String> getChannels() {
            return this.channels;
        }

        @Nonnull
        @Override
        public String getHost() {
            return this.host;
        }

        @Nonnull
        @Override
        public String getMessagingName() {
            return this.getNick();
        }

        @Nonnull
        @Override
        public String getNick() {
            return this.nick;
        }

        @Nonnull
        @Override
        public Optional<String> getRealName() {
            return this.realName;
        }

        @Nonnull
        @Override
        public Optional<String> getServer() {
            return this.server;
        }

        @Nonnull
        @Override
        public String getUserString() {
            return this.user;
        }

        @Override
        public int hashCode() {
            return (this.toLowerCase(this.getName()).hashCode() * 2) + this.getClient().hashCode();
        }

        @Override
        public boolean isAway() {
            return this.isAway;
        }

        @Override
        public boolean isStale() {
            IRCUser user = ActorProvider.this.getUser(this.getNick());
            return (user == null) || user.isStale(this);
        }

        @Override
        @Nonnull
        public String toString() {
            return new ToStringer(this).add("client", this.getClient()).add("nick", this.nick).add("user", this.user).add("host", this.host).add("channels", this.channels.size()).toString();
        }
    }

    class IRCServer extends IRCActor {
        private IRCServer(@Nonnull String name) {
            super(name);
        }

        @Override
        @Nonnull
        IRCServerSnapshot snapshot() {
            return new IRCServerSnapshot(this);
        }
    }

    class IRCServerSnapshot extends IRCActorSnapshot implements Server {
        private IRCServerSnapshot(@Nonnull IRCServer actor) {
            super(actor);
        }

        @Override
        @Nonnull
        public String toString() {
            return new ToStringer(this).add("client", this.getClient()).add("name", this.getName()).toString();
        }
    }

    // Valid nick chars: \w\[]^`{}|-_
    // Pattern unescaped: ([\w\\\[\]\^`\{\}\|\-_]+)!([~\w]+)@([\w\.\-:]+)
    // You know what? Screw it.
    // Let's just do it assuming no IRCD can handle following the rules.
    // New pattern: ([^!@]+)!([^!@]+)@([^!@]+)
    private static final Pattern NICK_PATTERN = Pattern.compile("([^!@]+)!([^!@]+)@([^!@]+)");
    private static final Pattern SERVER_PATTERN = Pattern.compile("(?!\\-)(?:[a-zA-Z\\d\\-]{0,62}[a-zA-Z\\d]\\.){1,126}(?!\\d+)[a-zA-Z\\d]{1,63}");

    private final InternalClient client;

    private final Map<String, IRCChannel> trackedChannels;
    private final Map<String, IRCUser> trackedUsers;

    ActorProvider(@Nonnull InternalClient client) {
        this.client = client;
        this.trackedChannels = new CIKeyMap<>(this.client);
        this.trackedUsers = new CIKeyMap<>(this.client);
    }

    @Override
    public void reset() {
        this.trackedChannels.forEach((name, channel) -> channel.markStale());
        this.trackedUsers.forEach((name, user) -> user.markStale());
    }

    void trackChannel(@Nonnull IRCChannel channel) {
        this.trackedChannels.put(channel.getName(), channel);
        channel.setTracked(true);
    }

    void unTrackChannel(@Nonnull IRCChannel channel) {
        this.trackedChannels.remove(channel.getName());
        channel.setTracked(false);
    }

    @Nonnull
    IRCActor getActor(@Nonnull String name) {
        Matcher nickMatcher = NICK_PATTERN.matcher(name);
        if (nickMatcher.matches()) {
            String nick = nickMatcher.group(1);
            IRCUser user = this.trackedUsers.get(nick);
            if (user != null) {
                return user;
            }
            return new IRCUser(name, nick, nickMatcher.group(2), nickMatcher.group(3));
        }
        IRCChannel channel = this.getChannel(name);
        if (channel != null) {
            return channel;
        }
        if (name.isEmpty() || SERVER_PATTERN.matcher(name).matches()) {
            return new IRCServer(name);
        }
        return new IRCActor(name);
    }

    @Nullable
    IRCChannel getChannel(@Nonnull String name) {
        IRCChannel channel = this.getTrackedChannel(name);
        if ((channel == null) && this.client.getServerInfo().isValidChannel(name)) {
            channel = new IRCChannel(name);
        }
        return channel;
    }

    @Nullable
    IRCChannel getTrackedChannel(@Nonnull String name) {
        return this.trackedChannels.get(name);
    }

    @Nonnull
    Set<String> getTrackedChannelNames() {
        return this.trackedChannels.keySet();
    }

    @Nonnull
    Collection<IRCChannel> getTrackedChannels() {
        return this.trackedChannels.values();
    }

    @Nullable
    IRCUser getUser(@Nonnull String nick) {
        return this.trackedUsers.get(nick);
    }

    private void staleUser(String nick) {
        IRCUser user = this.getUser(nick);
        if (user != null) {
            user.markStale();
        }
    }

    void setUserAccount(@Nonnull String nick, @Nullable String account) {
        this.trackedUsers.get(nick).setAccount(account);
    }

    void trackUser(@Nonnull IRCUser user) {
        if (!this.trackedUsers.containsKey(user.getNick())) {
            this.trackedUsers.put(user.getNick(), user);
        }
    }

    void setUserAway(@Nonnull String nick, boolean away) {
        IRCUser user = this.trackedUsers.get(nick);
        if (user != null) {
            user.setAway(away);
        }
    }

    void trackUserNickChange(@Nonnull String oldNick, @Nonnull String newNick) {
        IRCUser user = this.trackedUsers.remove(oldNick);
        user.setNick(newNick);
        this.trackedUsers.put(newNick, user);
        this.trackedChannels.values().forEach(channel -> channel.trackUserNick(oldNick, newNick));
    }

    void trackUserHostnameChange(@Nonnull String nick, @Nonnull String newHostname) {
        IRCUser user = this.trackedUsers.get(nick);
        user.setHost(newHostname);
    }

    void trackUserUserStringChange(@Nonnull String nick, @Nonnull String newUserString) {
        IRCUser user = this.trackedUsers.get(nick);
        user.setUser(newUserString);
    }

    void trackUserQuit(@Nonnull String nick) {
        this.trackedUsers.remove(nick);
        this.trackedChannels.values().forEach(channel -> channel.trackUserPart(nick));
        this.checkUserForTracking(nick);
    }

    private void checkUserForTracking(@Nonnull String nick) {
        if (!this.client.getServerInfo().getCaseMapping().areEqualIgnoringCase(nick, this.client.getNick())
                && this.trackedChannels.values().stream().noneMatch(channel -> channel.modes.containsKey(nick))) {
            IRCUser removed = this.trackedUsers.remove(nick);
            if (removed != null) {
                removed.markStale();
            }
        }
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("client", this.client).toString();
    }
}
