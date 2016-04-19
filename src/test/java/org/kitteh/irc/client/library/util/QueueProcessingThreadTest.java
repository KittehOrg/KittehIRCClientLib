package org.kitteh.irc.client.library.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class QueueProcessingThreadTest {

    @Test
    public void testQueuing() {
        StubMessageProcessingThread sut = new StubMessageProcessingThread("TestQueueThread");
        sut.queue("a");
        sut.queue("b");
        sut.queue("c");
        sut.queue("stop");
        try {
            sut.join(50);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(sut.wasProcessed("a"));
        Assert.assertTrue(sut.wasProcessed("b"));
        Assert.assertTrue(sut.wasProcessed("c"));
    }

    class StubMessageProcessingThread extends QueueProcessingThread<String> {

        private Set<String> processed = new HashSet<>();

        /**
         * Creates a thread and starts itself.
         *
         * @param name name of the thread
         */
        protected StubMessageProcessingThread(String name) {
            super(name);
        }

        /**
         * Processes an element from the queue. Stores it
         * for verification later.
         *
         * @param element next element from the queue
         */
        @Override
        protected void processElement(String element) {
            if (element.equals("stop")) {
                this.interrupt();
                return;
            }
            this.processed.add(element);
        }

        public boolean wasProcessed(String item) {
            return this.processed.contains(item);
        }
    }
}
