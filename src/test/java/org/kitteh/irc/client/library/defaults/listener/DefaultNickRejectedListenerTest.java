package org.kitteh.irc.client.library.defaults.listener;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.DefaultServerMessage;
import org.kitteh.irc.client.library.defaults.feature.DefaultActorTracker;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.client.NickRejectedEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.CaseMapping;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.kitteh.irc.client.library.util.Listener;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tests the default nick rejected listener.
 */
public class DefaultNickRejectedListenerTest {
    private Client.WithManagement client;
    private DefaultNickRejectedListener listener;
    private DefaultActorTracker actorTracker;
    private Listener<Exception> exceptionListener;
    private final List<String> sentNicks = new ArrayList<>();
    private final List<NickRejectedEvent> events = new ArrayList<>();
    private String goalNick;
    private String requestedNick;
    private Consumer<NickRejectedEvent> eventHandler = event -> {
    };

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void before() {
        this.client = Mockito.mock(Client.WithManagement.class);
        this.exceptionListener = Mockito.mock(Listener.class);
        EventManager eventManager = Mockito.mock(EventManager.class);
        Mockito.doAnswer(invocation -> {
            NickRejectedEvent event = invocation.getArgument(0);
            this.events.add(event);
            this.eventHandler.accept(event);
            return null;
        }).when(eventManager).callEvent(Mockito.any(NickRejectedEvent.class));
        Mockito.when(this.client.getEventManager()).thenReturn(eventManager);
        Mockito.when(this.client.getExceptionListener()).thenReturn(this.exceptionListener);
        Mockito.when(this.client.getIntendedNick()).thenAnswer(invocation -> this.goalNick);
        Mockito.when(this.client.getRequestedNick()).thenAnswer(invocation -> this.requestedNick);
        Mockito.doAnswer(invocation -> {
            this.requestedNick = invocation.getArgument(0);
            this.sentNicks.add(this.requestedNick);
            return null;
        }).when(this.client).sendNickChange(Mockito.anyString());
        this.newConnection();
        this.actorTracker = new DefaultActorTracker(this.client);
        this.listener = new DefaultNickRejectedListener(this.client);
        this.goalNick = this.requestedNick = "Kitteh";
    }

    private void newConnection() {
        ServerInfo.WithManagement serverInfo = Mockito.mock(ServerInfo.WithManagement.class);
        Mockito.when(serverInfo.getCaseMapping()).thenReturn(CaseMapping.ASCII);
        Mockito.when(this.client.getServerInfo()).thenReturn(serverInfo);
    }

    private ClientReceiveNumericEvent numeric(int numeric, String... args) {
        String command = String.format("%03d", numeric);
        String line = ":irc.network " + command + ' ' + String.join(" ", args);
        return new ClientReceiveNumericEvent(this.client, new DefaultServerMessage.NumericCommand(numeric, line, new ArrayList<>()), this.actorTracker.getActor("irc.network"), command, numeric, List.of(args));
    }

    private void reject(int numeric) {
        this.listener.nickInUse(this.numeric(numeric, "*", this.requestedNick, "Nope"));
    }

    @Test
    public void testInUseSequence() {
        for (int i = 0; i < 4; i++) {
            this.reject(433);
        }
        Assertions.assertEquals(List.of("Kitteh`", "Kitteh1", "Kitteh2", "Kitteh3"), this.sentNicks);
        Assertions.assertEquals("Kitteh2", this.events.getLast().getAttemptedNick());
    }

    @Test
    public void testGoalEndingInDigits() {
        this.goalNick = this.requestedNick = "Bot2";
        this.reject(433);
        this.reject(433);
        Assertions.assertEquals(List.of("Bot2`", "Bot21"), this.sentNicks);
    }

    @Test
    public void testErroneousStopsAfterLimit() {
        for (int i = 0; i < 20; i++) {
            this.reject(432);
        }
        Assertions.assertEquals(10, this.sentNicks.size());
        Assertions.assertEquals("Kitteh9", this.sentNicks.getLast());
        Assertions.assertEquals(20, this.events.size());
        Mockito.verify(this.exceptionListener, Mockito.times(1)).queue(Mockito.argThat(e -> (e instanceof KittehServerMessageException) && e.getMessage().contains("no longer retrying")));
    }

    @Test
    public void testUserNickAfterLimit() {
        for (int i = 0; i < 11; i++) {
            this.reject(432);
        }
        this.eventHandler = event -> event.setNewNick("Custom");
        this.reject(432);
        Assertions.assertEquals("Custom", this.sentNicks.getLast());
        Assertions.assertEquals(11, this.sentNicks.size());
    }

