/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ServerMessageEventBase;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * The Client's away status has changed.
 */
public class ClientAwayStatusChangeEvent extends ServerMessageEventBase {
    private final boolean isNowAway;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param isNowAway true if now away, false if now back
     */
    public ClientAwayStatusChangeEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, boolean isNowAway) {
        super(client, originalMessages);
        this.isNowAway = isNowAway;
    }

    /**
     * Gets if the Client is now away.
     *
     * @return true if now away, false if no longer away
     */
    public boolean isNowAway() {
        return this.isNowAway;
    }

    @Override
    @Nonnull
    protected ToStringer toStringer() {
        return super.toStringer().add("isNowAway", this.isNowAway);
    }
}
