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
package org.kitteh.irc.client.library.feature.sending;

import org.kitteh.irc.client.library.Client;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * A {@link QueueProcessingThreadSender} using a single set delay.
 */
public class SingleDelaySender extends QueueProcessingThreadSender {
    /**
     * The default message delay, in milliseconds.
     */
    public static final int DEFAULT_MESSAGE_DELAY = 1200;

    /**
     * Gets a delay supplier with a set delay.
     *
     * @param delay delay to set
     * @return supplier
     */
    public static Function<Client, SingleDelaySender> getSupplier(int delay) {
        return client -> new SingleDelaySender(client, "SingleDelay " + delay, delay);
    }

    private int delay;
    private long last = System.currentTimeMillis();

    /**
     * Constructs the sending queue.
     *
     * @param client the client
     * @param name name of this sending queue
     * @param delay initial delay
     */
    public SingleDelaySender(@Nonnull Client client, @Nonnull String name, int delay) {
        super(client, name);
        this.delay = delay;
    }

    @Override
    protected boolean checkReady(@Nonnull String message) {
        int currentDelay = this.delay;
        if (currentDelay == 0) {
            return true; // Get out as fast as possible OMG!
        }
        long now;
        long remaining;
        do {
            now = System.currentTimeMillis();
            remaining = currentDelay - (now - this.last);
            if (remaining > 0) {
                try {
                    Thread.sleep(remaining);
                } catch (InterruptedException e) {
                    this.interrupt();
                    return false;
                }
            }
        } while (remaining > 0);
        this.last = now;
        return true;
    }

    /**
     * Sets the delay for subsequent messages.
     *
     * @param delay the new delay
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }
}
