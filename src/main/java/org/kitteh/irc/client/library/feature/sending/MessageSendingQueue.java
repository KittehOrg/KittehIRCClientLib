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

import javax.annotation.Nonnull;
import java.util.Queue;

/**
 * A queue for sending messages.
 */
public interface MessageSendingQueue {
    /**
     * Called upon client connection, after negotiation is complete. Do not
     * call unless you know what you're doing.
     */
    void beginSending();

    /**
     * Gets if the queue currently has within it a particular message.
     *
     * @param message the message to check for
     * @return true if the message is not yet sent
     */
    boolean contains(@Nonnull String message);

    /**
     * Queues a given message.
     *
     * @param message the message to queue
     */
    void queue(@Nonnull String message);

    /**
     * Closes down shop. No further messages will be sent.
     *
     * @return the remaining messages
     */
    @Nonnull
    Queue<String> shutdown();
}
