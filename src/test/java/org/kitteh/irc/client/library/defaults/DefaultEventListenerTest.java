package org.kitteh.irc.client.library.defaults;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.DefaultServerMessage;
import org.kitteh.irc.client.library.defaults.feature.DefaultActorTracker;
import org.kitteh.irc.client.library.defaults.feature.DefaultEventManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultISupportManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultServerInfo;
import org.kitteh.irc.client.library.defaults.listener.DefaultListeners;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.event.client.ClientNegotiationCompleteEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.client.ISupportParameterRemovedEvent;
import org.kitteh.irc.client.library.event.user.UserNickChangeEvent;
import org.kitteh.irc.client.library.event.user.WallopsEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.ActorTracker;
import org.kitteh.irc.client.library.feature.CaseMapping;
import org.kitteh.irc.client.library.util.Listener;
import org.kitteh.irc.client.library.util.StringUtil;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Tests the EventListener.
 */
public class DefaultEventListenerTest {
    private Client.WithManagement client;
    private ActorTracker actorTracker;
    private DefaultEventManager eventManager;
    private Listener<Exception> exceptionListener;
    private DefaultServerInfo serverInfo;

    /**
     * And then Kitteh said, let there be test!
     */
    @BeforeEach
    public void before() {
        this.client = Mockito.mock(Client.WithManagement.class);
        this.actorTracker = new DefaultActorTracker(this.client);
        this.eventManager = Mockito.spy(new DefaultEventManager(this.client));
        for (DefaultListeners listener : DefaultListeners.values()) {
            this.eventManager.registerEventListener(listener.getConstructingFunction().apply(this.client));
        }
        this.exceptionListener = Mockito.mock(Listener.class);
        this.serverInfo = Mockito.mock(DefaultServerInfo.class);
        Mockito.when(this.client.getServerInfo()).thenReturn(this.serverInfo);
        Mockito.when(this.client.getEventManager()).thenReturn(this.eventManager);
        Mockito.when(this.client.getExceptionListener()).thenReturn(this.exceptionListener);
        Mockito.when(this.serverInfo.getCaseMapping()).thenReturn(CaseMapping.ASCII);
        Mockito.when(this.client.getISupportManager()).thenReturn(new DefaultISupportManager(this.client));
    }

    // BEGIN TODO - not have this be stolen from IRCClient

    private void fireLine(String line) {
        final String[] split = line.split(" ");

        int index = 0;

        if (split[index].startsWith("@")) {
            index++;
        }

        final String actorName;
        if (split[index].startsWith(":")) {
            actorName = split[index].substring(1);
            index++;
        } else {
            actorName = "";
        }
        final Actor actor = this.actorTracker.getActor(actorName);

        if (split.length <= index) {
            throw new KittehServerMessageException(new DefaultServerMessage(line, new ArrayList<>()), "Server sent a message without a command");
        }

        final String commandString = split[index++];

        final List<String> args = this.handleArgs(split, index);

        try {
            int numeric = Integer.parseInt(commandString);
            this.eventManager.callEvent(new ClientReceiveNumericEvent(this.client, new DefaultServerMessage.NumericCommand(numeric, line, new ArrayList<>()), actor, commandString, numeric, args));
        } catch (NumberFormatException exception) {
            this.eventManager.callEvent(new ClientReceiveCommandEvent(this.client, new DefaultServerMessage.StringCommand(commandString, line, new ArrayList<>()), actor, commandString, args));
        }
    }

    private List<String> handleArgs(@NonNull String[] split, int start) {
        final List<String> argsList = new LinkedList<>();

        int index = start;
        for (; index < split.length; index++) {
            if (split[index].startsWith(":")) {
                split[index] = split[index].substring(1);
                argsList.add(StringUtil.combineSplit(split, index));
                break;
            }
            argsList.add(split[index]);
        }

        return argsList;
    }

    // END TODO

    private ArgumentMatcher<Exception> exception(Class<? extends Exception> clazz, String message) {
        return o -> (o != null) && clazz.isAssignableFrom(o.getClass()) && ((message == null) ? (o.getMessage() == null) : o.getMessage().contains(message));
    }

    private ArgumentMatcher<ISupportParameter> iSupportParameter(@NonNull String name) {
        return o -> (o != null) && o.getName().equals(name);
    }

