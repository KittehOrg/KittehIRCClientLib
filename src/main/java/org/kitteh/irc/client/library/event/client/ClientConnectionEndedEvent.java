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
package org.kitteh.irc.client.library.event.client;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.abstractbase.ClientEventBase;
import org.kitteh.irc.client.library.event.helper.ConnectionEvent;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * The {@link Client} has had a connection end.
 */
public abstract class ClientConnectionEndedEvent extends ClientEventBase implements ConnectionEvent {
    /**
     * Default reconnection delay, in milliseconds.
     */
    public static final int DEFAULT_RECONNECTION_DELAY_MILLIS = 5000;

    @Nullable
    private final Exception exception;
    private boolean reconnecting;
    private int reconnectionDelayMillis = DEFAULT_RECONNECTION_DELAY_MILLIS;

    /**
     * Constructs the event.
     *
     * @param client client for which this is occurring
     * @param reconnecting true if the client plans to reconnect
     * @param exception exception, if there was one, closing it
     */
    protected ClientConnectionEndedEvent(@Nonnull Client client, boolean reconnecting, @Nullable Exception exception) {
        super(client);
        this.reconnecting = reconnecting;
        this.exception = exception;
    }

    /**
     * Gets the exception that caused this disconnect, if there was one.
     *
     * @return exception or empty if no exception
     */
    @Nonnull
    public Optional<Exception> getException() {
        return Optional.ofNullable(this.exception);
    }

    /**
     * Gets the delay until reconnection.
     *
     * @return reconnection delay, in milliseconds
     * @see #isReconnecting()
     * @see #setReconnecting(boolean)
     */
    public int getReconnectionDelay() {
        return this.reconnectionDelayMillis;
    }

    /**
     * Gets if the client plans to reconnect to the server.
     *
     * @return true if the client will attempt to reconnect
     */
    public boolean isReconnecting() {
        return this.reconnecting;
    }

    /**
     * Sets if the client will attempt to connect again.
     *
     * @param reconnecting true to reconnect, false to not reconnect
     */
    public void setReconnecting(boolean reconnecting) {
        // TODO client shutdown condition
        this.reconnecting = reconnecting;
    }

    /**
     * Sets the delay until a reconnection attempt, in milliseconds.
     *
     * @param millis reconnection delay
     * @throws IllegalArgumentException if negative
     * @see #isReconnecting()
     * @see #setReconnecting(boolean)
     */
    public void setReconnectionDelay(int millis) {
        Sanity.truthiness(millis > -1, "Delay cannot be negative");
        this.reconnectionDelayMillis = millis;
    }

    @Override
    @Nonnull
    protected ToStringer toStringer() {
        return super.toStringer().add("isReconnecting", this.reconnecting).add("reconnectionDelay", this.reconnectionDelayMillis);
    }
}
