package org.kitteh.irc.event;

import org.kitteh.irc.elements.Actor;

/**
 * An event with an actor.
 */
public abstract class ActorEvent {
    private final Actor actor;

    protected ActorEvent(Actor actor) {
        this.actor = actor;
    }

    /**
     * Gets the actor who performed this action.
     *
     * @return the actor
     */
    public Actor getActor() {
        return this.actor;
    }
}
