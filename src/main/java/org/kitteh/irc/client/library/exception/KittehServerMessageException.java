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
package org.kitteh.irc.client.library.exception;

import org.kitteh.irc.client.library.element.ServerMessage;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Indicates a problem has occurred in the information sent by the server.
 */
public class KittehServerMessageException extends RuntimeException {
    private final List<ServerMessage> messages;

    /**
     * Constructs the exception.
     *
     * @param message message that couldn't be processed
     * @param problem why it couldn't be processed
     */
    public KittehServerMessageException(ServerMessage message, String problem) {
        this(Collections.singletonList(message), problem);
    }

    /**
     * Constructs the exception.
     *
     * @param messages messages that couldn't be processed
     * @param problem why it couldn't be processed
     */
    public KittehServerMessageException(List<ServerMessage> messages, String problem) {
        super("Error processing message: " + problem + ". Messages: " + System.lineSeparator() + messages.stream().map(ServerMessage::getMessage).collect(Collectors.joining(System.lineSeparator())));
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
    }

    /**
     * Gets the message that led to this exception.
     *
     * @return message
     */
    @Nonnull
    public List<ServerMessage> getServerMessages() {
        return this.messages;
    }
}
