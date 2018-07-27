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
package org.kitteh.irc.client.library.feature.sending;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * A queue for sending messages.
 */
public interface MessageSendingQueue {
    /**
     * Starts sending messages to the given consumer.
     *
     * @param consumer the consumer to consume
     */
    void beginSending(@NonNull Consumer<String> consumer);

    /**
     * Gets if the queue currently has within it a particular message.
     *
     * @param message the message to check for
     * @return true if the message is not yet sent
     */
    boolean contains(@NonNull String message);

    /**
     * Gets the queue's currently set consumer.
     *
     * @return current consumer if present
     */
    @NonNull Optional<Consumer<String>> getConsumer();

    /**
     * Pauses message sending.
     */
    void pause();

    /**
     * Queues a given message.
     *
     * @param message the message to queue
     */
    void queue(@NonNull String message);

    /**
     * Closes down shop, interrupts all threads. No further messages.
     *
     * @return the remaining messages
     */
    @NonNull Queue<String> shutdown();
}
