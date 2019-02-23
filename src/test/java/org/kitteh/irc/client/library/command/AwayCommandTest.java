package org.kitteh.irc.client.library.command;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.mockito.Mockito;

/**
 * Tests AwayCommand.
 */
public class AwayCommandTest {
    private static final String MESSAGE = "Meow meow meow!";

    /**
     * Tests a simple away removal.
     */
    @Test
    public void testRemove() {
        Client client = Mockito.mock(Client.class);

        AwayCommand awayCommand = new AwayCommand(client);
        awayCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("AWAY");
    }

    /**
     * Tests a less simple away removal.
     */
    @Test
    public void testRemoveNull() {
        Client client = Mockito.mock(Client.class);

        AwayCommand awayCommand = new AwayCommand(client);
        awayCommand.away("meow");
        awayCommand.away(null);
        awayCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("AWAY");
    }

    /**
     * Tests a simple away removal after a message has been removed.
     */
    @Test
    public void testNoNewAnymore() {
        Client client = Mockito.mock(Client.class);

        AwayCommand awayCommand = new AwayCommand(client);
        awayCommand.away(MESSAGE);
        awayCommand.notAway();
        awayCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("AWAY");
    }

    /**
     * Tests a simple away set's toString.
     */
    @Test
    public void testToString() {
        Client client = Mockito.mock(Client.class);

        AwayCommand awayCommand = new AwayCommand(client);
        awayCommand.away(MESSAGE);
        awayCommand.execute();

        Assert.assertTrue(awayCommand.toString().contains(MESSAGE));
    }

    /**
     * Tests setting the away message.
     */
    @Test
    public void testSet() {
        Client client = Mockito.mock(Client.class);

        AwayCommand awayCommand = new AwayCommand(client);
        awayCommand.away(MESSAGE);
        awayCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("AWAY :" + MESSAGE);
    }
}
