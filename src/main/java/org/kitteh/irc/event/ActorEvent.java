package org.kitteh.irc.event;

import org.kitteh.irc.elements.Actor;

/**
 * An event with an actor.
 */
public abstract class ActorEvent<A extends Actor> {
    private final A actor;

    protected ActorEvent(A actor) {
        this.actor = actor;
    }

    /**
     * Gets the actor who performed this action.
     *
     * @return the actor
     */
    public A getActor() {
        return this.actor;
    }
}
