/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.event.dcc;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.DCCExchange;
import org.kitteh.irc.client.library.event.abstractbase.ClientEventBase;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Fires when a {@link DCCExchange} connection fails.
 */
public class DCCFailedEvent extends ClientEventBase {
    private final String reason;
    @Nullable
    private final Throwable cause;

    public DCCFailedEvent(Client client, @Nonnull String reason, @Nullable Throwable cause) {
        super(client);
        this.reason = Sanity.nullCheck(reason, "reason may not be null");
        this.cause = cause;
    }

    /**
     * Returns the human-readable reason for the failure.
     *
     * @return the human-readable reason for the failure
     */
    public String getReason() {
        return this.reason;
    }

    /**
     * Returns the exception that caused the failure. May be absent if the failure was not caused by an exception, but some sort of invalid state.
     *
     * @return the exception that caused the failure
     */
    public Optional<Throwable> getCause() {
        return Optional.ofNullable(this.cause);
    }
}
