package org.kitteh.irc.client.library.event.client;

import org.kitteh.irc.client.library.util.Sanity;

/**
 * The server has rejected your nick choice.
 */
public class NickRejectedEvent {
    private final String attemptedNick;
    private String newNick;

    public NickRejectedEvent(String attemptedNick, String newNick) {
        this.attemptedNick = attemptedNick;
        this.newNick = newNick;
    }

    /**
     * Gets the nickname which was attempted.
     *
     * @return the attempted nick
     */
    public String getAttemptedNick() {
        return this.attemptedNick;
    }

    /**
     * Gets the new nickname to attempt, by default this is the previously
     * attempted name ({@link #getAttemptedNick()}) with a backtick appended.
     *
     * @return new nick to attempt
     */
    public String getNewNick() {
        return this.newNick;
    }

    public void setNewNick(String newNick) {
        Sanity.nullCheck(newNick, "Nickname cannot be null!");
        Sanity.truthiness(!newNick.equals(this.attemptedNick), "Cannot set new nick to the currently failing nick");
        Sanity.safeMessageCheck(newNick, "nick");
        Sanity.truthiness(!newNick.contains(" "), "Nick cannot contain spaces");
        this.newNick = newNick;
    }
}