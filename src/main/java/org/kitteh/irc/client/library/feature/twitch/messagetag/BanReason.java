package org.kitteh.irc.client.library.feature.twitch.messagetag;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Message tag for ban reason.
 */
public class BanReason extends MessageTagManager.DefaultMessageTag {
    public static final TriFunction<Client, String, Optional<String>, BanReason> FUNCTION = (client, name, value) -> new BanReason(name, value);

    /**
     * Constructs ban reason message tag.
     *
     * @param name tag name
     * @param value tag value or {@link Optional#EMPTY}
     */
    public BanReason(@Nonnull String name, @Nonnull Optional<String> value) {
        super(name, value);
    }
}
