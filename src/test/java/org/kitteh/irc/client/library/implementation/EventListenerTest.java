package org.kitteh.irc.client.library.implementation;

import org.junit.Before;
import org.junit.Test;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.event.client.ClientConnectedEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.CaseMapping;
import org.kitteh.irc.client.library.util.StringUtil;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Tests the EventListener.
 */
public class EventListenerTest {
    private InternalClient client;
    private ActorProvider actorProvider;
    private ManagerEvent eventManager;
    private Listener<Exception> exceptionListener;
    private IRCServerInfo serverInfo;

    /**
     * And then Kitteh said, let there be test!
     */
    @Before
    public void before() {
        this.client = Mockito.mock(InternalClient.class);
        this.actorProvider = new ActorProvider(this.client);
        this.eventManager = Mockito.spy(new ManagerEvent(this.client));
        this.eventManager.registerEventListener(new EventListener(this.client));
        this.exceptionListener = Mockito.mock(Listener.class);
        this.serverInfo = Mockito.mock(IRCServerInfo.class);
        Mockito.when(this.client.getServerInfo()).thenReturn(this.serverInfo);
        Mockito.when(this.client.getEventManager()).thenReturn(this.eventManager);
        Mockito.when(this.client.getExceptionListener()).thenReturn(this.exceptionListener);
        Mockito.when(this.serverInfo.getCaseMapping()).thenReturn(CaseMapping.ASCII);
        Mockito.when(this.client.getISupportManager()).thenReturn(new ManagerISupport(this.client));
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
        final ActorProvider.IRCActor actor = this.actorProvider.getActor(actorName);

        if (split.length <= index) {
            throw new KittehServerMessageException(line, "Server sent a message without a command");
        }

        final String commandString = split[index++];

        final List<String> args = this.handleArgs(split, index);

        final IRCServerMessage serverMessage = new IRCServerMessage(line, new LinkedList<>());

        try {
            int numeric = Integer.parseInt(commandString);
            this.eventManager.callEvent(new ClientReceiveNumericEvent(this.client, serverMessage, actor.snapshot(), commandString, numeric, args));
        } catch (NumberFormatException exception) {
            this.eventManager.callEvent(new ClientReceiveCommandEvent(this.client, serverMessage, actor.snapshot(), commandString, args));
        }
    }

    private List<String> handleArgs(@Nonnull String[] split, int start) {
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
        return o -> (o != null) && clazz.isAssignableFrom(o.getClass()) && ((message == null) ? (((Exception) o).getMessage() == null) : ((Exception) o).getMessage().contains(message));
    }

    private ArgumentMatcher<ISupportParameter> iSupportParameter(@Nonnull String name) {
        return o -> (o != null) && ((ISupportParameter) o).getName().equals(name);
    }

    private <T> ArgumentMatcher<T> match(Class<T> clazz, Function<T, Boolean>... functions) {
        return o -> {
            if ((o == null) || !clazz.isAssignableFrom(o.getClass())) {
                return false;
            }
            for (Function<T, Boolean> function : functions) {
                if (!function.apply((T) o)) {
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
        Mockito.verify(this.exceptionListener, Mockito.times(1)).queue(Mockito.argThat(this.exception(KittehServerMessageException.class, "Nickname unconfirmed.")));
    }

    /**
     * Tests numeric 4.
     */
    @Test
    public void test4Version() {
        this.fireLine(":irc.network 004 Kitteh irc.network kittydis-1.3.3.7-dev DQRSZagiloswz CFILPQTbcefgijklmnopqrstvz bkloveqjfI");
        Mockito.verify(this.client, Mockito.times(1)).resetServerInfo();
        Mockito.verify(this.serverInfo, Mockito.times(1)).setAddress("irc.network");
        Mockito.verify(this.serverInfo, Mockito.times(1)).setVersion("kittydis-1.3.3.7-dev");
        Mockito.verify(this.client, Mockito.times(1)).startSending();
        Mockito.verify(this.eventManager, Mockito.times(1)).callEvent(Mockito.argThat(this.match(ClientConnectedEvent.class, event -> event.getServer().getName().equals("irc.network") && event.getServerInfo().equals(this.serverInfo))));
    }

    /**
     * Tests numeric 4 without version.
     */
    @Test
    public void test4VersionNoVersion() {
        this.fireLine(":irc.network 004 Kitteh irc.network");
        Mockito.verify(this.client, Mockito.times(1)).resetServerInfo();
        Mockito.verify(this.serverInfo, Mockito.times(1)).setAddress("irc.network");
        Mockito.verify(this.serverInfo, Mockito.times(0)).setVersion(Mockito.anyString());
        Mockito.verify(this.client, Mockito.times(1)).startSending();
        Mockito.verify(this.eventManager, Mockito.times(1)).callEvent(Mockito.argThat(this.match(ClientConnectedEvent.class)));
        Mockito.verify(this.exceptionListener, Mockito.times(1)).queue(Mockito.argThat(this.exception(KittehServerMessageException.class, "Server version missing.")));
    }


    /**
     * Tests numeric 4 without address or version.
     */
    @Test
    public void test4VersionNoAddressOrVersion() {
        this.fireLine(":irc.network 004 Kitteh");
        Mockito.verify(this.client, Mockito.times(1)).resetServerInfo();
        Mockito.verify(this.serverInfo, Mockito.times(0)).setAddress(Mockito.anyString());
        Mockito.verify(this.serverInfo, Mockito.times(0)).setVersion(Mockito.anyString());
        Mockito.verify(this.client, Mockito.times(1)).startSending();
        Mockito.verify(this.eventManager, Mockito.times(1)).callEvent(Mockito.argThat(this.match(ClientConnectedEvent.class)));
        Mockito.verify(this.exceptionListener, Mockito.times(1)).queue(Mockito.argThat(this.exception(KittehServerMessageException.class, "Server address and version missing.")));
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
    public void testMOTD() {
        this.fireLine(":irc.network 375 Kitteh :- irc.network Message of the Day -");
        this.fireLine(":irc.network 372 Kitteh :-   Hello                         ");
        this.fireLine(":irc.network 372 Kitteh");
        this.fireLine(":irc.network 376 Kitteh :End of /MOTD command.             ");
        Mockito.verify(this.serverInfo, Mockito.times(1)).setMOTD(Mockito.argThat(o -> o != null && ((List<String>) o).size() == 1 && ((List<String>) o).get(0).contains("Hello")));
        Mockito.verify(this.exceptionListener, Mockito.times(1)).queue(Mockito.argThat(this.exception(KittehServerMessageException.class, "MOTD message of incorrect length")));
    }
}
