package org.kitteh.irc.client.library.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
    @Test
    public void testNoNothing() {
        Client client = Mockito.mock(Client.class);

        WhoisCommand whoisCommand = new WhoisCommand(client);
        Assertions.assertThrows(IllegalStateException.class, whoisCommand::execute);
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

        Assertions.assertTrue(whoisCommand.toString().contains(TARGET));
    }
}
