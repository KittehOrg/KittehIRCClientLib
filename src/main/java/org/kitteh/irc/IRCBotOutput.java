/*
 * * Copyright (C) 2013-2014 Matt Baxter http://kitteh.org
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
package org.kitteh.irc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

final class IRCBotOutput extends Thread {
    private final Object wait = new Object();
    private final BufferedWriter bufferedWriter;
    private volatile int delay;
    private volatile String quitReason;
    private volatile boolean handleLowPriority = false;
    private final Queue<String> highPriorityQueue = new ConcurrentLinkedQueue<>();
    private final Queue<String> lowPriorityQueue = new ConcurrentLinkedQueue<>();

    IRCBotOutput(BufferedWriter bufferedWriter, String botName, int messageDelay) {
        this.setName("Kitteh IRCBot Output (" + botName + ")");
        this.bufferedWriter = bufferedWriter;
        this.delay = messageDelay;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            synchronized (this.wait) {
                if ((!this.handleLowPriority || this.lowPriorityQueue.isEmpty()) && this.highPriorityQueue.isEmpty()) {
                    try {
                        this.wait.wait();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
            String message = this.highPriorityQueue.poll();
            if (message == null && this.handleLowPriority) {
                message = this.lowPriorityQueue.poll();
            }
            if (message == null) {
                continue;
            }
            try {
                this.bufferedWriter.write(message + "\r\n");
                this.bufferedWriter.flush();
            } catch (final IOException ignored) {
            }

            try {
                Thread.sleep(this.delay);
            } catch (final InterruptedException e) {
                break;
            }
        }
        try {
            this.bufferedWriter.write("QUIT :" + this.quitReason + "\r\n");
            this.bufferedWriter.flush();
            this.bufferedWriter.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    void queueMessage(String message, boolean highPriority) {
        (highPriority ? this.highPriorityQueue : this.lowPriorityQueue).add(message);
        if (highPriority || this.handleLowPriority) {
            synchronized (this.wait) {
                this.wait.notify();
            }
        }
    }

    void readyForLowPriority() {
        this.handleLowPriority = true;
    }

    void setMessageDelay(int delay) {
        this.delay = delay;
    }

    void shutdown(String message) {
        this.quitReason = message;
        this.interrupt();
    }
}