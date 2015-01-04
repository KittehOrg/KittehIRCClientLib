/*
 * * Copyright (C) 2013-2014 Matt Baxter http://kitteh.org
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ActorProvider {
    class IRCActor implements Actor {
        private final String name;
        protected final Client client;

        IRCActor(String name, IRCClient client) {
            this.client = client;
            this.name = name;
        }

        @Override
        public Client getClient() {
            return this.client;
        }

        @Override
        public String getName() {
            return this.name;
        }

        protected String toLowerCase(String input) { // Shortcut
            return this.client.getServerInfo().getCaseMapping().toLowerCase(input);
        }
    }

    class IRCChannel extends IRCMessageReceiver implements Channel {
        private final Map<User, Set<ChannelUserMode>> users = new ConcurrentHashMap<>();
        private final Map<String, User> nickMap;

        IRCChannel(String channel, IRCClient client) {
            super(channel, client);
            this.nickMap = new LCKeyMap<>(this.getClient());
            ActorProvider.this.trackedChannels.put(channel, this);
        }

        @Override
        public boolean equals(Object o) {
            // RFC 2812 section 1.3 'Channel names are case insensitive.'
            return o instanceof IRCChannel && ((IRCChannel) o).client == this.client && this.toLowerCase(((Channel) o).getName()).equals(this.toLowerCase((this.getName())));
        }

        @Override
        public String getMessagingName() {
            return this.getName();
        }

        @Override
        public Map<User, Set<ChannelUserMode>> getUsers() {
            Map<User, Set<ChannelUserMode>> map = new HashMap<>();
            for (Map.Entry<User, Set<ChannelUserMode>> entry : this.users.entrySet()) {
                map.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            return Collections.unmodifiableMap(map);
        }

        @Override
        public int hashCode() {
            // RFC 2812 section 1.3 'Channel names are case insensitive.'
            return this.toLowerCase(this.getName()).hashCode() * 2 + this.client.hashCode();
        }

        @Override
        public void part(String reason) {
            this.client.removeChannel(this, reason);
        }

        void trackUser(User user, Set<ChannelUserMode> modes) {
            this.nickMap.put(user.getNick(), user);
            this.users.put(user, modes == null ? new CopyOnWriteArraySet<ChannelUserMode>() : new CopyOnWriteArraySet<>(modes));
        }

        void trackUserJoin(User user) {
            this.trackUser(user, null);
        }

        void trackUserModeAdd(String name, ChannelUserMode mode) {
            User user = this.nickMap.get(name);
            if (user != null) {
                this.users.get(user).add(mode);
            }
        }

        void trackUserModeRemove(String name, ChannelUserMode mode) {
            User user = this.nickMap.get(name);
            if (user != null) {
                this.users.get(user).remove(mode);
            }
        }

        void trackUserPart(User user) {
            this.users.remove(user);
            this.nickMap.remove(user.getNick());
        }
    }

    static class IRCChannelUserMode implements ChannelUserMode {
        private final char mode;
        private final char prefix;

        IRCChannelUserMode(char mode, char prefix) {
            this.mode = mode;
            this.prefix = prefix;
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

    private abstract class IRCMessageReceiver extends IRCActor implements MessageReceiver {
        protected IRCMessageReceiver(String name, IRCClient client) {
            super(name, client);
        }

        @Override
        public void sendCTCPMessage(String message) {
            this.client.sendCTCPMessage(this, message);
        }

        @Override
        public void sendMessage(String message) {
            this.client.sendMessage(this, message);
        }

        @Override
        public void sendNotice(String message) {
            this.client.sendNotice(this, message);
        }
    }

    class IRCUser extends IRCMessageReceiver implements User {
        private final String host;
        private final String nick;
        private final String user;

        IRCUser(String mask, String nick, String user, String host, IRCClient client) {
            super(mask, client);
            this.nick = nick;
            this.user = user;
            this.host = host;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof IRCUser && ((IRCUser) o).client == this.client && this.toLowerCase(((IRCUser) o).getName()).equals(this.toLowerCase((this.getName())));
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
            return this.toLowerCase(this.getName()).hashCode() * 2 + this.client.hashCode();
        }
    }

    private final IRCClient client;

    private int channelLength = -1;
    // Pattern: ([#!&\+][^ ,\07\r\n]{1,49})
    // Screw it, let's assume IRCDs disregard length policy
    // New pattern: ([#!&\+][^ ,\07\r\n]+)
    private final Pattern channelPattern = Pattern.compile("([#!&\\+][^ ,\\07\\r\\n]+)");

    private char[] channelPrefixes = new char[]{'#', '&', '!', '+'};

    private int nickLength = -1;
    // Valid nick chars: \w\[]^`{}|-_
    // Pattern unescaped: ([\w\\\[\]\^`\{\}\|\-_]+)!([~\w]+)@([\w\.\-:]+)
    // You know what? Screw it.
    // Let's just do it assuming no IRCD can handle following the rules.
    // New pattern: ([^!@]+)!([^!@]+)@([^!@]+)
    private final Pattern nickPattern = Pattern.compile("([^!@]+)!([^!@]+)@([^!@]+)");

    private final Map<String, IRCChannel> trackedChannels; // TODO stop tracking at some point

    ActorProvider(IRCClient client) {
        this.client = client;
        this.trackedChannels = new LCKeyMap<IRCChannel>(this.client);
    }

    Actor getActor(String name) {
        Matcher nickMatcher = this.nickPattern.matcher(name);
        if (nickMatcher.matches()) {
            return new IRCUser(name, nickMatcher.group(1), nickMatcher.group(2), nickMatcher.group(3), this.client);
        }
        Channel channel = this.getChannel(name);
        if (channel != null) {
            return channel;
        }
        return new IRCActor(name, this.client);
    }

    IRCChannel getChannel(String name) {
        IRCChannel channel = this.trackedChannels.get(name);
        if (channel == null && this.isValidChannel(name)) {
            channel = new IRCChannel(name, this.client);
        }
        return channel;
    }

    int getChannelLength() {
        return this.channelLength;
    }

    char[] getChannelPrefixes() {
        return this.channelPrefixes;
    }

    int getNickLength() {
        return this.nickLength;
    }

    boolean isValidChannel(String name) {
        if (this.channelPattern.matcher(name).matches() && name.length() > 1 && (channelLength < 0 || name.length() <= channelLength)) {
            for (char c : this.channelPrefixes) {
                if (name.charAt(0) == c) {
                    return true;
                }
            }
        }
        return false;
    }

    void setChannelLength(int length) {
        this.channelLength = length;
    }

    void setChannelPrefixes(char[] prefixes) {
        this.channelPrefixes = prefixes;
    }

    void setNickLength(int length) {
        this.nickLength = length;
    }

    void trackUserQuit(User user) {
        for (IRCChannel channel : this.trackedChannels.values()) {
            channel.trackUserPart(user);
        }
    }
}