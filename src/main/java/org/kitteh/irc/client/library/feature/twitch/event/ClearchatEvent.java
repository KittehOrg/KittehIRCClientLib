package org.kitteh.irc.client.library.feature.twitch.event;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ChannelEventBase;
import org.kitteh.irc.client.library.event.helper.ChannelEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * An event for when Twitch sends a CLEARCHAT message meaning a ban has
 * happened.
 */
public class ClearchatEvent extends ChannelEventBase implements ChannelEvent {
    private final String banReason;
    private final OptionalInt banDuration;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param channel the channel
     * @param banReason ban reason
     * @param banDuration ban duration
     */
    public ClearchatEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull Channel channel, @Nonnull String banReason, @Nonnull OptionalInt banDuration) {
        super(client, originalMessages, channel);
        this.banReason = banReason;
        this.banDuration = banDuration;
    }

    /**
     * Gets the ban duration.
     *
     * @return ban duration in seconds or {@link Optional#EMPTY} if
     * permanent
     */
    public OptionalInt getBanDuration() {
        return this.banDuration;
    }

    /**
     * Gets the ban reason.
     *
     * @return ban reason
     */
    public String getBanReason() {
        return this.banReason;
    }
}