    @Test
    public void testSilentTruncation() {
        this.goalNick = this.requestedNick = "LongGoalNick";
        // Server truncates to 9 characters, so every nick it sees is LongGoalN or a cut-down attempt
        for (int i = 0; i < 3; i++) {
            String seen = this.requestedNick.length() > 9 ? this.requestedNick.substring(0, 9) : this.requestedNick;
            this.listener.nickInUse(this.numeric(433, "*", seen, "Nickname is already in use"));
        }
        Assertions.assertEquals(List.of("LongGoal`", "LongGoal1", "LongGoal2"), this.sentNicks);
    }

    @Test
    public void testCollision() {
        this.reject(436);
        Assertions.assertEquals(List.of("Kitteh`"), this.sentNicks);
    }

    @Test
    public void testNoAutomaticRetryAfterWelcome() {
        this.listener.welcome(this.numeric(1, "Kitteh", "Welcome"));
        this.goalNick = this.requestedNick = "ThisNickIsWayTooLong";
        for (int i = 0; i < 3; i++) {
            this.reject(432);
        }
        Assertions.assertTrue(this.sentNicks.isEmpty());
        Assertions.assertEquals(3, this.events.size());
        Mockito.verify(this.exceptionListener, Mockito.never()).queue(Mockito.any());
    }

    @Test
    public void testUserNickAfterWelcome() {
        this.listener.welcome(this.numeric(1, "Kitteh", "Welcome"));
        this.goalNick = this.requestedNick = "Taken";
        this.eventHandler = event -> event.setNewNick("Other");
        this.reject(433);
        Assertions.assertEquals(List.of("Other"), this.sentNicks);
    }

    @Test
    public void testStateResetsOnNewConnection() {
        this.listener.welcome(this.numeric(1, "Kitteh", "Welcome"));
        this.newConnection();
        this.reject(433);
        this.reject(433);
        Assertions.assertEquals(List.of("Kitteh`", "Kitteh1"), this.sentNicks);
    }

    @Test
    public void testErroneousLongNickShortened() {
        this.goalNick = this.requestedNick = "X".repeat(40);
        for (int i = 0; i < 11; i++) {
            this.reject(432);
        }
        Assertions.assertEquals(List.of("X".repeat(24), "X".repeat(16), "X".repeat(12), "X".repeat(10), "X".repeat(9),
                "XXXXXXXX`", "XXXXXXXX1", "XXXXXXXX2", "XXXXXXXX3", "XXXXXXXX4"), this.sentNicks);
        Mockito.verify(this.exceptionListener, Mockito.times(1)).queue(Mockito.argThat(e -> e.getMessage().contains("no longer retrying")));
    }

    @Test
    public void testShortenedThenInUse() {
        this.goalNick = this.requestedNick = "LongGoalNickName";
        this.reject(432);
        this.reject(433);
        this.reject(433);
        Assertions.assertEquals(List.of("LongGoalNick", "LongGoalNic`", "LongGoalNic1"), this.sentNicks);
    }

    @Test
    public void testSanitizedAfterLimit() {
        this.goalNick = this.requestedNick = "Kitteh.Cat";
        for (int i = 0; i < 10; i++) {
            this.reject(432);
        }
        Assertions.assertEquals("Kitteh.Ca", this.sentNicks.getFirst());
        Assertions.assertEquals(10, this.sentNicks.size());
        this.reject(432);
        Assertions.assertEquals("Kitteh_Cat", this.sentNicks.getLast());
        this.reject(433);
        Assertions.assertEquals("Kitteh_Cat`", this.sentNicks.getLast());
        // Second round also stops after the limit
        for (int i = 0; i < 20; i++) {
            this.reject(432);
        }
        Assertions.assertEquals(21, this.sentNicks.size());
        Mockito.verify(this.exceptionListener, Mockito.times(1)).queue(Mockito.argThat(e -> e.getMessage().contains("no longer retrying")));
    }

    @Test
    public void testSanitize() {
        Assertions.assertEquals("Kitteh", DefaultNickRejectedListener.sanitize("Kitteh"));
        Assertions.assertEquals("[cat]`_^{|}-9", DefaultNickRejectedListener.sanitize("[cat]`_^{|}-9"));
        Assertions.assertEquals("K_t_cat", DefaultNickRejectedListener.sanitize("Kät.cat"));
        Assertions.assertEquals("_1337", DefaultNickRejectedListener.sanitize("1337"));
        Assertions.assertEquals("_-a", DefaultNickRejectedListener.sanitize("-a"));
    }
}
