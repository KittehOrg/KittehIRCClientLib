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
import org.kitteh.irc.client.library.util.LCKeyMap;
import org.kitteh.irc.client.library.util.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
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
        private final Map<IRCUser, Set<ChannelUserMode>> users = new ConcurrentHashMap<>();
        private final Map<String, IRCUser> nickMap;
        private volatile boolean fullListReceived;
        private long lastWho = System.currentTimeMillis();
        private volatile boolean tracked;

        private IRCChannel(String channel, IRCClient client) {
            super(channel, client);
            this.nickMap = new LCKeyMap<>(this.getClient());
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

        IRCChannelSnapshot snapshot() {
            synchronized (this.users) {
                if (this.tracked && !this.fullListReceived) {
                    long now = System.currentTimeMillis();
                    if (now - this.lastWho > 5000) {
                        this.lastWho = now;
                        this.getClient().sendRawLine("WHO " + this.getName());
                    }
                }
            }
            return new IRCChannelSnapshot(this.getName(), this.users, this.getClient(), this.fullListReceived);
        }

        void trackUser(IRCUser user, Set<ChannelUserMode> modes) {
            this.nickMap.put(user.getNick(), user);
            this.users.put(user, modes == null ? new CopyOnWriteArraySet<>() : new CopyOnWriteArraySet<>(modes));
        }

        void trackUserOrUpdate(IRCUser user, Set<ChannelUserMode> modes) {
            if (this.nickMap.containsKey(user.getNick())) {
                // update the old entry with the new one
                IRCUser oldUser = this.nickMap.get(user.getNick());
                this.users.remove(oldUser);
                this.nickMap.remove(user.getNick());

                this.trackUser(user, modes);
            } else {
                this.trackUser(user, modes);
            }
        }

        void trackUserJoin(IRCUser user) {
            this.trackUser(user, null);
        }

        void trackUserModeAdd(String name, ChannelUserMode mode) {
            IRCUser user = this.nickMap.get(name);
            if (user != null) {
                this.users.get(user).add(mode);
            }
        }

        void trackUserModeRemove(String name, ChannelUserMode mode) {
            IRCUser user = this.nickMap.get(name);
            if (user != null) {
                this.users.get(user).remove(mode);
            }
        }

        void trackUserNick(IRCUser oldUser, IRCUser newUser) {
            this.nickMap.remove(oldUser.getNick());
            this.trackUser(newUser, this.users.remove(oldUser));
        }

        void trackUserPart(IRCUser user) {
            this.users.remove(user);
            this.nickMap.remove(user.getNick());
        }
    }

    class IRCChannelSnapshot extends IRCMessageReceiverSnapshot implements Channel {
        private final Map<User, Set<ChannelUserMode>> users;
        private final Map<String, User> nickMap;
        private final boolean complete;

        private IRCChannelSnapshot(String channel, Map<IRCUser, Set<ChannelUserMode>> userMap, IRCClient client, boolean complete) {
            super(channel, client);
            this.complete = complete;
            Map<User, Set<ChannelUserMode>> users = new HashMap<>();
            this.nickMap = new LCKeyMap<>(client);
            userMap.forEach((ircuser, set) -> {
                User user = ircuser.snapshot();
                users.put(user, set);
                this.nickMap.put(user.getNick(), user);
            });
            this.users = Collections.unmodifiableMap(users);
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
        public Map<User, Set<ChannelUserMode>> getUsers() {
            return this.users;
        }

        @Override
        public Pair<User, Set<ChannelUserMode>> getUser(String nick) {
            User user = this.nickMap.get(nick);
            return user == null ? null : new Pair<>(user, this.users.get(user));
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
        protected final String nick;

        private IRCUser(String nick, IRCClient client) {
            super(nick, client);
            this.nick = nick;
        }

        String getNick() {
            return this.nick;
        }

        IRCUserSnapshot snapshot() {
            return new IRCUserSnapshot(null, this.nick, null, null, this.getClient());
        }
    }

    class ExtendedIRCUser extends IRCUser {
        private final String host;
        private final String user;
        private final String mask;

        public ExtendedIRCUser(String mask, String nick, String user, String host, IRCClient client) {
            super(nick, client);
            this.user = user;
            this.host = host;
            this.mask = mask;
        }

        IRCUserSnapshot snapshot() {
            return new IRCUserSnapshot(this.mask, this.nick, this.user, this.host, this.getClient());
        }
    }

    class IRCUserSnapshot extends IRCMessageReceiverSnapshot implements User {
        private final Set<String> channels;
        private final String host;
        private final String nick;
        private final String user;
        private final String mask;

        private IRCUserSnapshot(String mask, String nick, String user, String host, IRCClient client) {
            super(nick, client);
            this.nick = nick;
            this.user = user;
            this.host = host;
            this.mask = mask;
            this.channels = Collections.unmodifiableSet(ActorProvider.this.trackedChannels.values().stream().filter(channel -> channel.getUser(nick) != null).map(IRCChannel::getName).collect(Collectors.toSet()));
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof IRCUserSnapshot) {
                IRCUserSnapshot other = (IRCUserSnapshot) o;

                if (this.getClient() != other.getClient()) {
                    return false;
                }
                if (this.getMask() != null && other.getMask() != null) {
                    return this.toLowerCase(this.getMask()).equals(this.toLowerCase(other.getMask()));
                } else {
                    return this.toLowerCase(this.getNick()).equals(this.toLowerCase(other.getNick()));
                }
            }
            return false;
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
        public String getMask() {
            return this.mask;
        }

        @Override
        public int hashCode() {
            if (this.getMask() != null) {
                return this.toLowerCase(this.getMask()).hashCode() * 2 + this.getClient().hashCode();
            } else {
                return this.toLowerCase(this.getNick()).hashCode() * 2 + this.getClient().hashCode();
            }
        }
    }

    private final IRCClient client;

    // Valid nick chars: \w\[]^`{}|-_
    // Pattern unescaped: ([\w\\\[\]\^`\{\}\|\-_]+)!([~\w]+)@([\w\.\-:]+)
    // You know what? Screw it.
    // Let's just do it assuming no IRCD can handle following the rules.
    // New pattern: ([^!@]+)!([^!@]+)@([^!@]+)
    private final Pattern userMaskPattern = Pattern.compile("([^!@]+)!([^!@]+)@([^!@]+)");

    private final Map<String, IRCChannel> trackedChannels;

    ActorProvider(IRCClient client) {
        this.client = client;
        this.trackedChannels = new LCKeyMap<>(this.client);
    }

    void channelTrack(IRCChannel channel) {
        this.trackedChannels.put(channel.getName(), channel);
        channel.setTracked(true);
    }

    void channelUntrack(IRCChannel channel) {
        this.trackedChannels.remove(channel.getName());
        channel.setTracked(false);
    }

    /**
     * Attempts to return the best possible actor matched for this name,
     * for a host mask this returns a new user, if it doesn't detect a hostmask,
     * it attempts to find a channel by that name, else it returns a plain old actor
     * @param name
     * @return A best matched IRCActor
     */
    IRCActor getActor(String name) {
        Matcher nickMatcher = this.userMaskPattern.matcher(name);
        if (nickMatcher.matches()) {
            return new ExtendedIRCUser(name, nickMatcher.group(1), nickMatcher.group(2), nickMatcher.group(3), this.client);
        }
        IRCChannel channel = this.getChannel(name);
        if (channel != null) {
            return channel;
        }
        return new IRCActor(name, this.client);
    }

    IRCUser getUserByNick(String nick) {
        return new IRCUser(nick, this.client);
    }

    IRCUser getUserByHostMask(String hostMask) {
        Matcher nickMatcher = this.userMaskPattern.matcher(hostMask);
        if (nickMatcher.matches()) {
            return new ExtendedIRCUser(hostMask, nickMatcher.group(1), nickMatcher.group(2), nickMatcher.group(3), this.client);
        }
        return null;
    }

    IRCChannel getChannel(String name) {
        IRCChannel channel = this.trackedChannels.get(name);
        if (channel == null && this.client.getServerInfo().isValidChannel(name)) {
            channel = new IRCChannel(name, this.client);
        }
        return channel;
    }

    IRCUser trackUserNick(IRCUser user, String newNick) {
        if (user instanceof ExtendedIRCUser) {
            // we only have mask info on extended users
            String mask = user.snapshot().getMask();
            IRCUser newUser = this.getUserByHostMask(newNick + mask.substring(mask.indexOf('!'), mask.length()));
            this.trackedChannels.values().forEach(channel -> channel.trackUserNick(user, newUser));
            return newUser;
        } else {
            IRCUser newUser = this.getUserByNick(newNick);
            this.trackedChannels.values().forEach(channel -> channel.trackUserNick(user, newUser));
            return newUser;
        }
    }

    void trackUserQuit(IRCUser user) {
        this.trackedChannels.values().forEach(channel -> channel.trackUserPart(user));
    }
}