package org.kitteh.irc.client.library.event.dcc;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.DccExchange;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.abstractbase.ActorEventBase;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Fires when an unknown protocol or service is encountered by the incoming DCC connection handler.
 */
public class UnknownDccRequestEvent extends ActorEventBase<User> {
    private final String service;
    private final String type;

    public UnknownDccRequestEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull User actor, @Nonnull String service, @Nonnull String type) {
        super(client, originalMessages, actor);
        Sanity.nullCheck(service, "service cannot be null");
        Sanity.nullCheck(type, "type cannot be null");
        this.service = service;
        this.type = type;
    }

    public String getService() {
        return this.service;
    }

    public String getType() {
        return this.type;
    }
}
