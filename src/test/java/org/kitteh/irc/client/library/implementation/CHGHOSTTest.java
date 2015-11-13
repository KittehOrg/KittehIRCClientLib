package org.kitteh.irc.client.library.implementation;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.EventManager;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.user.UserHostnameChangeEvent;
import org.kitteh.irc.client.library.event.user.UserUserStringChangeEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests CHGHOST support.
 */
public class CHGHOSTTest {
    /**
     * Expected!
     */
    @Rule
    public final ExpectedException expectedEx = ExpectedException.none();

    /**
     * Tests invalid actor.
     */
    @Test
    public void testChghostWithInvalidActorThrowsException() {
        this.expectedEx.expect(KittehServerMessageException.class);
        this.expectedEx.expectMessage(CoreMatchers.containsString("Invalid actor for CHGHOST message"));

        EventListener sut = this.getEventListener();
        final Client clientMock = Mockito.mock(Client.class);
        final Actor actorMock = Mockito.mock(Actor.class);
        Mockito.when(actorMock.getClient()).thenReturn(clientMock);
        sut.chghost(new ClientReceiveCommandEvent(clientMock, Mockito.mock(ServerMessage.class), actorMock, "CHGHOST", Arrays.asList("foo", "bar")));
    }

    /**
     * Tests parameter overload.
     */
    @Test
    public void testChghostWithTooManyParameters() {
        this.expectedEx.expect(KittehServerMessageException.class);
        this.expectedEx.expectMessage(CoreMatchers.containsString("Invalid number of parameters for CHGHOST message"));

        this.testChghostWithParameters(Arrays.asList("foo", "bar", "kitten"));
    }

    /**
     * Tests parameter underload.
     */
    @Test
    public void testChghostWithTooFewParameters() {
        this.expectedEx.expect(KittehServerMessageException.class);
        this.expectedEx.expectMessage(CoreMatchers.containsString("Invalid number of parameters for CHGHOST message"));

        this.testChghostWithParameters(Collections.singletonList("foo"));
    }

    /**
     * Tests hostname update.
     */
    @Test
    public void testChghostCallsActorProviderToUpdateHostname() {
        final InternalClient internalClient = Mockito.mock(InternalClient.class);
        Mockito.when(internalClient.getEventManager()).thenReturn(Mockito.mock(EventManager.class));

        final ActorProvider actorProviderMock = this.testChghostWithMockUserAndParameters(internalClient, Arrays.asList("~meow", "test.kitteh.org"));
        Mockito.verify(actorProviderMock).trackUserHostnameChange("Kitteh", "test.kitteh.org");
    }

    /**
     * Tests userstring update.
     */
    @Test
    public void testChghostCallsActorProviderToUpdateUserString() {
        final InternalClient internalClient = Mockito.mock(InternalClient.class);
        Mockito.when(internalClient.getEventManager()).thenReturn(Mockito.mock(EventManager.class));

        final ActorProvider actorProviderMock = this.testChghostWithMockUserAndParameters(internalClient, Arrays.asList("~purr", "kitteh.org"));
        Mockito.verify(actorProviderMock).trackUserUserStringChange("Kitteh", "~purr");
    }

    /**
     * Tests simultaneous update.
     */
    @Test
    public void testChghostCallsActorProviderToUpdateUserStringAndHostnameAtOnce() {
        final InternalClient internalClient = Mockito.mock(InternalClient.class);
        Mockito.when(internalClient.getEventManager()).thenReturn(Mockito.mock(EventManager.class));

        final ActorProvider actorProviderMock = this.testChghostWithMockUserAndParameters(internalClient, Arrays.asList("~purr", "test.kitteh.org"));
        Mockito.verify(actorProviderMock).trackUserUserStringChange("Kitteh", "~purr");
        Mockito.verify(actorProviderMock).trackUserHostnameChange("Kitteh", "test.kitteh.org");
    }

    /**
     * Tests event firing.
     */
    @Test
    public void testChghostFiresEventsAsExpected() {
        final InternalClient internalClient = Mockito.mock(InternalClient.class);
        final EventManager eventManager = Mockito.mock(EventManager.class);
        Mockito.when(internalClient.getEventManager()).thenReturn(eventManager);

        this.testChghostWithMockUserAndParameters(internalClient, Arrays.asList("~purr", "test.kitteh.org"));
        Mockito.verify(eventManager, Mockito.times(2)).callEvent(Mockito.argThat(
                o -> (o instanceof UserHostnameChangeEvent) || (o instanceof UserUserStringChangeEvent)
        ));
    }

    private ActorProvider testChghostWithMockUserAndParameters(InternalClient internalClient, List<String> list) {
        final Client clientMock = Mockito.mock(Client.class);

        final User userMock = Mockito.mock(User.class);
        Mockito.when(userMock.getNick()).thenReturn("Kitteh");
        Mockito.when(userMock.getHost()).thenReturn("kitteh.org");
        Mockito.when(userMock.getUserString()).thenReturn("~meow");
        Mockito.when(userMock.getClient()).thenReturn(clientMock);

        final ActorProvider actorProviderMock = Mockito.mock(ActorProvider.class);
        Mockito.when(internalClient.getActorProvider()).thenReturn(actorProviderMock);

        ActorProvider.IRCUser ircUser = Mockito.mock(ActorProvider.IRCUser.class);
        Mockito.when(ircUser.getNick()).thenReturn("Kitteh");
        Mockito.when(ircUser.getName()).thenReturn("Kitteh!~meow@kitteh.org");

        final ActorProvider.IRCUserSnapshot snapshotMock = Mockito.mock(ActorProvider.IRCUserSnapshot.class);
        Mockito.when(snapshotMock.getClient()).thenReturn(internalClient);
        Mockito.when(snapshotMock.getHost()).thenReturn("kitteh.org");
        Mockito.when(snapshotMock.getNick()).thenReturn("Kitteh");
        Mockito.when(snapshotMock.getUserString()).thenReturn("~meow");

        Mockito.when(ircUser.snapshot()).thenReturn(snapshotMock);
        Mockito.when(actorProviderMock.getUser("Kitteh")).thenReturn(ircUser);
        Mockito.when(userMock.getClient()).thenReturn(clientMock);

        EventListener sut = new EventListener(internalClient);

        sut.chghost(new ClientReceiveCommandEvent(clientMock, Mockito.mock(ServerMessage.class), userMock, "CHGHOST", list));
        return actorProviderMock;
    }

    private void testChghostWithParameters(List<String> list) {
        EventListener sut = this.getEventListener();
        final Client clientMock = Mockito.mock(Client.class);
        final Actor actorMock = Mockito.mock(User.class);
        Mockito.when(actorMock.getClient()).thenReturn(clientMock);
        sut.chghost(new ClientReceiveCommandEvent(clientMock, Mockito.mock(ServerMessage.class), actorMock, "CHGHOST", list));
    }

    private EventListener getEventListener() {
        return this.getEventListener(null);
    }

    private EventListener getEventListener(ActorProvider provider) {
        final InternalClient client = Mockito.mock(InternalClient.class);
        Mockito.when(client.getActorProvider()).thenReturn(provider);
        return new EventListener(client);
    }
}
