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
package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a message sent by the server.
 */
final class IRCServerMessage implements ServerMessage {
    private final String message;
    private final List<MessageTag> tags;

    /**
     * Constructs a server message.
     *
     * @param message full message sent
     * @param tags processed tags
     */
    IRCServerMessage(@Nonnull String message, @Nonnull List<MessageTag> tags) {
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.nullCheck(tags, "Tags cannot be null");
        this.message = message;
        this.tags = Collections.unmodifiableList(new ArrayList<>(tags));
    }

    /**
     * Gets the full content of the line sent by the server, minus linebreak
     * characters \r and \n.
     *
     * @return full message content
     */
    @Nonnull
    @Override
    public String getMessage() {
        return this.message;
    }

    /**
     * Gets the processed message tags, if any, contained in the message.
     *
     * @return message tags or empty if none sent.
     */
    @Nonnull
    @Override
    public List<MessageTag> getTags() {
        return this.tags;
    }
}