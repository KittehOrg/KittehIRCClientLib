/*
 * * Copyright (C) 2013-2020 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.QueueProcessingThread;
import org.kitteh.irc.client.library.util.Sanity;

import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * A {@link MessageSendingQueue} using {@link QueueProcessingThread}.
 */
public class QueueProcessingThreadSender extends QueueProcessingThread<String> implements MessageSendingQueue {
    private final Client client;
    private Consumer<String> consumer = string -> {
    };
    private final Object sendingLock = new Object();
    private volatile boolean waiting = true;

    /**
     * Constructs the sending queue.
     *
     * @param client the client
     * @param name name of this sending queue
     */
    public QueueProcessingThreadSender(@NonNull Client client, @NonNull String name) {
        super("KICL " + Sanity.nullCheck(name, "Name") + " Sending Queue (" + Sanity.nullCheck(client, "Client").getName() + ')');
        this.client = client;
    }

    @Override
    protected void processElement(@NonNull String message) {
        Sanity.nullCheck(message, "Message");
        synchronized (this.sendingLock) {
            if (this.waiting) {
                try {
                    this.sendingLock.wait();
                } catch (InterruptedException e) {
                    return;
                }
            }
            if (this.checkReady(message)) {
                this.consumer.accept(message);
            }
        }
    }

    /**
     * Checks if the message can be sent yet, or at all. This is where delay
     * can be factored in via a thread sleep or other approach.
     *
     * @param message the message to be sent
     * @return true if the message will send, false to drop it
     */
    protected boolean checkReady(@NonNull String message) {
        return true; // NOOP, immediate sending.
    }

    @Override
    protected final void cleanup(@NonNull Queue<String> remainingQueue) {
        // NOOP - Nothing to do about these missed messages but cry
    }

    /**
     * Gets the client.
     *
     * @return the client
     */
    protected @NonNull Client getClient() {
        return this.client;
    }

    @Override
    public void beginSending(@NonNull Consumer<String> consumer) {
        Sanity.nullCheck(consumer, "Consumer");
        synchronized (this.sendingLock) {
            this.consumer = consumer;
            this.waiting = false;
            this.sendingLock.notify();
        }
    }

    @Override
    public @NonNull Optional<Consumer<String>> getConsumer() {
        return Optional.ofNullable(this.consumer);
    }

    @Override
    public void pause() {
        synchronized (this.sendingLock) {
            this.waiting = true;
        }
    }

    @Override
    public @NonNull Queue<String> shutdown() {
        synchronized (this.sendingLock) {
            this.interrupt();
            this.sendingLock.notify();
            return this.getQueue();
        }
    }
}
