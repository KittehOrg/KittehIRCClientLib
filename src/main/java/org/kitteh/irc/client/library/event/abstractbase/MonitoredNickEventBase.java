package org.kitteh.irc.client.library.event.abstractbase;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.helper.MonitoredNickStatusEvent;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A nickname tracked by MONITOR!
 */
public abstract class MonitoredNickEventBase extends ServerMessageEventBase implements MonitoredNickStatusEvent {
    private final String nick;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param nick the tracked nick
     */
    protected MonitoredNickEventBase(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull String nick) {
        super(client, originalMessages);
        this.nick = nick;
    }

    @Nonnull
    @Override
    public String getNick() {
        return this.nick;
    }
}
