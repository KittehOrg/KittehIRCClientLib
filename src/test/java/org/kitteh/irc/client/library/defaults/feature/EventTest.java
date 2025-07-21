package org.kitteh.irc.client.library.defaults.feature;

import net.engio.mbassy.listener.Handler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.FakeClient;
import org.kitteh.irc.client.library.feature.EventManager;

/**
 * Confirm an event listener can be registered and an event fired.
 */
public class EventTest {
    private static class Event {
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
        Assertions.assertTrue(event.success, "Failed to register and fire an event");
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
