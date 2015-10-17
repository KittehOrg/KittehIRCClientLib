package org.kitteh.irc.client.library.event.user;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ServerMessageEventBase;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fires when the server sends the full list of tracked nicknames for the
 * MONITOR feature.
 */
public class MonitoredNickListEvent extends ServerMessageEventBase {
    private final List<String> nicks;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param nicks nicknames tracked
     */
    public MonitoredNickListEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull List<String> nicks) {
        super(client, originalMessages);
        this.nicks = Collections.unmodifiableList(new ArrayList<>(nicks));
    }

    /**
     * Gets the tracked nicknames.
     *
     * @return tracked nicknames
     */
    public List<String> getNicks() {
        return this.nicks;
    }
}
