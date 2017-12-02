package org.kitteh.irc.client.library.command;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.kitteh.irc.client.library.feature.defaultmessage.SimpleDefaultMessageMap;
import org.mockito.Mockito;

/**
 * Tests the KickCommand
 */
public class KickCommandTest {
    private static final String CHANNEL = "#targetchannel";
    private static final String USER = "targetuser";
    private static final String REASON = "bad breath";

    private Client client;
    private User user;

    /**
     * And then Kitteh said, let there be test!
     */
    @Before
    public void before() {
        this.client = Mockito.mock(Client.class);
        ServerInfo serverInfo = Mockito.mock(ServerInfo.class);
        this.user = Mockito.mock(User.class);
        Mockito.when(this.client.getServerInfo()).thenReturn(serverInfo);
        Mockito.when(serverInfo.isValidChannel(Mockito.any())).thenReturn(true);
        Mockito.when(this.client.getDefaultMessageMap()).thenReturn(new SimpleDefaultMessageMap(null));
        Mockito.when(this.client.toString()).thenReturn("CLIENT OMG");
        Mockito.when(this.user.getNick()).thenReturn(USER);
    }

    /**
     * Test with strings and no reason.
     */
    @Test
    public void noReasonStrings() {
        KickCommand command = new KickCommand(this.client, CHANNEL);
        command.target(USER);
        command.execute();

        Mockito.verify(this.client).sendRawLine("KICK " + CHANNEL + ' ' + USER);
    }

    /**
     * Test with strings and no reason after removal.
     */
    @Test
    public void noReasonAnymore() {
        KickCommand command = new KickCommand(this.client, CHANNEL);
        command.reason(REASON);
        command.target(USER);
        command.reason(null);
        command.execute();

        Mockito.verify(this.client).sendRawLine("KICK " + CHANNEL + ' ' + USER);
    }

    /**
     * Test with elements and a reason.
     */
    @Test
    public void reasonElements() {
        Mockito.when(this.user.getClient()).thenReturn(this.client);

        KickCommand command = new KickCommand(this.client, CHANNEL);
        command.target(this.user);
        command.reason(REASON);
        command.execute();

        Mockito.verify(this.client).sendRawLine("KICK " + CHANNEL + ' ' + USER + " :" + REASON);
    }

    /**
     * Tests a targetless execution.
     */
    @Test(expected = IllegalStateException.class)
    public void noTarget() {
        KickCommand command = new KickCommand(this.client, CHANNEL);
        command.execute();
    }

    /**
     * Tests a wrong-Client attempt.
     */
    @Test(expected = IllegalArgumentException.class)
    public void wrongClientUser() {
        Mockito.when(this.user.getClient()).thenReturn(Mockito.mock(Client.class));

        KickCommand command = new KickCommand(this.client, CHANNEL);
        command.target(this.user);
    }

    /**
     * Confirms toString fires, has info in it.
     */
    @Test
    public void toStringer() {
        KickCommand command = new KickCommand(this.client, CHANNEL);

        Assert.assertTrue(command.toString().contains(CHANNEL));
    }
}
