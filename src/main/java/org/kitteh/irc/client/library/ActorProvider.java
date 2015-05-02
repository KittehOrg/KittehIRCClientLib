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
package org.kitteh.irc.client.library;


import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.kitteh.irc.client.library.element.MessageReceiver;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.util.CIKeyMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class ActorProvider {
    class IRCActor {
        private final String name;
        private final IRCClient client;

        private IRCActor(String name, IRCClient client) {
            this.client = client;
            this.name = name;
        }

        protected IRCClient getClient() {
            return this.client;
        }

        protected String getName() {
            return this.name;
        }

        IRCActorSnapshot snapshot() {
            return new IRCActorSnapshot(this.name, this.client);
        }
    }

    class IRCActorSnapshot implements Actor {
        private final Client client;
        private final long creationTime = System.currentTimeMillis();
        private final String name;

        private IRCActorSnapshot(String name, IRCClient client) {
            this.client = client;
            this.name = name;
        }

        @Override
        public Client getClient() {
            return this.client;
        }

        @Override
        public long getCreationTime() {
            return this.creationTime;
        }

        @Override
        public String getName() {
            return this.name;
        }

        protected String toLowerCase(String input) { // Shortcut
            return this.client.getServerInfo().getCaseMapping().toLowerCase(input);
        }
    }

    class IRCChannel extends IRCActor {
        private final Map<String, Set<ChannelUserMode>> modes;
        private final Map<String, IRCUser> nickMap;
        private volatile boolean fullListReceived;
        private long lastWho = System.currentTimeMillis();
        private String topic;
        private Actor topicSetter;
        private long topicTime;
        private volatile boolean tracked;

        private IRCChannel(String channel, IRCClient client) {
            super(channel, client);
            this.modes = new CIKeyMap<>(this.getClient());
            this.nickMap = new CIKeyMap<>(this.getClient());
            ActorProvider.this.trackedChannels.put(channel, this);
        }

        IRCUser getUser(String nick) {
            return this.nickMap.get(nick);
        }

        void setListReceived() {
            this.fullListReceived = true;
        }

        private void setTracked(boolean tracked) {
            this.tracked = tracked;
        }

        void setTopic(String topic) {
            this.topic = topic;
            this.topicTime = -1;
            this.topicSetter = null;
        }

        void setTopic(long time, Actor user) {
            this.topicTime = time;
            this.topicSetter = user;
        }

        IRCChannelSnapshot snapshot() {
            synchronized (this.modes) {
                if (this.tracked && !this.fullListReceived) {
                    long now = System.currentTimeMillis();
                    if (now - this.lastWho > 5000) {
                        this.lastWho = now;
                        this.getClient().sendRawLine("WHO " + this.getName());
                    }
                }
            }
            Channel.Topic topic = new IRCChannelTopicSnapshot(this.topicTime, this.topic, this.topicSetter);
            return new IRCChannelSnapshot(this.getName(), this.modes, this.nickMap, this.getClient(), this.fullListReceived, topic);
        }

        void trackNick(String nick, Set<ChannelUserMode> modes) {
            this.getModes(nick).addAll(modes);
        }

        void trackUser(IRCUser user, Set<ChannelUserMode> modes) {
            this.nickMap.put(user.getNick(), user);
            this.modes.put(user.getNick(), modes == null ? new HashSet<>() : new HashSet<>(modes));
        }

        void trackUserJoin(IRCUser user) {
            this.trackUser(user, null);
        }

        void trackUserModeAdd(String nick, ChannelUserMode mode) {
            this.getModes(nick).add(mode);
        }

        void trackUserModeRemove(String nick, ChannelUserMode mode) {
            this.getModes(nick).remove(mode);
        }

        void trackUserNick(IRCUser oldUser, IRCUser newUser) {
            this.nickMap.remove(oldUser.getNick());
            this.trackUser(newUser, this.modes.remove(oldUser.getNick()));
        }

        void trackUserPart(IRCUser user) {
            this.modes.remove(user.getNick());
            this.nickMap.remove(user.getNick());
        }

        private Set<ChannelUserMode> getModes(String nick) {
            Set<ChannelUserMode> set = this.modes.get(nick);
            if (set == null) {
                set = new HashSet<>();
                this.modes.put(nick, set);
            }
            return set;
        }
    }

    class IRCChannelTopicSnapshot implements Channel.Topic {
        private final Actor setter;
        private final long time;
        private final String topic;

        private IRCChannelTopicSnapshot(long time, String topic, Actor setter) {
            this.time = time;
            this.topic = topic;
            this.setter = setter;
        }

        @Override
        public Actor getSetter() {
            return this.setter;
        }

        @Override
        public long getTime() {
            return this.time;
        }

        @Override
        public String getTopic() {
            return this.topic;
        }
    }

    class IRCChannelSnapshot extends IRCMessageReceiverSnapshot implements Channel {
        private final Map<String, Set<ChannelUserMode>> modes;
        private final List<String> names;
        private final Map<String, User> nickMap;
        private final List<User> users;
        private final boolean complete;
        private final Topic topic;

        private IRCChannelSnapshot(String channel, Map<String, Set<ChannelUserMode>> modes, Map<String, IRCUser> nickMap, IRCClient client, boolean complete, Topic topic) {
            super(channel, client);
            this.complete = complete;
            this.topic = topic;
            Map<String, Set<ChannelUserMode>> newModes = new CIKeyMap<>(client);
            newModes.putAll(modes);
            this.modes = Collections.unmodifiableMap(newModes);
            this.names = Collections.unmodifiableList(new ArrayList<>(this.modes.keySet()));
            Map<String, User> newNickMap = new CIKeyMap<>(client);
            nickMap.forEach((nick, user) -> newNickMap.put(nick, user.snapshot()));
            this.nickMap = Collections.unmodifiableMap(newNickMap);
            this.users = Collections.unmodifiableList(new ArrayList<>(this.nickMap.values()));
        }

        @Override
        public boolean equals(Object o) {
            // RFC 2812 section 1.3 'Channel names are case insensitive.'
            return o instanceof IRCChannelSnapshot && ((IRCChannelSnapshot) o).getClient() == this.getClient() && this.toLowerCase(((Channel) o).getName()).equals(this.toLowerCase((this.getName())));
        }

        @Override
        public String getMessagingName() {
            return this.getName();
        }

        @Override
        public List<String> getNicknames() {
            return this.names;
        }

        @Override
        public Topic getTopic() {
            return this.topic;
        }

        @Override
        public User getUser(String nick) {
            return this.nickMap.get(nick);
        }

        @Override
        public Set<ChannelUserMode> getUserModes(String nick) {
            return this.modes.get(nick);
        }

        @Override
        public List<User> getUsers() {
            return this.users;
        }

        @Override
        public boolean isComplete() {
            return this.complete;
        }

        @Override
        public int hashCode() {
            // RFC 2812 section 1.3 'Channel names are case insensitive.'
            return this.toLowerCase(this.getName()).hashCode() * 2 + this.getClient().hashCode();
        }
    }

    static class IRCChannelUserMode implements ChannelUserMode {
        private final Client client;
        private final char mode;
        private final char prefix;

        IRCChannelUserMode(Client client, char mode, char prefix) {
            this.client = client;
            this.mode = mode;
            this.prefix = prefix;
        }

        @Override
        public Client getClient() {
            return this.client;
        }

        @Override
        public char getMode() {
            return this.mode;
        }

        @Override
        public char getPrefix() {
            return this.prefix;
        }
    }

    abstract class IRCMessageReceiverSnapshot extends IRCActorSnapshot implements MessageReceiver {
        protected IRCMessageReceiverSnapshot(String name, IRCClient client) {
            super(name, client);
        }
    }

    class IRCUser extends IRCActor {
        private final String host;
        private final String nick;
        private final String user;

        private IRCUser(String mask, String nick, String user, String host, IRCClient client) {
            super(mask, client);
            this.nick = nick;
            this.user = user;
            this.host = host;
        }

        String getNick() {
            return this.nick;
        }

        IRCUserSnapshot snapshot() {
            return new IRCUserSnapshot(this.getName(), this.nick, this.user, this.host, this.getClient());
        }
    }

    class IRCUserSnapshot extends IRCMessageReceiverSnapshot implements User {
        private final Set<String> channels;
        private final String host;
        private final String nick;
        private final String user;

        private IRCUserSnapshot(String mask, String nick, String user, String host, IRCClient client) {
            super(mask, client);
            this.nick = nick;
            this.user = user;
            this.host = host;
            this.channels = Collections.unmodifiableSet(ActorProvider.this.trackedChannels.values().stream().filter(channel -> channel.getUser(nick) != null).map(IRCChannel::getName).collect(Collectors.toSet()));
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof IRCUserSnapshot && ((IRCUserSnapshot) o).getClient() == this.getClient() && this.toLowerCase(((IRCUserSnapshot) o).getName()).equals(this.toLowerCase((this.getName())));
        }

        @Override
        public Set<String> getChannels() {
            return this.channels;
        }

        @Override
        public String getHost() {
            return this.host;
        }

        @Override
        public String getMessagingName() {
            return this.getNick();
        }

        @Override
        public String getNick() {
            return this.nick;
        }

        @Override
        public String getUser() {
            return this.user;
        }

        @Override
        public int hashCode() {
            return this.toLowerCase(this.getName()).hashCode() * 2 + this.getClient().hashCode();
        }
    }

    private final IRCClient client;

    // Valid nick chars: \w\[]^`{}|-_
    // Pattern unescaped: ([\w\\\[\]\^`\{\}\|\-_]+)!([~\w]+)@([\w\.\-:]+)
    // You know what? Screw it.
    // Let's just do it assuming no IRCD can handle following the rules.
    // New pattern: ([^!@]+)!([^!@]+)@([^!@]+)
    private final Pattern nickPattern = Pattern.compile("([^!@]+)!([^!@]+)@([^!@]+)");

    private final Map<String, IRCChannel> trackedChannels;

    ActorProvider(IRCClient client) {
        this.client = client;
        this.trackedChannels = new CIKeyMap<>(this.client);
    }

    void channelTrack(IRCChannel channel) {
        this.trackedChannels.put(channel.getName(), channel);
        channel.setTracked(true);
    }

    void channelUntrack(IRCChannel channel) {
        this.trackedChannels.remove(channel.getName());
        channel.setTracked(false);
    }

    IRCActor getActor(String name) {
        Matcher nickMatcher = this.nickPattern.matcher(name);
        if (nickMatcher.matches()) {
            return new IRCUser(name, nickMatcher.group(1), nickMatcher.group(2), nickMatcher.group(3), this.client);
        }
        IRCChannel channel = this.getChannel(name);
        if (channel != null) {
            return channel;
        }
        return new IRCActor(name, this.client);
    }

    IRCChannel getChannel(String name) {
        IRCChannel channel = this.trackedChannels.get(name);
        if (channel == null && this.client.getServerInfo().isValidChannel(name)) {
            channel = new IRCChannel(name, this.client);
        }
        return channel;
    }

    IRCUser trackUserNick(IRCUser user, String newNick) {
        IRCUser newUser = (IRCUser) this.getActor(newNick + user.getName().substring(user.getName().indexOf('!'), user.getName().length()));
        this.trackedChannels.values().forEach(channel -> channel.trackUserNick(user, newUser));
        return newUser;
    }

    void trackUserQuit(IRCUser user) {
        this.trackedChannels.values().forEach(channel -> channel.trackUserPart(user));
    }
}