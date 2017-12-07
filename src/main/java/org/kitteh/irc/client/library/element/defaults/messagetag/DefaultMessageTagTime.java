package org.kitteh.irc.client.library.element.defaults.messagetag;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
import java.time.Instant;

public class DefaultMessageTagTime extends MessageTagManager.DefaultMessageTag implements MessageTag.Time {
    public static final TriFunction<Client, String, String, DefaultMessageTagTime> FUNCTION = (client, name, value) -> new DefaultMessageTagTime(name, value, Instant.parse(value));

    private final Instant time;

    private DefaultMessageTagTime(@Nonnull String name, @Nonnull String value, @Nonnull Instant time) {
        super(name, value);
        this.time = time;
    }

    @Nonnull
    @Override
    public Instant getTime() {
        return this.time;
    }
}