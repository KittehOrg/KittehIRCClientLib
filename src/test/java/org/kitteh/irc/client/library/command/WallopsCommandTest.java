package org.kitteh.irc.client.library.command;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.mockito.Mockito;

/**
 * Tests the WallopsCommand.
 */
public class WallopsCommandTest {
    private static final String MESSAGE = "Meow meow";

    /**
     * Test with strings and no reason.
     */
    @Test
    public void testValid() {
        Client client = Mockito.mock(Client.class);

        WallopsCommand command = new WallopsCommand(client);
        command.message(MESSAGE);
        command.execute();

        Mockito.verify(client).sendRawLine("WALLOPS :" + MESSAGE);
    }

    /**
     * Test with strings and no reason after removal.
     */
    @Test
    public void toStringObscured() {
        Client client = Mockito.mock(Client.class);

        WallopsCommand command = new WallopsCommand(client);
        command.message(MESSAGE);

        Assert.assertTrue("toString missing message", command.toString().contains(MESSAGE));
    }

    /**
     * Tests a messageless execution.
     */
    @Test(expected = IllegalStateException.class)
    public void noUser() {
        Client client = Mockito.mock(Client.class);

        WallopsCommand command = new WallopsCommand(client);
        command.execute();
    }
}
