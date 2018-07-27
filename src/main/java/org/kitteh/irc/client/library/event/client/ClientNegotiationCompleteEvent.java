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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.event.abstractbase.ClientEventBase;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

/**
 * The {@link Client} has successfully completed negotiation with the server.
 * At this time the client will begin to send queued messages which were not
 * essential to negotiating the connection.
 */
public class ClientNegotiationCompleteEvent extends ClientEventBase {
    private final Actor server;
    private final ServerInfo serverInfo;

    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param server the server to which the client is connected
     * @param serverInfo information about the server
     */
    public ClientNegotiationCompleteEvent(@NonNull Client client, @NonNull Actor server, @NonNull ServerInfo serverInfo) {
        super(client);
        this.server = Sanity.nullCheck(server, "Server cannot be null");
        this.serverInfo = Sanity.nullCheck(serverInfo, "ServerInfo cannot be null");
    }

    /**
     * Gets the server name to which the client has connected
     *
     * @return the server the client is connected to
     */
    @NonNull
    public Actor getServer() {
        return this.server;
    }

    /**
     * Gets information about the server to which the client is currently
     * connected. As long as the client remains connected the information
     * returned by this object will update according to information received
     * from the server. Note that at the time of this event the server has
     * not sent any information beyond its address and version.
     *
     * @return the server information object
     * @see ServerInfo#getAddress()
     * @see ServerInfo#getVersion()
     */
    @NonNull
    public ServerInfo getServerInfo() {
        return this.serverInfo;
    }

    @Override
    @NonNull
    protected ToStringer toStringer() {
        return super.toStringer().add("server", this.server);
    }
}
