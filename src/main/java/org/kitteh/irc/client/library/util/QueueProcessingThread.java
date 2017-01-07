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
package org.kitteh.irc.client.library.util;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Self starting processor of queued items on its own thread.
 *
 * @param <Type> type of items queued
 */
public abstract class QueueProcessingThread<Type> extends Thread {
    private final Queue<Type> queue = new ConcurrentLinkedQueue<>();

    /**
     * Creates a thread and starts itself.
     *
     * @param name name of the thread
     */
    protected QueueProcessingThread(@Nonnull String name) {
        this.setName(name);
        this.start();
    }

    @Override
    public void run() {
        dance:
        while (!this.isInterrupted()) {
            synchronized (this.queue) {
                while (this.queue.isEmpty()) {
                    try {
                        this.queue.wait();
                    } catch (InterruptedException e) {
                        break dance;
                    }
                }
            }
            this.processElement(this.queue.poll());
        }
        this.interrupt();
        this.cleanup(this.queue);
    }

    /**
     * This method is called after the thread has been interrupted.
     *
     * @param remainingQueue the queue
     */
    protected void cleanup(@Nonnull Queue<Type> remainingQueue) {
        // NOOP
    }

    /**
     * Processes an element from the queue.
     *
     * @param element next element from the queue
     */
    protected abstract void processElement(@Nonnull Type element);

    /**
     * Gets if the queue contains a specified item.
     *
     * @param item the item
     * @return true if the item is in the queue
     */
    public boolean contains(@Nonnull Type item) {
        return this.queue.contains(Sanity.nullCheck(item, "Item cannot be null"));
    }

    /**
     * Gets a copy of the queue in its current state.
     *
     * @return a queue copy
     */
    @Nonnull
    public Queue<Type> getQueue() {
        return new ArrayDeque<>(this.queue);
    }

    /**
     * Queues an item.
     *
     * @param item item to queue
     */
    public void queue(@Nonnull Type item) {
        Sanity.nullCheck(item, "Item cannot be null");
        synchronized (this.queue) {
            this.queue.add(item);
            this.queue.notify();
        }
    }
}
