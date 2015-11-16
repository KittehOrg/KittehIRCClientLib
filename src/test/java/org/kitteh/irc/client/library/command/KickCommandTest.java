package org.kitteh.irc.client.library.command;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * Tests the KickCommand
 */
public class KickCommandTest {
    private static final String CHANNEL = "#targetchannel";
    private static final String USER = "targetuser";
    private static final String REASON = "bad breath";

    /**
     * Test with strings and no reason.
     */
    @Test
    public void noReasonStrings() {
        Client client = Mockito.mock(Client.class);
        Mockito.when(client.getChannel(CHANNEL)).thenReturn(Optional.of(Mockito.mock(Channel.class)));

        KickCommand command = new KickCommand(client, CHANNEL);
        command.target(USER);
        command.execute();

        Mockito.verify(client).sendRawLine("KICK " + CHANNEL + ' ' + USER);
    }

    /**
     * Test with strings and no reason after removal.
     */
    @Test
    public void noReasonAnymore() {
        Client client = Mockito.mock(Client.class);
        Mockito.when(client.getChannel(CHANNEL)).thenReturn(Optional.of(Mockito.mock(Channel.class)));

        KickCommand command = new KickCommand(client, CHANNEL);
        command.reason(REASON);
        command.target(USER);
        command.reasonRemove();
        command.execute();

        Mockito.verify(client).sendRawLine("KICK " + CHANNEL + ' ' + USER);
    }

    /**
     * Test with elements and a reason.
     */
    @Test
    public void reasonElements() {
        Client client = Mockito.mock(Client.class);
        Channel channel = Mockito.mock(Channel.class);
        User user = Mockito.mock(User.class);
        Mockito.when(channel.getName()).thenReturn(CHANNEL);
        Mockito.when(channel.getClient()).thenReturn(client);
        Mockito.when(client.getChannel(CHANNEL)).thenReturn(Optional.of(Mockito.mock(Channel.class)));
        Mockito.when(user.getNick()).thenReturn(USER);
        Mockito.when(user.getClient()).thenReturn(client);

        KickCommand command = new KickCommand(client, channel);
        command.target(user);
        command.reason(REASON);
        command.execute();

        Mockito.verify(client).sendRawLine("KICK " + CHANNEL + ' ' + USER + " :" + REASON);
    }

    /**
     * Tests a targetless execution.
     */
    @Test(expected = IllegalStateException.class)
    public void noTarget() {
        Client client = Mockito.mock(Client.class);
        Mockito.when(client.getChannel(CHANNEL)).thenReturn(Optional.of(Mockito.mock(Channel.class)));

        KickCommand command = new KickCommand(client, CHANNEL);
        command.execute();
    }

    /**
     * Tests a wrong-Client attempt.
     */
    @Test(expected = IllegalArgumentException.class)
    public void wrongClientChannel() {
        Client client = Mockito.mock(Client.class);
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(channel.getClient()).thenReturn(Mockito.mock(Client.class));

        new KickCommand(client, channel);
    }

    /**
     * Tests a wrong-Client attempt.
     */
    @Test(expected = IllegalArgumentException.class)
    public void wrongClientUser() {
        Client client = Mockito.mock(Client.class);
        User user = Mockito.mock(User.class);
        Mockito.when(user.getClient()).thenReturn(Mockito.mock(Client.class));

        KickCommand command = new KickCommand(client, CHANNEL);
        command.target(user);
    }

    /**
     * Confirms toString fires, has info in it.
     */
    @Test
    public void toStringer() {
        Client client = Mockito.mock(Client.class);
        Mockito.when(client.getChannel(CHANNEL)).thenReturn(Optional.of(Mockito.mock(Channel.class)));
        Mockito.when(client.toString()).thenReturn("CLIENT OMG");

        KickCommand command = new KickCommand(client, CHANNEL);

        Assert.assertTrue(command.toString().contains(CHANNEL));
    }
}
