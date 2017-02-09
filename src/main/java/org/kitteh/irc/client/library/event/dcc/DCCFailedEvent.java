/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
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
import org.kitteh.irc.client.library.event.helper.DCCEvent;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Fires when a {@link DCCExchange} connection fails.
 */
public class DCCFailedEvent extends ClientEventBase implements DCCEvent {
    private final DCCExchange exchange;
    private final String reason;
    @Nullable
    private final Throwable cause;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param exchange exchange that failed
     * @param reason reason for failure
     * @param cause cause of failure
     */
    public DCCFailedEvent(@Nonnull Client client, @Nonnull DCCExchange exchange, @Nonnull String reason, @Nullable Throwable cause) {
        super(client);
        this.exchange = Sanity.nullCheck(exchange, "Exchange cannot be null");
        this.reason = Sanity.nullCheck(reason, "reason cannot be null");
        this.cause = cause;
    }

    /**
     * Gets the exception that caused the failure. May be absent if the
     * failure was not caused by an exception, but some sort of invalid
     * state.
     *
     * @return the exception that caused the failure
     */
    @Nonnull
    public Optional<Throwable> getCause() {
        return Optional.ofNullable(this.cause);
    }

    /**
     * Gets the exchange that failed.
     *
     * @return the failed exchange
     */
    @Nonnull
    public DCCExchange getExchange() {
        return this.exchange;
    }

    /**
     * Gets the human-readable reason for the failure.
     *
     * @return the human-readable reason for the failure
     */
    @Nonnull
    public String getReason() {
        return this.reason;
    }

    @Nonnull
    @Override
    protected ToStringer toStringer() {
        return super.toStringer().add("exchange", this.exchange).add("reason", this.reason).add("cause", this.cause);
    }
}