    @SafeVarargs
    private <T> ArgumentMatcher<T> match(Class<T> clazz, Function<T, Boolean>... functions) {
        return o -> {
            if ((o == null) || !clazz.isAssignableFrom(o.getClass())) {
                return false;
            }
            for (Function<T, Boolean> function : functions) {
                if (!function.apply(o)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Tests a successful welcome message.
     */
    @Test
    public void test1Welcome() {
        this.fireLine(":irc.network 001 Kitteh :Welcome to the CatNet Internet Relay Chat Network Kitteh");
        Mockito.verify(this.client, Mockito.times(1)).setCurrentNick("Kitteh");
    }

    /**
     * Tests an unsuccessful welcome message.
     */
    @Test
    public void test1WelcomeFail() {
        this.fireLine(":irc.network 001");
        Mockito.verify(this.client, Mockito.times(0)).setCurrentNick(Mockito.anyString());
        Mockito.verify(this.exceptionListener, Mockito.times(1)).queue(Mockito.argThat(this.exception(KittehServerMessageException.class, "Nickname missing from welcome message; can't confirm")));
    }

    /**
     * Tests numeric 4.
     */
    @Test
    public void test4Version() {
        this.fireLine(":irc.network 004 Kitteh irc.network kittydis-1.3.3.7-dev DQRSZagiloswz CFILPQTbcefgijklmnopqrstvz bkloveqjfI");
        Mockito.verify(this.serverInfo, Mockito.times(1)).setAddress("irc.network");
        Mockito.verify(this.serverInfo, Mockito.times(1)).setVersion("kittydis-1.3.3.7-dev");
        this.fireLine(":irc.network 005 Kitteh CHANTYPES=# :are supported by this server");
        this.fireLine(":irc.network 005 Kitteh NETWORK=Kitteh :are supported by this server");
        Mockito.verify(this.client, Mockito.times(0)).startSending();
        Mockito.verify(this.eventManager, Mockito.times(0)).callEvent(Mockito.argThat(this.match(ClientNegotiationCompleteEvent.class)));
        this.fireLine(":irc.network 251 Kitteh :There are 1 users");
        this.fireLine(":irc.network 422 Kitteh :MOTD File is missing");
        Mockito.verify(this.client, Mockito.times(1)).startSending();
        Mockito.verify(this.eventManager, Mockito.times(1)).callEvent(Mockito.argThat(this.match(ClientNegotiationCompleteEvent.class, event -> "irc.network".equals(event.getServer().getName()) && event.getServerInfo().equals(this.serverInfo))));
    }

    /**
     * Tests numeric 4 without version.
     */
    @Test
    public void test4VersionNoVersion() {
        this.fireLine(":irc.network 004 Kitteh irc.network");
        this.fireLine(":irc.network 422 Kitteh :MOTD File is missing");
        Mockito.verify(this.serverInfo, Mockito.times(1)).setAddress("irc.network");
        Mockito.verify(this.serverInfo, Mockito.times(0)).setVersion(Mockito.anyString());
        Mockito.verify(this.client, Mockito.times(1)).startSending();
        Mockito.verify(this.eventManager, Mockito.times(1)).callEvent(Mockito.argThat(this.match(ClientNegotiationCompleteEvent.class)));
        Mockito.verify(this.exceptionListener, Mockito.times(1)).queue(Mockito.argThat(this.exception(KittehServerMessageException.class, "Server version and user modes missing")));
    }

    /**
     * Tests numeric 4 without address or version.
     */
    @Test
    public void test4VersionNoAddressOrVersion() {
        this.fireLine(":irc.network 004 Kitteh");
        this.fireLine(":irc.network 422 Kitteh :MOTD File is missing");
        Mockito.verify(this.serverInfo, Mockito.times(0)).setAddress(Mockito.anyString());
        Mockito.verify(this.serverInfo, Mockito.times(0)).setVersion(Mockito.anyString());
        Mockito.verify(this.client, Mockito.times(1)).startSending();
        Mockito.verify(this.eventManager, Mockito.times(1)).callEvent(Mockito.argThat(this.match(ClientNegotiationCompleteEvent.class)));
        Mockito.verify(this.exceptionListener, Mockito.times(1)).queue(Mockito.argThat(this.exception(KittehServerMessageException.class, "Server address, version, and user modes missing")));
    }

    /**
     * Tests that a reconnect between 004 and the end of 005 doesn't complete
     * negotiation on the new connection's first line.
     */
    @Test
    public void test4VersionThenReconnect() {
        this.fireLine(":irc.network 004 Kitteh irc.network kittydis-1.3.3.7-dev DQRSZagiloswz CFILPQTbcefgijklmnopqrstvz bkloveqjfI");
        DefaultServerInfo newServerInfo = Mockito.mock(DefaultServerInfo.class);
        Mockito.when(newServerInfo.getCaseMapping()).thenReturn(CaseMapping.ASCII);
        Mockito.when(this.client.getServerInfo()).thenReturn(newServerInfo);
        this.fireLine(":irc.network NOTICE * :*** Looking up your hostname...");
        Mockito.verify(this.client, Mockito.times(0)).startSending();
        Mockito.verify(this.eventManager, Mockito.times(0)).callEvent(Mockito.argThat(this.match(ClientNegotiationCompleteEvent.class)));
        this.fireLine(":irc.network 004 Kitteh irc.network kittydis-1.3.3.7-dev DQRSZagiloswz CFILPQTbcefgijklmnopqrstvz bkloveqjfI");
        this.fireLine(":irc.network 422 Kitteh :MOTD File is missing");
        Mockito.verify(this.client, Mockito.times(1)).startSending();
        Mockito.verify(this.eventManager, Mockito.times(1)).callEvent(Mockito.argThat(this.match(ClientNegotiationCompleteEvent.class, event -> event.getServerInfo() == newServerInfo)));
    }

    @Test
    public void test5ISUPPORT() {
        this.fireLine(":irc.network 005 Kitteh SAFELIST ELIST=CTU CHANTYPES=# EXCEPTS INVEX");
        Mockito.verify(this.serverInfo, Mockito.times(1)).addISupportParameter(Mockito.argThat(this.iSupportParameter("SAFELIST")));
        Mockito.verify(this.serverInfo, Mockito.times(1)).addISupportParameter(Mockito.argThat(this.iSupportParameter("ELIST")));
        Mockito.verify(this.serverInfo, Mockito.times(1)).addISupportParameter(Mockito.argThat(this.iSupportParameter("CHANTYPES")));
        Mockito.verify(this.serverInfo, Mockito.times(1)).addISupportParameter(Mockito.argThat(this.iSupportParameter("EXCEPTS")));
        Mockito.verify(this.serverInfo, Mockito.times(1)).addISupportParameter(Mockito.argThat(this.iSupportParameter("INVEX")));
        Mockito.verify(this.serverInfo, Mockito.times(5)).addISupportParameter(Mockito.any());
    }

    @Test
    public void test5ISUPPORTLonger() {
        this.fireLine(":irc.network 005 Kitteh SAFELIST ELIST=CTU CHANTYPES=# EXCEPTS INVEX :are supported by this server");
        Mockito.verify(this.serverInfo, Mockito.times(1)).addISupportParameter(Mockito.argThat(this.iSupportParameter("SAFELIST")));
        Mockito.verify(this.serverInfo, Mockito.times(1)).addISupportParameter(Mockito.argThat(this.iSupportParameter("ELIST")));
        Mockito.verify(this.serverInfo, Mockito.times(1)).addISupportParameter(Mockito.argThat(this.iSupportParameter("CHANTYPES")));
        Mockito.verify(this.serverInfo, Mockito.times(1)).addISupportParameter(Mockito.argThat(this.iSupportParameter("EXCEPTS")));
        Mockito.verify(this.serverInfo, Mockito.times(1)).addISupportParameter(Mockito.argThat(this.iSupportParameter("INVEX")));
        Mockito.verify(this.serverInfo, Mockito.times(5)).addISupportParameter(Mockito.any());
    }

    @Test
    public void test5ISUPPORTNegation() {
        DefaultServerInfo realServerInfo = new DefaultServerInfo(this.client);
        Mockito.when(this.client.getServerInfo()).thenReturn(realServerInfo);
        List<ChannelUserMode> defaultModes = realServerInfo.getChannelUserModes();
        this.fireLine(":irc.network 005 Kitteh EXCEPTS PREFIX=(qov)~@+ :are supported by this server");
        Assertions.assertTrue(realServerInfo.getISupportParameter("EXCEPTS").isPresent());
        Assertions.assertEquals(3, realServerInfo.getChannelUserModes().size());

        this.fireLine(":irc.network 005 Kitteh -EXCEPTS -PREFIX -NEVERSENT :are supported by this server");
        Assertions.assertFalse(realServerInfo.getISupportParameter("EXCEPTS").isPresent());
        Assertions.assertFalse(realServerInfo.getISupportParameter("-EXCEPTS").isPresent());
        Assertions.assertEquals(defaultModes.size(), realServerInfo.getChannelUserModes().size());
        Assertions.assertTrue(realServerInfo.getISupportParameters().isEmpty());
        Mockito.verify(this.eventManager, Mockito.times(1)).callEvent(Mockito.argThat(this.match(ISupportParameterRemovedEvent.class, event -> "EXCEPTS".equals(event.getParameter().getName()))));
        Mockito.verify(this.eventManager, Mockito.times(1)).callEvent(Mockito.argThat(this.match(ISupportParameterRemovedEvent.class, event -> "PREFIX".equals(event.getParameter().getName()))));
        Mockito.verify(this.eventManager, Mockito.times(2)).callEvent(Mockito.argThat(this.match(ISupportParameterRemovedEvent.class)));
        Mockito.verify(this.exceptionListener, Mockito.never()).queue(Mockito.any());
    }

    @Test
    public void test5ISUPPORTInvalidNegation() {
        this.fireLine(":irc.network 005 Kitteh -EXCEPTS=1 - INVEX :are supported by this server");
        Mockito.verify(this.serverInfo, Mockito.never()).removeISupportParameter(Mockito.anyString());
        Mockito.verify(this.serverInfo, Mockito.times(1)).addISupportParameter(Mockito.argThat(this.iSupportParameter("INVEX")));
        Mockito.verify(this.serverInfo, Mockito.times(1)).addISupportParameter(Mockito.any());
        Mockito.verify(this.exceptionListener, Mockito.times(2)).queue(Mockito.argThat(this.exception(KittehServerMessageException.class, "Invalid ISUPPORT negation")));
    }

    @Test
    public void testMOTD() {
        this.fireLine(":irc.network 375 Kitteh :- irc.network Message of the Day -");
        this.fireLine(":irc.network 372 Kitteh :-   Hello                         ");
        this.fireLine(":irc.network 372 Kitteh");
        this.fireLine(":irc.network 376 Kitteh :End of /MOTD command.             ");
        Mockito.verify(this.serverInfo, Mockito.times(1)).setMotd(Mockito.argThat(o -> (o != null) && (o.size() == 1) && o.getFirst().contains("Hello")));
        Mockito.verify(this.exceptionListener, Mockito.times(1)).queue(Mockito.argThat(this.exception(KittehServerMessageException.class, "MOTD message too short")));
    }

    @Test
    public void testWALLOPS() {
        this.fireLine(":irc.network WALLOPS :Meow meow");
        Mockito.verify(this.eventManager, Mockito.times(1)).callEvent(Mockito.argThat(this.match(WallopsEvent.class))); // TODO test message and sender
    }

    @Test
    public void testWALLOPSFail() {
        this.fireLine(":irc.network WALLOPS");
        Mockito.verify(this.exceptionListener, Mockito.times(1)).queue(Mockito.argThat(this.exception(KittehServerMessageException.class, "WALLOPS message too short")));
    }

    private void setUpSelf(boolean tracked) {
        Mockito.when(this.client.getActorTracker()).thenReturn(this.actorTracker);
        Mockito.when(this.client.getNick()).thenReturn("Kitteh");
        if (tracked) {
            this.actorTracker.trackUser((User) this.actorTracker.getActor("Kitteh!kitteh@meow.test"));
        }
    }

    /**
     * Tests a bare own-nick source resolving to the tracked self user, as
     * UnrealIRCd and Solanum send for NICK and user MODE.
     */
    @Test
    public void testBareSelfSourceIsUser() {
        this.setUpSelf(true);
        Actor actor = this.actorTracker.getActor("kitteh");
        Assertions.assertInstanceOf(User.class, actor);
        Assertions.assertEquals("meow.test", ((User) actor).getHost());
    }

    /**
     * Tests a bare nick that isn't our own staying a plain actor.
     */
    @Test
    public void testBareOtherSourceIsNotUser() {
        this.setUpSelf(true);
        this.actorTracker.trackUser((User) this.actorTracker.getActor("Other!other@meow.test"));
        Assertions.assertFalse(this.actorTracker.getActor("Other") instanceof User);
    }

    /**
     * Tests own NICK change with a bare-nick source while tracked (UnrealIRCd).
     */
    @Test
    public void testNickSelfBareSourceTracked() {
        this.setUpSelf(true);
        this.fireLine(":Kitteh NICK :Meow");
        Mockito.verify(this.client, Mockito.times(1)).setCurrentNick("Meow");
        Mockito.verify(this.eventManager, Mockito.times(1)).callEvent(Mockito.argThat(this.match(UserNickChangeEvent.class,
                e -> e.getOldUser().getNick().equals("Kitteh"), e -> e.getNewUser().getNick().equals("Meow"))));
        Assertions.assertTrue(this.actorTracker.getTrackedUser("Meow").isPresent());
        Mockito.verify(this.exceptionListener, Mockito.never()).queue(Mockito.any());
    }

    /**
     * Tests own NICK change with a bare-nick source while not in any channel (UnrealIRCd).
     */
    @Test
    public void testNickSelfBareSourceUntracked() {
        this.setUpSelf(false);
        this.fireLine(":Kitteh NICK :Meow");
        Mockito.verify(this.client, Mockito.times(1)).setCurrentNick("Meow");
        Mockito.verify(this.exceptionListener, Mockito.never()).queue(Mockito.any());
    }

    /**
     * Tests NICK from a bare nick that isn't our own still failing.
     */
    @Test
    public void testNickOtherBareSource() {
        this.setUpSelf(true);
        this.fireLine(":Other NICK :Meow");
        Mockito.verify(this.client, Mockito.never()).setCurrentNick(Mockito.anyString());
        Mockito.verify(this.exceptionListener, Mockito.times(1)).queue(Mockito.argThat(this.exception(KittehServerMessageException.class, "NICK message sent for non-user")));
    }
}
