/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.feature;

import org.kitteh.irc.client.library.event.dcc.DCCConnectedEvent;
import org.kitteh.irc.client.library.event.dcc.DCCFailedEvent;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a request for DCC that can be responded to.
 */
public class DCCRequest {
    private final CompletableFuture<Void> acceptFuture;
    private final String service;
    private final String type;
    private final InetSocketAddress ip;

    /**
     * Constructs a DCCRequest.
     *
     * @param acceptFuture a completable future on which
     * {@link CompletableFuture#complete(Object)} will be called upon calling
     * {@link #accept()}, which should trigger connection using the
     * information provided in this request.
     * @param service DCC service requested
     * @param type DCC type requested
     * @param ip IP address to which connection will be made
     */
    public DCCRequest(@Nonnull CompletableFuture<Void> acceptFuture, @Nonnull String service, @Nonnull String type, @Nonnull InetSocketAddress ip) {
        this.acceptFuture = Sanity.nullCheck(acceptFuture, "acceptFuture cannot be null");
        this.service = Sanity.nullCheck(service, "service cannot be null");
        this.type = Sanity.nullCheck(type, "type cannot be null");
        this.ip = Sanity.nullCheck(ip, "ip cannot be null");
    }

    /**
     * Accepts the request and connects to the socket.
     *
     * This will trigger {@link DCCConnectedEvent} if the connection is successful and
     * {@link DCCFailedEvent} otherwise.
     */
    public void accept() {
        if (this.acceptFuture.isDone()) {
            throw new IllegalStateException("This request has already been responded to.");
        }
        this.acceptFuture.complete(null);
    }

    /**
     * Returns the address offered in the request. Calling {@link #accept()}
     * will connect to this address.
     *
     * @return the offered address
     */
    @Nonnull
    public InetSocketAddress getRequestAddress() {
        return this.ip;
    }

    /**
     * Gets the service of DCC being requested. Typically CHAT or SEND.
     *
     * @return the service of DCC being requested
     */
    @Nonnull
    public String getService() {
        return this.service;
    }

    /**
     * Gets the type of DCC being requested. For the CHAT service, this could
     * be 'chat' or 'whiteboard'. For other services, it usually is the same
     * as the service.
     *
     * @return the type of DCC being requested
     */
    @Nonnull
    public String getType() {
        return this.type;
    }

    /**
     * Gets if the request has been accepted already.
     *
     * @return true if accepted and false if not
     */
    public boolean isAccepted() {
        return this.acceptFuture.isDone();
    }
}
