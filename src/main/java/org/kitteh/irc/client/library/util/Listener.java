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
package org.kitteh.irc.client.library.util;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;

import java.util.Queue;
import java.util.function.Consumer;

/**
 * A listener is a receiver of items that, if given a consumer, operates a
 * queue processing thread to send items to that consumer. Items are only
 * queued if a consumer is present.
 *
 * @param <Type> type of object listened to
 */
public class Listener<Type> {
    private final class ListenerThread extends QueueProcessingThread<Type> {
        private volatile Consumer<Type> consumer;

        private ListenerThread(@NonNull String clientName, @NonNull Consumer<Type> consumer) {
            super("KICL Listener (" + clientName + ')');
            this.consumer = consumer;
        }

        @Override
        protected void processElement(@NonNull Type element) {
            try {
                this.consumer.accept(element);
            } catch (final Throwable thrown) {
                // NOOP
            }
        }

        @Override
        protected void cleanup(@NonNull Queue<Type> remainingQueue) {
            while (!remainingQueue.isEmpty()) {
                try {
                    this.consumer.accept(remainingQueue.poll());
                } catch (final Throwable thrown) {
                    // NOOP
                }
            }
        }
    }

    private final String clientName;
    private @Nullable ListenerThread thread;

    /**
     * @param client the client
     * @param consumer consumer or null for no consumer
     */
    public Listener(@NonNull Client client, @Nullable Consumer<Type> consumer) {
        this.clientName = Sanity.nullCheck(client, "Client cannot be null").getName();
        this.thread = (consumer == null) ? null : new ListenerThread(this.clientName, consumer);
    }

    /**
     * Queues an item.
     *
     * @param item item to queue
     */
    public void queue(@NonNull Type item) {
        if (this.thread != null) {
            this.thread.queue(item);
        }
    }

    /**
     * Removes the consumer from the listener.
     */
    public void removeConsumer() {
        this.shutdown();
        this.thread = null;
    }

    /**
     * Sets the consumer for the listener, starting a queue processing thread
     * if none existed.
     *
     * @param consumer new consumer
     */
    public void setConsumer(@NonNull Consumer<Type> consumer) {
        if (this.thread == null) {
            this.thread = new ListenerThread(this.clientName, consumer);
        } else {
            this.thread.consumer = consumer;
        }
    }

    /**
     * Shuts down the listener. The listener cannot be brought back from this
     * state, and this method should typically only be called by the Client
     * during shutdown.
     */
    public void shutdown() {
        if (this.thread != null) {
            this.thread.interrupt();
        }
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).add("clientName", this.clientName).toString();
    }
}
