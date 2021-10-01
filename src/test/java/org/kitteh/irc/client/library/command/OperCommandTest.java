package org.kitteh.irc.client.library.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.mockito.Mockito;

/**
 * Tests the OperCommand.
 */
public class OperCommandTest {
    private static final String USER = "myUser";
    private static final String PASSWORD = "myPassword";

    /**
     * Test a valid command.
     */
    @Test
    public void testValid() {
        Client client = Mockito.mock(Client.class);

        OperCommand command = new OperCommand(client);
        command.user(USER);
        command.password(PASSWORD);
        command.execute();

        Mockito.verify(client).sendRawLine("OPER " + USER + ' ' + PASSWORD);
    }

    /**
     * Test to confirm toString doesn't include user/pass.
     */
    @Test
    public void toStringObscured() {
        Client client = Mockito.mock(Client.class);

        OperCommand command = new OperCommand(client);
        command.user(USER);
        command.password(PASSWORD);

        Assertions.assertFalse(command.toString().contains(USER) || command.toString().contains(PASSWORD), "Details in toString");
    }

    /**
     * Tests a userless execution.
     */
    @Test
    public void noUser() {
        Client client = Mockito.mock(Client.class);

        OperCommand command = new OperCommand(client);
        command.password(PASSWORD);
        Assertions.assertThrows(IllegalStateException.class, command::execute);
    }

    /**
     * Tests a passless execution.
     */
    @Test
    public void noPass() {
        Client client = Mockito.mock(Client.class);

        OperCommand command = new OperCommand(client);
        command.user(USER);
        Assertions.assertThrows(IllegalStateException.class, command::execute);
    }
}
