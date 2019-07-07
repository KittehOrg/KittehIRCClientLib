package org.kitteh.irc.client.library.feature.twitch.messagetag;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.TriFunction;

public class MsgParamCumulativeMonths extends MessageTagManager.DefaultMessageTag {
    /**
     * Name of this message tag.
     */
    public static final String NAME = "msg-param-cumulative-months";

    /**
     * Function to create this message tag.
     */
    @SuppressWarnings("ConstantConditions")
    public static final TriFunction<Client, String, String, MsgParamCumulativeMonths> FUNCTION = (client, name, value) -> new MsgParamCumulativeMonths(name, value, Integer.parseInt(value));

    private final int months;

    private MsgParamCumulativeMonths(@NonNull String name, @NonNull String value, int months) {
        super(name, value);
        this.months = months;
    }

    /**
     * Gets the number of months the user has subscribed for
     *
     * @return consecutive months of subscription
     */
    public int getMonths() {
        return this.months;
    }
}
