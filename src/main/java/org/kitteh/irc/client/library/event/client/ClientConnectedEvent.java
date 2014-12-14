package org.kitteh.irc.client.library.event.client;

import org.kitteh.irc.client.library.element.Actor;

/**
 * The {@link org.kitteh.irc.client.library.Client} has successfully
 * connected to the server. At this time the client will begin to send
 * queued messages which were not essential to establishing the connection.
 */
public class ClientConnectedEvent {
    private final Actor server;

    /**
     * Creates the event.
     *
     * @param server the server to which the client is connected
     */
    public ClientConnectedEvent(Actor server) {
        this.server = server;
    }

    /**
     * Gets the server name to which the client has connected
     *
     * @return the server the client is connected to
     */
    public Actor getServer() {
        return this.server;
    }
}