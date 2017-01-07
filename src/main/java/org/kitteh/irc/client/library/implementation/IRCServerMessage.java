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

import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class IRCServerMessage implements ServerMessage {
    static class IRCNumericCommandServerMessage extends IRCServerMessage implements NumericCommandServerMessage {
        private final int command;

        IRCNumericCommandServerMessage(int command, @Nonnull String message, @Nonnull List<MessageTag> tags) {
            super(message, tags);
            this.command = command;
        }

        @Override
        public int getCommand() {
            return this.command;
        }
    }

    static class IRCStringCommandServerMessage extends IRCServerMessage implements StringCommandServerMessage {
        private final String command;

        IRCStringCommandServerMessage(@Nonnull String command, @Nonnull String message, @Nonnull List<MessageTag> tags) {
            super(message, tags);
            this.command = command;
        }

        @Nonnull
        @Override
        public String getCommand() {
            return this.command;
        }
    }

    private final String message;
    private final List<MessageTag> tags;

    IRCServerMessage(@Nonnull String message, @Nonnull List<MessageTag> tags) {
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.nullCheck(tags, "Tags cannot be null");
        this.message = message;
        this.tags = Collections.unmodifiableList(new ArrayList<>(tags));
    }

    @Nonnull
    @Override
    public String getMessage() {
        return this.message;
    }

    @Nonnull
    @Override
    public List<MessageTag> getTags() {
        return this.tags;
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("message", this.message).add("tags", this.tags).toString();
    }
}
