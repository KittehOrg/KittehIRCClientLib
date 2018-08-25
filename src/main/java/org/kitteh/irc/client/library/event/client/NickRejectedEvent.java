/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.irc.client.library.event.client;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ServerMessageEventBase;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.List;

/**
 * The server has rejected your nick choice.
 */
public class NickRejectedEvent extends ServerMessageEventBase {
    private final String attemptedNick;
    private String newNick;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param attemptedNick the nick that failed
     * @param newNick the new nick to attempt
     */
    public NickRejectedEvent(@NonNull Client client, @NonNull List<ServerMessage> originalMessages, @NonNull String attemptedNick, @NonNull String newNick) {
        super(client, originalMessages);
        this.attemptedNick = Sanity.nullCheck(attemptedNick, "Attempted nick cannot be null");
        this.newNick = Sanity.nullCheck(newNick, "New nick cannot be null");
    }

    /**
     * Gets the nickname which was attempted.
     *
     * @return the attempted nick
     */
    @NonNull
    public String getAttemptedNick() {
        return this.attemptedNick;
    }

    /**
     * Gets the new nickname to attempt, by default this is the previously
     * attempted name ({@link #getAttemptedNick()}) with a ` appended.
     *
     * @return new nick to attempt
     */
    @NonNull
    public String getNewNick() {
        return this.newNick;
    }

    /**
     * Sets the new nickname to attempt.
     *
     * @param newNick a new nickname
     * @throws IllegalArgumentException if nickname is null, is same as the
     * nickname this events reports has failed, contains invalid characters,
     * or contains spaces
     */
    public void setNewNick(@NonNull String newNick) {
        Sanity.safeMessageCheck(newNick, "Nick");
        Sanity.truthiness(!newNick.equals(this.attemptedNick), "Cannot set new nick to the currently failing nick");
        Sanity.truthiness(!newNick.contains(" "), "Nick cannot contain spaces");
        this.newNick = newNick;
    }

    @Override
    @NonNull
    protected ToStringer toStringer() {
        return super.toStringer().add("attemptedNick", this.attemptedNick).add("newNick", this.newNick);
    }
}
