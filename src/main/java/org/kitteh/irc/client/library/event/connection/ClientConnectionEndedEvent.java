/*
 * * Copyright (C) 2013-2021 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.event.connection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.abstractbase.ClientEventBase;
import org.kitteh.irc.client.library.event.helper.ConnectionEvent;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.Optional;

/**
 * The {@link Client} has had a connection end.
 */
public abstract class ClientConnectionEndedEvent extends ClientEventBase implements ConnectionEvent {
    /**
     * Default reconnection delay, in milliseconds.
     */
    public static final int DEFAULT_RECONNECTION_DELAY_MILLIS = 5000;

    private final boolean canReconnect;
    private final @Nullable Throwable cause;
    private int reconnectionDelayMillis = ClientConnectionEndedEvent.DEFAULT_RECONNECTION_DELAY_MILLIS;
    private boolean attemptReconnect;

    /**
     * Constructs the event.
     *
     * @param client client for which this is occurring
     * @param canReconnect true if the client plans to reconnect
     * @param cause cause, if there was one, closing it
     */
    protected ClientConnectionEndedEvent(@NonNull Client client, boolean canReconnect, @Nullable Throwable cause) {
        super(client);
        this.canReconnect = canReconnect;
        this.attemptReconnect = canReconnect;
        this.cause = cause;
    }

    /**
     * Gets the exception that caused this disconnect, if there was one.
     *
     * @return exception or empty if no exception
     */
    public @NonNull Optional<Throwable> getCause() {
        return Optional.ofNullable(this.cause);
    }

    /**
     * Gets the delay until reconnection.
     *
     * @return reconnection delay, in milliseconds
     * @see #canAttemptReconnect()
     * @see #willAttemptReconnect()
     * @see #setAttemptReconnect(boolean)
     */
    public int getReconnectionDelay() {
        return this.reconnectionDelayMillis;
    }

    /**
     * Gets if the client will be able to reconnect. This is false if, for
     * instance, the client has been shutdown.
     *
     * @return true if the client can reconnect
     */
    public boolean canAttemptReconnect() {
        return this.canReconnect;
    }

    /**
     * Gets if the client plans to reconnect to the server.
     *
     * @return true if the client will attempt to reconnect
     */
    public boolean willAttemptReconnect() {
        return this.canReconnect && this.attemptReconnect;
    }

    /**
     * Sets if the client will attempt to connect again. Note that this will
     * only happen if {@link #canAttemptReconnect()} is true as the client cannot
     * try to reconnect if it has been shutdown. Setting to true will still
     * result in a false {@link #willAttemptReconnect()} if it is not able
     * to reconnect.
     *
     * @param reconnecting true to reconnect, false to not reconnect
     */
    public void setAttemptReconnect(boolean reconnecting) {
        this.attemptReconnect = reconnecting;
    }

    /**
     * Sets the delay until a reconnection attempt, in milliseconds.
     *
     * @param millis reconnection delay
     * @throws IllegalArgumentException if negative
     * @see #canAttemptReconnect()
     * @see #willAttemptReconnect()
     * @see #setAttemptReconnect(boolean)
     */
    public void setReconnectionDelay(int millis) {
        Sanity.truthiness(millis > -1, "Delay cannot be negative");
        this.reconnectionDelayMillis = millis;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer()
                .add("canAttemptReconnect", this.canReconnect)
                .add("willAttemptReconnect", this.attemptReconnect)
                .add("reconnectionDelay", this.reconnectionDelayMillis)
                .add("cause", this.cause);
    }
}
