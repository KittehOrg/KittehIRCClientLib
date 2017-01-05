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
import org.kitteh.irc.client.library.event.user.PrivateCTCPQueryEvent;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Fires when a DCC request is received by the incoming DCC connection handler. All raw data can be viewed via {@link PrivateCTCPQueryEvent}.
 */
public abstract class DCCRequestEvent extends ActorEventBase<User> {
    private final String type;
    private final InetSocketAddress ip;

    protected DCCRequestEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull User actor, @Nonnull String service, @Nonnull String type, @Nonnull InetSocketAddress ip) {
        super(client, originalMessages, actor);
        this.type = Sanity.nullCheck(type, "type cannot be null");
        this.ip = Sanity.nullCheck(ip, "ip cannot be null");
    }

    public String getType() {
        return this.type;
    }

    /**
     * Returns the address offered in the request. Calling {@link #accept()} will connect to this address.
     *
     * @return the offered address
     */
    public InetSocketAddress getRequestAddress() {
        return this.ip;
    }

    /**
     * Accepts the request and connects to the socket.
     *
     * <p>This will trigger {@link DCCConnectedEvent} if the connection is successful and
     * {@link DCCFailedEvent} otherwise.</p>
     */
    public abstract void accept();

    /**
     * Denies the request.
     */
    public abstract void deny();
}
