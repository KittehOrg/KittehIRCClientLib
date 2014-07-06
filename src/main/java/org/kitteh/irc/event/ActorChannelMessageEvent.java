package org.kitteh.irc.event;

import org.kitteh.irc.elements.Actor;
import org.kitteh.irc.elements.Channel;

/**
 * When an actor performs an action with a message in a channel.
 */
public abstract class ActorChannelMessageEvent<A extends Actor> extends ActorChannelEvent<A> {
    private final String message;

    protected ActorChannelMessageEvent(A actor, Channel channel, String message) {
        super(actor, channel);
        this.message = message;
    }

    /**
     * Gets the sent message.
     *
     * @return the sent message
     */
    public String getMessage() {
        return this.message;
    }
}