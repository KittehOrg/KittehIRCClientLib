package org.kitteh.irc.client.library.util;

import net.engio.mbassy.listener.MessageHandler;
import net.engio.mbassy.subscription.SubscriptionContext;
import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * Tests CommandFilter.
 */
public class CommandFilterTest {
    /**
     * Tests filtering.
     *
     * @throws Exception when mad
     */
    @Test
    public void testFilter() throws Exception {
        Method goodMethod = CommandFilterTest.class.getMethod("goodMethod");
        CommandFilter.Filter filter = new CommandFilter.Filter();
        Assert.assertNotNull(filter.toString());
        SubscriptionContext context = Mockito.mock(SubscriptionContext.class);
        MessageHandler handler = Mockito.mock(MessageHandler.class);
        Mockito.when(handler.getMethod()).thenReturn(goodMethod);
        Mockito.when(context.getHandler()).thenReturn(handler);

        Client client = Mockito.mock(Client.class);
        Actor actor = Mockito.mock(Actor.class);
        Mockito.when(actor.getClient()).thenReturn(client);

        Assert.assertTrue(filter.accepts(new ClientReceiveCommandEvent(client, Mockito.mock(ServerMessage.class), actor, "meow", new LinkedList<>()), context));
        Assert.assertFalse(filter.accepts(new ClientReceiveCommandEvent(client, Mockito.mock(ServerMessage.class), actor, "mew", new LinkedList<>()), context));
    }

    /**
     * Good method.
     */
    @CommandFilter("meow")
    public void goodMethod() {
    }
}
