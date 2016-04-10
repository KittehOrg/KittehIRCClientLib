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
package org.kitteh.irc.client.library.event.client;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ServerMessageEventBase;
import org.kitteh.irc.client.library.feature.ServerInfo;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * Indicates the Client has received a complete MOTD from the server.
 */
public class ClientReceiveMOTDEvent extends ServerMessageEventBase {
    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     */
    public ClientReceiveMOTDEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages) {
        super(client, originalMessages);
    }

    /**
     * Gets the MOTD.
     *
     * @return the motd if known
     * @see ServerInfo#getMOTD()
     */
    @Nonnull
    public Optional<List<String>> getMOTD() {
        return this.getClient().getServerInfo().getMOTD();
    }
}
