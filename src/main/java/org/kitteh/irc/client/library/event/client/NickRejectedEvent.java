/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.abstractbase.ClientEventBase;
import org.kitteh.irc.client.library.util.Sanity;

/**
 * The server has rejected your nick choice.
 */
public class NickRejectedEvent extends ClientEventBase {
    private final String attemptedNick;
    private String newNick;

    public NickRejectedEvent(Client client, String attemptedNick, String newNick) {
        super(client);
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