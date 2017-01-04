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
package org.kitteh.irc.client.library.event.dcc;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.abstractbase.ActorEventBase;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Fires when a DCC request is received by the incoming DCC connection handler.
 */
public class DccRequestEvent extends ActorEventBase<User> {
    private final String type;
    private final String ip;
    private final int port;

    public DccRequestEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull User actor, @Nonnull String service, @Nonnull String type, @Nonnull String ip, int port) {
        super(client, originalMessages, actor);
        Sanity.nullCheck(type, "type cannot be null");
        Sanity.nullCheck(ip, "ip cannot be null");
        this.type = type;
        this.ip = ip;
        this.port = port;
    }

    public String getType() {
        return this.type;
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    /**
     * Accepts the request and connect to the socket.
     *
     * <p>This will trigger {@link DccConnectedEvent} if successful and
     * {@link DccFailedEvent} otherwise.</p>
     */
    public void accept() {
        // TODO actually accept :P
    }
}
