/*
 * * Copyright (C) 2013-2023 Matt Baxter https://kitteh.org
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;

import java.util.List;

/**
 * A FAIL has been received.
 */
public class FailEvent extends StandardReplyEvent {
    /**
     * Constructs the event.
     *
     * @param client the client
     * @param sourceMessage source message
     * @param command the command
     * @param code the code
     * @param context the context
     * @param description the description
     */
    public FailEvent(@NonNull Client client, @NonNull ServerMessage sourceMessage, @NonNull String command, @NonNull String code, @NonNull List<String> context, @NonNull String description) {
        super(client, sourceMessage, Type.FAIL, command, code, context, description);
    }
}
