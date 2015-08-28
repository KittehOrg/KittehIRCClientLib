/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ChannelMode;
import org.kitteh.irc.client.library.element.ChannelModeStatus;
import org.kitteh.irc.client.library.element.ChannelModeStatusList;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.kitteh.irc.client.library.element.Staleable;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.util.CIKeyMap;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class ActorProvider {
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

        protected IRCStaleable(@Nonnull String name) {
            super(name);
        }

        protected boolean isStale(@Nonnull T potentiallyStale) {
            return this.snapshot != potentiallyStale;
        }

        protected void markStale() {
            this.snapshot = null;
        }

        @Nonnull
        protected synchronized T snapshot(@Nonnull Supplier<T> supplier) {
            if (this.snapshot != null) {
                return this.snapshot;
            }
            return this.snapshot = supplier.get();
        }
    }

    class IRCChannel extends IRCStaleable<IRCChannelSnapshot> {
        private final Map<Character, ChannelModeStatus> channelModes = new HashMap<>();
        private final Map<String, Set<ChannelUserMode>> modes;
        private volatile boolean fullListReceived;
        private long lastWho = System.currentTimeMillis();
        private Optional<String> topic = Optional.empty();
        private Optional<Actor> topicSetter = Optional.empty();
        private Optional<Instant> topicTime = Optional.empty();
        private volatile boolean tracked;

        private IRCChannel(@Nonnull String channel) {
            super(channel);
            this.modes = new CIKeyMap<>(ActorProvider.this.client);
            ActorProvider.this.trackedChannels.put(channel, this);
        }

        void setListReceived() {
            this.markStale();
            this.fullListReceived = true;
        }

        private void setTracked(boolean tracked) {
            this.markStale();
            this.tracked = tracked;
            this.modes.keySet().forEach(ActorProvider.this::staleUser);
        }

        void setTopic(@Nonnull String topic) {
            this.markStale();
            this.topic = Optional.of(topic);
            this.topicTime = Optional.empty();
            this.topicSetter = Optional.empty();
        }

        void setTopic(long time, @Nonnull Actor actor) {
            this.markStale();
            this.topicTime = Optional.of(Instant.ofEpochMilli(time));
            this.topicSetter = Optional.of(actor);
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

        void trackUser(@Nonnull IRCUser user, @Nonnull Set<ChannelUserMode> modes) {
            this.markStale();
            user.markStale();
            ActorProvider.this.trackUser(user);
            this.setModes(user.getNick(), modes);
        }

        void trackNick(@Nonnull String nick, @Nonnull Set<ChannelUserMode> modes) {
            this.markStale();
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
        }

        void trackUserModeAdd(@Nonnull String nick, @Nonnull ChannelUserMode mode) {
            this.markStale();
            this.getModes(nick).add(mode);
        }

        void trackUserModeRemove(@Nonnull String nick, @Nonnull ChannelUserMode mode) {
            this.markStale();
            this.getModes(nick).remove(mode);
        }

        private void trackUserNick(@Nonnull String oldNick, @Nonnull String newNick) {
            this.markStale();
            Set<ChannelUserMode> modes = this.modes.remove(oldNick);
            if (modes != null) {
                this.setModes(newNick, modes);
            }
        }

        void trackUserPart(@Nonnull String nick) {
            this.markStale();
            this.modes.remove(nick);
            ActorProvider.this.checkUserForTracking(nick);
            ActorProvider.this.staleUser(nick);
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
            this.markStale();
            this.modes.put(nick, new HashSet<>(modes));
        }

        void updateChannelModes(ChannelModeStatusList statusList) {
            this.markStale();
            statusList.getStatuses().stream().filter(status -> !(status.getMode() instanceof ChannelUserMode) && (status.getMode().getType() != ChannelMode.Type.A_MASK)).forEach(status -> {
                if (status.isSetting()) {
                    this.channelModes.put(status.getMode().getChar(), status);
                } else {
                    this.channelModes.remove(status.getMode().getChar());
                }
            });
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

        private IRCChannelTopicSnapshot(@Nonnull Optional<Instant> time, @Nonnull Optional<String> topic, @Nonnull Optional<Actor> setter) {
            this.time = time;
            this.topic = topic;
            this.setter = setter;
        }

        @Nonnull
        @Override
        public Optional<Actor> getSetter() {
            return this.setter;
        }

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
        private final ChannelModeStatusList channelModes;
        private final Map<String, Set<ChannelUserMode>> modes;
        private final List<String> names;
        private final Map<String, User> nickMap;
        private final List<User> users;
        private final boolean complete;
        private final Topic topic;

        private IRCChannelSnapshot(@Nonnull IRCChannel channel, @Nonnull Topic topic) {
            super(channel);
            this.complete = channel.fullListReceived;
            this.channelModes = ChannelModeStatusList.of(channel.channelModes.values());
            this.topic = topic;
            Map<String, Set<ChannelUserMode>> newModes = new CIKeyMap<>(ActorProvider.this.client);
            newModes.putAll(channel.modes);
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

        @Override
        @Nonnull
        public ChannelModeStatusList getModes() {
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
        public Optional<Set<ChannelUserMode>> getUserModes(@Nonnull String nick) {
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

    class IRCUser extends IRCStaleable<IRCUserSnapshot> {
        private String account;
        private final String host;
        private String nick;
        private final String user;
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
            this.markStale();
            this.nick = newNick;
            this.setName(this.nick + '!' + this.user + '@' + this.host);
        }

        void setAccount(@Nullable String account) {
            this.markStale();
            this.account = account;
        }

        void setAway(boolean isAway) {
            this.markStale();
            this.isAway = isAway;
        }

        void setRealName(@Nonnull String realName) {
            this.markStale();
            this.realName = realName;
        }

        void setServer(@Nonnull String server) {
            this.markStale();
            this.server = server;
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

    private final InternalClient client;

    // Valid nick chars: \w\[]^`{}|-_
    // Pattern unescaped: ([\w\\\[\]\^`\{\}\|\-_]+)!([~\w]+)@([\w\.\-:]+)
    // You know what? Screw it.
    // Let's just do it assuming no IRCD can handle following the rules.
    // New pattern: ([^!@]+)!([^!@]+)@([^!@]+)
    private final Pattern nickPattern = Pattern.compile("([^!@]+)!([^!@]+)@([^!@]+)");

    private final Map<String, IRCChannel> trackedChannels;
    private final Map<String, IRCUser> trackedUsers;

    ActorProvider(@Nonnull InternalClient client) {
        this.client = client;
        this.trackedChannels = new CIKeyMap<>(this.client);
        this.trackedUsers = new CIKeyMap<>(this.client);
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
        Matcher nickMatcher = this.nickPattern.matcher(name);
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

    private void trackUser(@Nonnull IRCUser user) {
        if (!this.trackedUsers.containsKey(user.getNick())) {
            this.trackedUsers.put(user.getNick(), user);
        }
    }

    void setUserAway(@Nonnull String nick, boolean away) {
        this.trackedUsers.get(nick).setAway(away);
    }

    void trackUserNickChange(@Nonnull String oldNick, @Nonnull String newNick) {
        IRCUser user = this.trackedUsers.remove(oldNick);
        user.setNick(newNick);
        this.trackedUsers.put(newNick, user);
        this.trackedChannels.values().forEach(channel -> channel.trackUserNick(oldNick, newNick));
    }

    void trackUserQuit(@Nonnull String nick) {
        this.trackedUsers.remove(nick);
        this.trackedChannels.values().forEach(channel -> channel.trackUserPart(nick));
        this.checkUserForTracking(nick);
    }

    private void checkUserForTracking(@Nonnull String nick) {
        if (this.trackedChannels.values().stream().noneMatch(channel -> channel.modes.containsKey(nick))) {
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