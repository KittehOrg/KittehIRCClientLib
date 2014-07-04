package org.kitteh.irc.event;

import org.kitteh.irc.elements.Actor;
import org.kitteh.irc.elements.Channel;

/**
 * When an actor does something in a channel.
 */
public abstract class ActorChannelEvent extends ActorEvent {
    private final Channel channel;

    protected ActorChannelEvent(Actor actor, Channel channel) {
        super(actor);
        this.channel = channel;
    }

    /**
     * Gets the channel in which the action occured.
     *
     * @return the channel
     */
    public Channel getChannel() {
        return this.channel;
    }
}
