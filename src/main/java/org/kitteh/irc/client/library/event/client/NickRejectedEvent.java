/*
 * * Copyright (C) 2013-2026 Matt Baxter https://kitteh.org
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

import org.jspecify.annotations.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ServerMessageEventBase;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

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
     * @param sourceMessage source message
     * @param attemptedNick the nick that failed
     * @param newNick the new nick to attempt
     */
    public NickRejectedEvent(@NonNull Client client, @NonNull ServerMessage sourceMessage, @NonNull String attemptedNick, @NonNull String newNick) {
        super(client, sourceMessage);
        this.attemptedNick = Sanity.nullCheck(attemptedNick, "Attempted nick");
        this.newNick = Sanity.nullCheck(newNick, "New nick");
    }

    /**
     * Gets the nickname which was attempted.
     *
     * @return the attempted nick
     */
    public @NonNull String getAttemptedNick() {
        return this.attemptedNick;
    }

    /**
     * Gets the new nickname to attempt. By default, this is the intended nick
     * with a ` appended on the first rejection and then an increasing
     * number appended, shortened if the server is found to truncate nicks or
     * if a long nick is rejected as erroneous. After repeated rejections, the
     * intended nick is reduced to the characters allowed by RFC 2812.
     * <p>
     * The default listener only sends the default nick automatically while
     * connecting, for a limited number of attempts. After the client is
     * connected, or once the attempts are exhausted, a nick is only sent if
     * one is set via {@link #setNewNick(String)}.
     *
     * @return new nick to attempt
     */
    public @NonNull String getNewNick() {
        return this.newNick;
    }

    /**
     * Sets the new nickname to attempt.
     *
     * @param newNick a new nickname
     * @throws IllegalArgumentException if nickname is null, is same as the
     * nickname this event reports has failed, or is invalid per
     * {@link Sanity#nickCheck(String)}
     */
    public void setNewNick(@NonNull String newNick) {
        Sanity.nickCheck(newNick);
        Sanity.truthiness(!newNick.equals(this.attemptedNick), "Cannot set new nick to the currently failing nick");
        this.newNick = newNick;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("attemptedNick", this.attemptedNick).add("newNick", this.newNick);
    }
}
