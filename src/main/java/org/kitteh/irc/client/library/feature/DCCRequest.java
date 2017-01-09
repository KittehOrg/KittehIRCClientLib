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

    public DCCRequest(@Nonnull CompletableFuture<Void> acceptFuture, @Nonnull String service, @Nonnull String type, @Nonnull InetSocketAddress ip) {
        this.acceptFuture = Sanity.nullCheck(acceptFuture, "acceptFuture cannot be null");
        this.service = Sanity.nullCheck(service, "service cannot be null");
        this.type = Sanity.nullCheck(type, "type cannot be null");
        this.ip = Sanity.nullCheck(ip, "ip cannot be null");
    }

    /**
     * The service of DCC being requested. Usually something like CHAT or SEND.
     *
     * @return the service of DCC being requested
     */
    String getService() {
        return this.service;
    }

    /**
     * The type of DCC being requested. For the CHAT service, this could be 'chat' or 'whiteboard'. For other services, it usually is the same as the service.
     *
     * @return the type of DCC being requested
     */
    String getType() {
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
    public void accept() {
        if (this.acceptFuture.isDone()) {
            throw new IllegalStateException("This request has already been responded to.");
        }
        this.acceptFuture.complete(null);
    }
}
