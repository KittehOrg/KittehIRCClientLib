package org.kitteh.irc.client.library.defaults.feature;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.DefaultUser;
import org.kitteh.irc.client.library.defaults.listener.DefaultChgHostListener;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.user.UserHostnameChangeEvent;
import org.kitteh.irc.client.library.event.user.UserUserStringChangeEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.ActorTracker;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.util.Listener;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Tests CHGHOST support.
 */
public class ChghostTest {
    /**
     * Tests invalid actor.
     */
    @Test
    public void testChghostWithInvalidActorThrowsException() {
        List<Exception> exceptions = new LinkedList<>();
        DefaultChgHostListener sut = this.getEventListener(exceptions);
        final Client clientMock = Mockito.mock(Client.class);
        final Actor actorMock = Mockito.mock(Actor.class);
        Mockito.when(actorMock.getClient()).thenReturn(clientMock);
        sut.chghost(new ClientReceiveCommandEvent(clientMock, Mockito.mock(ServerMessage.class), actorMock, "CHGHOST", Arrays.asList("foo", "bar")));
        Assert.assertEquals("No exception fired", 1, exceptions.size());
        Assert.assertEquals("Wrong exception type", KittehServerMessageException.class, exceptions.get(0).getClass());
        Assert.assertThat("Wrong exception fired", exceptions.get(0).getMessage(), CoreMatchers.containsString("Invalid actor for CHGHOST message"));
    }

    /**
     * Tests parameter overload.
     */
    @Test
    public void testChghostWithTooManyParameters() {
        List<Exception> exceptions = new LinkedList<>();
        DefaultChgHostListener sut = this.getEventListener(exceptions);
        final Client clientMock = Mockito.mock(Client.class);
        final Actor actorMock = Mockito.mock(User.class);
        Mockito.when(actorMock.getClient()).thenReturn(clientMock);
        sut.chghost(new ClientReceiveCommandEvent(clientMock, Mockito.mock(ServerMessage.class), actorMock, "CHGHOST", Arrays.asList("foo", "bar", "kitten")));
        Assert.assertEquals("No exception fired", 1, exceptions.size());
        Assert.assertEquals("Wrong exception type", KittehServerMessageException.class, exceptions.get(0).getClass());
        Assert.assertThat("Wrong exception fired", exceptions.get(0).getMessage(), CoreMatchers.containsString("Invalid number of parameters for CHGHOST message"));
    }

    /**
     * Tests parameter underload.
     */
    @Test
    public void testChghostWithTooFewParameters() {
        List<Exception> exceptions = new LinkedList<>();
        DefaultChgHostListener sut = this.getEventListener(exceptions);
        final Client clientMock = Mockito.mock(Client.class);
        final Actor actorMock = Mockito.mock(User.class);
        Mockito.when(actorMock.getClient()).thenReturn(clientMock);
        sut.chghost(new ClientReceiveCommandEvent(clientMock, Mockito.mock(ServerMessage.class), actorMock, "CHGHOST", Collections.singletonList("foo")));
        Assert.assertEquals("No exception fired", 1, exceptions.size());
        Assert.assertEquals("Wrong exception type", KittehServerMessageException.class, exceptions.get(0).getClass());
        Assert.assertThat("Wrong exception fired", exceptions.get(0).getMessage(), CoreMatchers.containsString("Invalid number of parameters for CHGHOST message"));
    }

    /**
     * Tests hostname update.
     */
    @Test
    public void testChghostCallsActorTrackerToUpdateHostname() {
        final Client.WithManagement internalClient = Mockito.mock(Client.WithManagement.class);
        Mockito.when(internalClient.getEventManager()).thenReturn(Mockito.mock(EventManager.class));

        final ActorTracker actorProviderMock = this.testChghostWithMockUserAndParameters(internalClient, Arrays.asList("~meow", "test.kitteh.org"));
        Mockito.verify(actorProviderMock).trackUserHostnameChange("Kitteh", "test.kitteh.org");
    }

