package org.kitteh.irc.client.library;

import org.junit.Assert;
import org.junit.Test;

/**
 * Confirm an event listener can be registered and an event fired.
 */
public class EventTest {
    private class Event {
        private boolean success = false;
    }

    @Test
    public void testEventRegistration() {
        EventManager manager = new EventManager(null);
        manager.registerEventListener(this);
        Event event = new Event();
        manager.callEvent(event);
        Assert.assertTrue("Failed to register and fire an event", event.success);
    }

    @EventHandler
    public void eventHandler(Event e) {
        e.success = true;
    }
}