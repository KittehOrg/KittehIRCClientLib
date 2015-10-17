package org.kitteh.irc.client.library.event.user;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.MonitoredNickEventBase;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A nick tracked by MONITOR is now offline!
 */
public class MonitoredNickOfflineEvent extends MonitoredNickEventBase {
    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param nick the tracked nick
     */
    public MonitoredNickOfflineEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull String nick) {
        super(client, originalMessages, nick);
    }
}
