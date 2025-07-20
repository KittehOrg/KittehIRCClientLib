/*
 * * Copyright (C) 2013-2025 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.command;

import org.jspecify.annotations.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ClientLinked;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a command which is executable on the server by the client.
 */
public abstract class Command<C extends Command<C>> implements ClientLinked {
    public class Tags {
        private final List<MessageTag> tags = new ArrayList<>();

        public Tags add(@NonNull MessageTag tag) {
            this.tags.add(Sanity.nullCheck(tag, "Tag"));
            return this;
        }

        public Tags add(@NonNull String name) {
            this.tags.add(new MessageTagManager.DefaultMessageTag(Sanity.nullCheck(name, "Name"), null));
            return this;
        }

        public Tags add(@NonNull String name, @NonNull String value) {
            Sanity.nullCheck(name, "Name");
            Sanity.nullCheck(value, "Value");
            this.tags.add(new MessageTagManager.DefaultMessageTag(name, value));
            return this;
        }

        public Tags clear() {
            this.tags.clear();
            return this;
        }

        @SuppressWarnings("unchecked")
        public C then() {
            return (C) Command.this;
        }
    }

    private final Client client;
    private Tags tags;

    /**
     * Constructs the command.
     *
     * @param client the client
     * @throws IllegalArgumentException if client is null
     */
    protected Command(@NonNull Client client) {
        this.client = Sanity.nullCheck(client, "Client");
    }

    /**
     * Gets the client on which this command will be run.
     *
     * @return the client
     */
    @Override
    public @NonNull Client getClient() {
        return this.client;
    }

    /**
     * Executes the command.
     */
    public abstract void execute();

    protected void sendCommandLine(@NonNull String line) {
        this.sendCommandLine(line, false);
    }

    protected void sendCommandLine(@NonNull String line, boolean immediately) {
        if (this.tags == null || this.tags.tags.isEmpty()) {
            if (immediately) {
                this.client.sendRawLineImmediately(line);
            } else {
                this.client.sendRawLine(line);
            }
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append('@');
            for (MessageTag tag : this.tags.tags) {
                builder.append(tag.getAsString()).append(';');
            }
            builder.setCharAt(builder.length() - 1, ' ');
            builder.append(line);
            if (immediately) {
                this.client.sendRawLineImmediately(builder.toString());
            } else {
                this.client.sendRawLine(builder.toString());
            }
        }
    }

    public Tags tags() {
        if (this.tags == null) {
            this.tags = new Tags();
        }
        return this.tags;
    }

    @Override
    public String toString() {
        return this.toStringer().toString();
    }

    /**
     * Generates a partial {@link ToStringer} for the command.
     *
     * @return the partial toString generator
     */
    protected @NonNull ToStringer toStringer() {
        return new ToStringer(this).add("client", this.getClient());
    }
}
