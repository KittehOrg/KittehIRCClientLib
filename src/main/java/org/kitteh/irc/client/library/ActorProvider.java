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
import org.kitteh.irc.client.library.element.User;

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
        public String getName() {
            return this.name;
        }
    }

    class IRCChannel extends IRCActor implements Channel {
        IRCChannel(String channel, IRCClient client) {
            super(channel, client);
        }

        @Override
        public boolean equals(Object o) {
            // RFC 2812 section 1.3 'Channel names are case insensitive.'
            return o instanceof IRCChannel && ((IRCChannel) o).client == this.client && ActorProvider.this.toLowerCase(((Channel) o).getName()).equals(ActorProvider.this.toLowerCase((this.getName())));
        }

        @Override
        public int hashCode() {
            // RFC 2812 section 1.3 'Channel names are case insensitive.'
            return ActorProvider.this.toLowerCase(this.getName()).hashCode() * 2 + this.client.hashCode();
        }
    }

    class IRCUser extends IRCActor implements User {
        private final String host;
        private final String nick;
        private final String user;

        IRCUser(String mask, IRCClient client) {
            super(mask, client);
            Matcher matcher = nickPattern.matcher(mask);
            matcher.find();
            this.nick = matcher.group(1);
            this.user = matcher.group(2);
            this.host = matcher.group(3);
        }

        @Override
        public String getHost() {
            return this.host;
        }

        @Override
        public String getNick() {
            return this.nick;
        }

        @Override
        public String getUser() {
            return this.user;
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

    ActorProvider(IRCClient client) {
        this.client = client;
    }

    Actor getActor(String name) {
        if (this.nickPattern.matcher(name).matches() && (this.nickLength < 0 || name.length() <= this.nickLength)) {
            return new IRCUser(name, this.client);
        } else if (this.isValidChannel(name)) {
            return new IRCChannel(name, this.client);
        } else {
            return new IRCActor(name, this.client);
        }
    }

    Channel getChannel(String name) {
        if (this.isValidChannel(name)) {
            return new IRCChannel(name, this.client);
        }
        return null;
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

    private String toLowerCase(String input) {
        return input.toLowerCase(); // TODO
    }
}