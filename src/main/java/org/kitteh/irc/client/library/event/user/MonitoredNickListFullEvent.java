package org.kitteh.irc.client.library.event.user;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ServerMessageEventBase;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fires when a MONITOR target addition command is rejected.
 */
public class MonitoredNickListFullEvent extends ServerMessageEventBase {
    private final int limit;
    private final List<String> rejectedNicks;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param limit limit
     * @param rejectedNicks rejected nicks
     */
    public MonitoredNickListFullEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, int limit, @Nonnull List<String> rejectedNicks) {
        super(client, originalMessages);
        this.limit = limit;
        this.rejectedNicks = Collections.unmodifiableList(new ArrayList<>(rejectedNicks));
    }

    /**
     * Gets the maximum number of targets a client can have.
     *
     * @return target limit
     */
    public int getLimit() {
        return this.limit;
    }

    /**
     * Gets the nicknames not added due to the limit.
     *
     * @return rejected nicknames
     */
    @Nonnull
    public List<String> getRejectedNicks() {
        return this.rejectedNicks;
    }
}
