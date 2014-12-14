package org.kitteh.irc.client.library.event.client;

/**
 * The {@link org.kitteh.irc.client.library.Client} has disconnected from
 * the server.
 */
public class ClientConnectionClosedEvent {
    private final boolean reconnecting;

    /**
     * Constructs the event.
     *
     * @param reconnecting true if the client plans to reconnect
     */
    public ClientConnectionClosedEvent(boolean reconnecting) {
        this.reconnecting = reconnecting;
    }

    /**
     * Gets if the client plans to reconnect to the server.
     *
     * @return true if the client will attempt to reconnect
     */
    public boolean isReconnecting() {
        return this.reconnecting;
    }
}