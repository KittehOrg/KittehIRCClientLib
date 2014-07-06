package org.kitteh.irc.event;

import org.kitteh.irc.elements.Actor;

/**
 * An event with a message sent by an actor.
 */
public abstract class ActorMessageEvent<A extends Actor> extends ActorEvent<A> {
    private final String message;

    protected ActorMessageEvent(A actor, String message) {
        super(actor);
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