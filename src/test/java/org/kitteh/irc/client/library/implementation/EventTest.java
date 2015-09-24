package org.kitteh.irc.client.library.implementation;

import net.engio.mbassy.listener.Handler;
import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.EventManager;

/**
 * Confirm an event listener can be registered and an event fired.
 */
public class EventTest {
    private class Event {
        private boolean success = false;
    }

    /**
     * Tests ability to register and fire an event.
     */
    @Test
    public void testEventRegistration() {
        FakeClient fakeClient = new FakeClient();
        EventManager manager = fakeClient.getEventManager();
        manager.registerEventListener(this);
        Event event = new Event();
        manager.callEvent(event);
        Assert.assertTrue("Failed to register and fire an event", event.success);
    }

    /**
     * A test method for listening to an event.
     *
     * @param e the test event
     */
    @Handler
    public void eventHandler(Event e) {
        e.success = true;
    }
}
