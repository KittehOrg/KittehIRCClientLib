package org.kitteh.irc.event;

import org.kitteh.irc.elements.Actor;

/**
 * An event with a message sent by an actor.
 */
public abstract class ActorSendMessageEvent extends ActorEvent {
    private final String message;

    protected ActorSendMessageEvent(Actor actor, String message) {
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