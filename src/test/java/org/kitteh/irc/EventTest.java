package org.kitteh.irc;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class EventTest {
    private class Event {
        private boolean success = false;
    }

    @Test
    public void testEventRegistration() {
        EventManager manager = new EventManager();
        manager.registerEventListener(this);
        Event event = new Event();
        manager.callEvent(event);
        assertTrue("Failed to register and fire an event", event.success);
    }

    @EventHandler
    public void eventHandler(Event e) {
        e.success = true;
    }
}