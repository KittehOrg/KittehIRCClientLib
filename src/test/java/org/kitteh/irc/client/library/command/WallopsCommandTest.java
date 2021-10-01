package org.kitteh.irc.client.library.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

        Assertions.assertTrue(command.toString().contains(MESSAGE), "toString missing message");
    }

    /**
     * Tests a messageless execution.
     */
    @Test
    public void noUser() {
        Client client = Mockito.mock(Client.class);

        WallopsCommand command = new WallopsCommand(client);
        Assertions.assertThrows(IllegalStateException.class, command::execute);
    }
}
