package org.kitteh.irc.client.library;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class RateLimitHandler {
    private class LimitQueue extends Thread {
        private BlockingQueue<String> queue = new LinkedBlockingQueue<>();

        private LimitQueue(Command command) {
            super("Kitteh IRC Client " + command + " Rate Limit Queue (" + RateLimitHandler.this.client.getName() + ")");
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                try {
                    String message = this.queue.take();
                    Thread.sleep(5000);
                    RateLimitHandler.this.client.sendRawLine(message);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        void add(String message) {
            this.queue.offer(message);
        }
    }

    private final Client client;
    private final Set<Command> limited = new HashSet<>();
    private final Map<Command, LimitQueue> queued = new HashMap<>();

    RateLimitHandler(IRCClient client) {
        this.client = client;
    }

    synchronized boolean isPendingLimitInfo(Command command) {
        return this.limited.contains(command);
    }

    synchronized void queue(Command command, String message) {
        this.limited.remove(command);
        LimitQueue queue = this.queued.get(command);
        if (queue == null) {
            queue = new LimitQueue(command);
            this.queued.put(command, queue);
        }
        queue.add(message);
    }

    synchronized void setPendingLimitInfo(Command command) {
        this.limited.add(command);
    }

    synchronized void shutdown() {
        this.queued.values().forEach(RateLimitHandler.LimitQueue::interrupt);
    }
}