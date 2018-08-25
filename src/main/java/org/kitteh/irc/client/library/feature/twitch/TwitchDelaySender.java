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
package org.kitteh.irc.client.library.feature.twitch;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.sending.QueueProcessingThreadSender;

import java.util.function.Function;

/**
 * A {@link QueueProcessingThreadSender} using a single set delay.
 */
public class TwitchDelaySender extends QueueProcessingThreadSender {
    /**
     * Number of commands per 30 seconds if only sending to channels in which
     * the client is moderator or operator.
     */
    public static final int MOD_OP_PER_THIRTY_SECONDS = 100;
    /**
     * Number of commands per 30 seconds if ever sending out of channels in
     * which the client is not moderator or operator.
     */
    public static final int NON_MOD_OP_PER_THIRTY_SECONDS = 20;

    /**
     * Gets a supplier.
     *
     * @return supplier
     */
    public static Function<Client.WithManagement, TwitchDelaySender> getSupplier() {
        return getSupplier(false);
    }

    /**
     * Gets a supplier.
     *
     * @param modOrOpOnly true if will only ever send to channels in which
     * the client is moderator or operator
     * @return supplier
     */
    public static Function<Client.WithManagement, TwitchDelaySender> getSupplier(boolean modOrOpOnly) {
        return client -> new TwitchDelaySender(client,
                "TwitchDelay " + (modOrOpOnly ? "Only Mod/Op" : "Standard"),
                modOrOpOnly ? MOD_OP_PER_THIRTY_SECONDS : NON_MOD_OP_PER_THIRTY_SECONDS);
    }

    /**
     * Constructs the sending queue.
     *
     * @param client the client
     * @param name name of this sending queue
     * @param perThirtySeconds messages per thirty seconds
     */
    public TwitchDelaySender(@NonNull Client client, @NonNull String name, int perThirtySeconds) {
        super(client, name);
        this.timestamps = new long[perThirtySeconds];
    }

    private int currentIndex = 0;
    private final long[] timestamps;

    @Override
    protected boolean checkReady(@NonNull String message) {
        long now;
        long remaining;
        do {
            now = System.currentTimeMillis();
            remaining = 30000 - (now - this.timestamps[this.currentIndex]);
            if (remaining > 0) {
                try {
                    Thread.sleep(remaining);
                } catch (InterruptedException e) {
                    this.interrupt();
                    return false;
                }
            }
        } while (remaining > 0);
        this.timestamps[this.currentIndex++] = now;
        if (this.currentIndex >= this.timestamps.length) {
            this.currentIndex = 0;
        }
        return true;
    }
}
