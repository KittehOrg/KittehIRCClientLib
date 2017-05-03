package org.kitteh.irc.client.library.feature.twitch.messagetag;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Message tag for ban duration.
 */
public class BanDuration extends MessageTagManager.DefaultMessageTag {
    public static final TriFunction<Client, String, Optional<String>, BanDuration> FUNCTION = (client, name, value) -> new BanDuration(name, value, Integer.parseInt(value.get()));

    private final int duration;

    /**
     * Constructs ban reason message tag.
     *
     * @param name tag name
     * @param value tag value or {@link Optional#empty()}
     * @param duration duration, in seconds, of the ban
     */
    public BanDuration(@Nonnull String name, @Nonnull Optional<String> value, int duration) {
        super(name, value);
        this.duration = duration;
    }

    /**
     * Gets the ban duration.
     *
     * @return time, in seconds, the ban will last
     */
    public int getDuration() {
        return this.duration;
    }
}
