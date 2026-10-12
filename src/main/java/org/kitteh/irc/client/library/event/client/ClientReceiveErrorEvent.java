/*
 * * Copyright (C) 2013-2026 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.event.helper.MessageEvent;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

/**
 * The server has sent an ERROR message, reporting a fatal error. The server
 * is expected to close the connection after sending this.
 *
 * @see org.kitteh.irc.client.library.event.connection.ClientConnectionClosedEvent
 */
public class ClientReceiveErrorEvent extends ServerMessageEventBase implements MessageEvent {
    private final String message;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param sourceMessage source message
     * @param message the error reason
     */
    public ClientReceiveErrorEvent(@NonNull Client client, @NonNull ServerMessage sourceMessage, @NonNull String message) {
        super(client, sourceMessage);
        this.message = Sanity.nullCheck(message, "Message");
    }

    /**
     * Gets the error reason sent by the server.
     *
     * @return the error reason
     */
    @Override
    public @NonNull String getMessage() {
        return this.message;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("message", this.message);
    }
}