    /**
     * Tests userstring update.
     */
    @Test
    public void testChghostCallsActorTrackerToUpdateUserString() {
        final Client.WithManagement internalClient = Mockito.mock(Client.WithManagement.class);
        Mockito.when(internalClient.getEventManager()).thenReturn(Mockito.mock(EventManager.class));

        final ActorTracker actorProviderMock = this.testChghostWithMockUserAndParameters(internalClient, Arrays.asList("~purr", "kitteh.org"));
        Mockito.verify(actorProviderMock).trackUserUserStringChange("Kitteh", "~purr");
    }

    /**
     * Tests simultaneous update.
     */
    @Test
    public void testChghostCallsActorTrackerToUpdateUserStringAndHostnameAtOnce() {
        final Client.WithManagement internalClient = Mockito.mock(Client.WithManagement.class);
        Mockito.when(internalClient.getEventManager()).thenReturn(Mockito.mock(EventManager.class));

        final ActorTracker actorProviderMock = this.testChghostWithMockUserAndParameters(internalClient, Arrays.asList("~purr", "test.kitteh.org"));
        Mockito.verify(actorProviderMock).trackUserUserStringChange("Kitteh", "~purr");
        Mockito.verify(actorProviderMock).trackUserHostnameChange("Kitteh", "test.kitteh.org");
    }

    /**
     * Tests event firing.
     */
    @Test
    public void testChghostFiresEventsAsExpected() {
        final Client.WithManagement internalClient = Mockito.mock(Client.WithManagement.class);
        final EventManager eventManager = Mockito.mock(EventManager.class);
        Mockito.when(internalClient.getEventManager()).thenReturn(eventManager);

        this.testChghostWithMockUserAndParameters(internalClient, Arrays.asList("~purr", "test.kitteh.org"));
        Mockito.verify(eventManager, Mockito.times(2)).callEvent(Mockito.argThat(
                o -> (o instanceof UserHostnameChangeEvent) || (o instanceof UserUserStringChangeEvent)
        ));
    }

    private ActorTracker testChghostWithMockUserAndParameters(Client.WithManagement internalClient, List<String> list) {
        final User userMock = Mockito.mock(User.class);
        Mockito.when(userMock.getNick()).thenReturn("Kitteh");
        Mockito.when(userMock.getHost()).thenReturn("kitteh.org");
        Mockito.when(userMock.getUserString()).thenReturn("~meow");
        final ActorTracker actorProviderMock = Mockito.mock(ActorTracker.class);
        Mockito.when(internalClient.getActorTracker()).thenReturn(actorProviderMock);

        final DefaultUser snapshotMock = Mockito.mock(DefaultUser.class);
        Mockito.when(actorProviderMock.getTrackedUser("Kitteh")).thenReturn(Optional.of(userMock));
        Mockito.when(userMock.getClient()).thenReturn(internalClient);

        DefaultChgHostListener sut = new DefaultChgHostListener(internalClient);

        ClientReceiveCommandEvent ev = new ClientReceiveCommandEvent(internalClient, Mockito.mock(ServerMessage.class), userMock, "CHGHOST", list);
        sut.chghost(ev);
        return actorProviderMock;
    }

    private DefaultChgHostListener getEventListener(List<Exception> exceptionList) {
        return this.getEventListener(null, exceptionList);
    }

    private DefaultChgHostListener getEventListener(ActorTracker tracker, List<Exception> exceptionList) {
        final Client.WithManagement client = Mockito.mock(Client.WithManagement.class);
        final Listener<Exception> exceptionListener = Mockito.mock(Listener.class);
        Mockito.when(client.getExceptionListener()).thenReturn(exceptionListener);
        Mockito.doAnswer(invocationOnMock -> exceptionList.add((Exception) invocationOnMock.getArguments()[0])).when(exceptionListener).queue(Mockito.any());
        return new DefaultChgHostListener(client);
    }
}
