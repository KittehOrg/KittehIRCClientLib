/*
 * * Copyright (C) 2013-2021 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.defaults.element;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link ServerMessage}.
 */
public class DefaultServerMessage implements ServerMessage {
    /**
     * Default implementation of {@link NumericCommandServerMessage}.
     */
    public static class NumericCommand extends DefaultServerMessage implements NumericCommandServerMessage {
        private final int command;

        /**
         * Constructs a numeric command message.
         *
         * @param command numeric
         * @param message full message
         * @param tags message tags
         */
        public NumericCommand(int command, @NonNull String message, @NonNull List<MessageTag> tags) {
            super(message, tags);
            this.command = command;
        }

        @Override
        public int getCommand() {
            return this.command;
        }
    }

    /**
     * Default implementation of {@link StringCommandServerMessage}.
     */
    public static class StringCommand extends DefaultServerMessage implements StringCommandServerMessage {
        private final String command;

        /**
         * Constructs a string command message.
         *
         * @param command command
         * @param message full message
         * @param tags message tags
         */
        public StringCommand(@NonNull String command, @NonNull String message, @NonNull List<MessageTag> tags) {
            super(message, tags);
            this.command = command;
        }

        @Override
        public @NonNull String getCommand() {
            return this.command;
        }
    }

    private final String message;
    private final List<MessageTag> tags;

    /**
     * Constructs a sad, non-command message, only used in KICL for bad
     * messages going to a {@link KittehServerMessageException}.
     *
     * @param message full message
     * @param tags parsed tags
     */
    public DefaultServerMessage(@NonNull String message, @NonNull List<MessageTag> tags) {
        Sanity.nullCheck(message, "Message");
        Sanity.nullCheck(tags, "Tags");
        this.message = message;
        this.tags = Collections.unmodifiableList(new ArrayList<>(tags));
    }

    @Override
    public @NonNull String getMessage() {
        return this.message;
    }

    @Override
    public final @NonNull List<MessageTag> getTags() {
        return this.tags;
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).add("message", this.message).add("tags", this.tags).toString();
    }
}
