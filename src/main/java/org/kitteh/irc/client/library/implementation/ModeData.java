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
import org.kitteh.irc.client.library.element.ChannelMode;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;

final class ModeData {
    abstract static class IRCChannelModeBase {
        private final Client client;
        private final char mode;

        IRCChannelModeBase(@Nonnull Client client, char mode) {
            this.client = client;
            this.mode = mode;
        }

        @Nonnull
        public Client getClient() {
            return this.client;
        }

        public char getChar() {
            return this.mode;
        }
    }

    static class IRCChannelUserMode extends IRCChannelModeBase implements ChannelUserMode {
        private final char prefix;

        IRCChannelUserMode(@Nonnull Client client, char mode, char prefix) {
            super(client, mode);
            this.prefix = prefix;
        }

        @Override
        public char getNickPrefix() {
            return this.prefix;
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("client", this.getClient()).add("char", this.getChar()).add("prefix", this.prefix).toString();
        }
    }

    static class IRCChannelMode extends IRCChannelModeBase implements ChannelMode {
        private final Type type;

        IRCChannelMode(@Nonnull Client client, char mode, @Nonnull Type type) {
            super(client, mode);
            this.type = type;
        }

        @Nonnull
        @Override
        public Type getType() {
            return this.type;
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("client", this.getClient()).add("char", this.getChar()).add("type", this.type).toString();
        }
    }
}
