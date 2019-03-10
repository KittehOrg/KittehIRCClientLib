/*
 * * Copyright (C) 2013-2019 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.event.abstractbase;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.helper.ServerMultipleMessageEvent;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for events involving messages from the server. Use
 * the helper events if you want to listen to such events.
 *
 * @see ServerMultipleMessageEvent
 */
public abstract class ServerMultipleMessageEventBase extends ClientEventBase implements ServerMultipleMessageEvent {
    private final List<ServerMessage> originalMessages;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     */
    protected ServerMultipleMessageEventBase(@NonNull Client client, @NonNull List<ServerMessage> originalMessages) {
        super(client);
        this.originalMessages = Collections.unmodifiableList(new ArrayList<>(Sanity.nullCheck(originalMessages, "Original messages cannot be null")));
    }

    @Override
    public @NonNull List<ServerMessage> getSource() {
        return this.originalMessages;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("originalMessages", this.originalMessages);
    }
}
