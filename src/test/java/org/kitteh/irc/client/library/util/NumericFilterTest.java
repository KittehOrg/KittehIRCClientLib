package org.kitteh.irc.client.library.util;

import net.engio.mbassy.listener.MessageHandler;
import net.engio.mbassy.subscription.SubscriptionContext;
import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * Tests NumericFilter.
 */
public class NumericFilterTest {
    /**
     * Tests filtering.
     *
     * @throws Exception when mad
     */
    @Test
    public void testFilter() throws Exception {
        Method goodMethod = NumericFilterTest.class.getMethod("goodMethod");
        NumericFilter.Filter filter = new NumericFilter.Filter();
        Assert.assertNotNull(filter.toString());
        SubscriptionContext context = Mockito.mock(SubscriptionContext.class);
        MessageHandler handler = Mockito.mock(MessageHandler.class);
        Mockito.when(handler.getMethod()).thenReturn(goodMethod);
        Mockito.when(context.getHandler()).thenReturn(handler);

        Client client = Mockito.mock(Client.class);
        Actor actor = Mockito.mock(Actor.class);
        Mockito.when(actor.getClient()).thenReturn(client);

        Assert.assertTrue(filter.accepts(new ClientReceiveNumericEvent(client, Mockito.mock(ServerMessage.class), actor, "1", 1, new LinkedList<>()), context));
        Assert.assertFalse(filter.accepts(new ClientReceiveNumericEvent(client, Mockito.mock(ServerMessage.class), actor, "2", 2, new LinkedList<>()), context));
    }

    /**
     * Good method.
     */
    @NumericFilter(1)
    public void goodMethod() {
    }
}
