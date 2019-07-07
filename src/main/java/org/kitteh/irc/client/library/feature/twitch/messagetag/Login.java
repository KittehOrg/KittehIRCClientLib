package org.kitteh.irc.client.library.feature.twitch.messagetag;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.TriFunction;

public class Login extends MessageTagManager.DefaultMessageTag {
    /**
     * Name of this message tag.
     */
    public static final String NAME = "login";

    /**
     * Function to create this message tag.
     */
    @SuppressWarnings("ConstantConditions")
    public static final TriFunction<Client, String, String, Login> FUNCTION = (client, name, value) -> new Login(name, value);

    private Login(@NonNull String name, @NonNull String value) {
        super(name, value);
    }
}