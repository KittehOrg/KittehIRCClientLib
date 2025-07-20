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
package org.kitteh.irc.client.library.event.client;

import org.jspecify.annotations.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ServerMessageEventBase;

import java.util.Collections;
import java.util.List;

/**
 * A standard reply. <a href="https://ircv3.net/specs/extensions/standard-replies">https://ircv3.net/specs/extensions/standard-replies</a>
 */
public abstract class StandardReplyEvent extends ServerMessageEventBase {
    /**
     * Types of standard replies.
     */
    public enum Type {
        /**
         * A failure to process a command or an error about the current
         * session.
         */
        FAIL,
        /**
         * Informational.
         */
        NOTE,
        /**
         * Non-fatal feedback.
         */
        WARN
    }

    private final Type type;
    private final String command;
    private final String code;
    private final List<String> context;
    private final String description;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param sourceMessage source message
     * @param type the type
     * @param command the command
     * @param code the code
     * @param context the context
     * @param description the description
     */
    protected StandardReplyEvent(@NonNull Client client, @NonNull ServerMessage sourceMessage, @NonNull Type type, @NonNull String command, @NonNull String code, @NonNull List<String> context, @NonNull String description) {
        super(client, sourceMessage);
        this.type = type;
        this.command = command;
        this.code = code;
        this.context = context;
        this.description = description;
    }

    /**
     * Gets the type of standard reply this is.
     *
     * @return type of reply
     */
    public @NonNull Type getType() {
        return this.type;
    }

    /**
     * Gets the command this message is about, or "*" for no command.
     *
     * @return the command this is about
     */
    public @NonNull String getCommand() {
        return this.command;
    }

    /**
     * Gets the code for this message.
     *
     * @return code
     */
    public @NonNull String getCode() {
        return this.code;
    }

    /**
     * Gets the context, if any, for this message.
     *
     * @return a list of context, or empty if
     */
    public @NonNull List<String> getContext() {
        return Collections.unmodifiableList(this.context);
    }

    /**
     * Gets the description.
     *
     * @return description
     */
    public @NonNull String getDescription() {
        return this.description;
    }
}
