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
package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.Objects;

final class ModeData {
    abstract static class IRCModeBase {
        private final Client client;
        private final char mode;

        IRCModeBase(@Nonnull Client client, char mode) {
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

    static class IRCChannelUserMode extends IRCModeBase implements ChannelUserMode {
        private final char prefix;

        IRCChannelUserMode(@Nonnull Client client, char mode, char prefix) {
            super(client, mode);
            this.prefix = prefix;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IRCChannelUserMode)) {
                return false;
            }
            IRCChannelUserMode other = (IRCChannelUserMode) o;
            return (other.getNickPrefix() == this.getNickPrefix()) && (other.getType() == this.getType()) && (other.getClient().equals(this.getClient())) && (other.getChar() == this.getChar());
        }

        @Override
        public char getNickPrefix() {
            return this.prefix;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getClient(), this.getChar(), this.getType(), this.getNickPrefix());
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("client", this.getClient()).add("char", this.getChar()).add("prefix", this.prefix).toString();
        }
    }

    static class IRCChannelMode extends IRCModeBase implements ChannelMode {
        private final Type type;

        IRCChannelMode(@Nonnull Client client, char mode, @Nonnull Type type) {
            super(client, mode);
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IRCChannelMode)) {
                return false;
            }
            IRCChannelMode other = (IRCChannelMode) o;
            return (other.getType() == this.getType()) && (other.getClient().equals(this.getClient())) && (other.getChar() == this.getChar());
        }

        @Nonnull
        @Override
        public Type getType() {
            return this.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getClient(), this.getChar(), this.type);
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("client", this.getClient()).add("char", this.getChar()).add("type", this.type).toString();
        }
    }

    static class IRCUserMode extends IRCModeBase implements UserMode {
        IRCUserMode(@Nonnull Client client, char mode) {
            super(client, mode);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IRCUserMode)) {
                return false;
            }
            IRCUserMode other = (IRCUserMode) o;
            return (other.getClient().equals(this.getClient())) && (other.getChar() == this.getChar());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getClient(), this.getChar());
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("client", this.getClient()).add("char", this.getChar()).toString();
        }
    }
}
