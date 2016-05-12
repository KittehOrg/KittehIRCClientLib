package org.kitteh.irc.client.library.command;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.mockito.Mockito;

/**
 * Tests TopicCommand.
 */
public class WhoisCommandTest {
    private static final String TARGET = "Kitteh";
    private static final String SERVER = "irc.kitteh.org";

    /**
     * Tests a simple WHOIS.
     */
    @Test
    public void testNoServer() {
        Client client = Mockito.mock(Client.class);

        WhoisCommand whoisCommand = new WhoisCommand(client);
        whoisCommand.target(TARGET);
        whoisCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("WHOIS " + TARGET);
    }

    /**
     * Tests a simple mistake.
     */
    @Test(expected = IllegalStateException.class)
    public void testNoNothing() {
        Client client = Mockito.mock(Client.class);

        WhoisCommand whoisCommand = new WhoisCommand(client);
        whoisCommand.execute();
    }

    /**
     * Tests a server WHOIS.
     */
    @Test
    public void testServer() {
        Client client = Mockito.mock(Client.class);

        WhoisCommand whoisCommand = new WhoisCommand(client);
        whoisCommand.target(TARGET);
        whoisCommand.server(SERVER);
        whoisCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("WHOIS " + SERVER + ' ' + TARGET);
    }

    /**
     * Tests a WHOIS without server after setting server.
     */
    @Test
    public void testNoServerAnymore() {
        Client client = Mockito.mock(Client.class);

        WhoisCommand whoisCommand = new WhoisCommand(client);
        whoisCommand.target(TARGET);
        whoisCommand.server(SERVER);
        whoisCommand.server(null);
        whoisCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("WHOIS " + TARGET);
    }

    /**
     * Tests a simple toString.
     */
    @Test
    public void testToString() {
        Client client = Mockito.mock(Client.class);

        WhoisCommand whoisCommand = new WhoisCommand(client);
        whoisCommand.target(TARGET);

        Assert.assertTrue(whoisCommand.toString().contains(TARGET));
    }
}
