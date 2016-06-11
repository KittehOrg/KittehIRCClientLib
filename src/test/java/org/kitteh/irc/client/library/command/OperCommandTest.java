package org.kitteh.irc.client.library.command;

import org.junit.Assert;
import org.junit.Test;
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

        Assert.assertFalse("Details in toString", command.toString().contains(USER) || command.toString().contains(PASSWORD));
    }

    /**
     * Tests a userless execution.
     */
    @Test(expected = IllegalStateException.class)
    public void noUser() {
        Client client = Mockito.mock(Client.class);

        OperCommand command = new OperCommand(client);
        command.password(PASSWORD);
        command.execute();
    }

    /**
     * Tests a passless execution.
     */
    @Test(expected = IllegalStateException.class)
    public void noPass() {
        Client client = Mockito.mock(Client.class);

        OperCommand command = new OperCommand(client);
        command.user(USER);
        command.execute();
    }
}
