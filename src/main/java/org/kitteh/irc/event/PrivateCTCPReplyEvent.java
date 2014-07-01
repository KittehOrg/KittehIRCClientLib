package org.kitteh.irc.event;

import org.kitteh.irc.elements.Actor;

/**
 * The bot has received a reply to a CTCP query!
 */
public class PrivateCTCPReplyEvent {
    private final String message;
    private final Actor sender;

    public PrivateCTCPReplyEvent(Actor sender, String message) {
        this.message = message;
        this.sender = sender;
    }

    /**
     * Gets the CTCP message sent.
     *
     * @return the CTCP message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Gets the sender of the CTCP message.
     *
     * @return the sender
     */
    public Actor getSender() {
        return this.sender;
    }
}