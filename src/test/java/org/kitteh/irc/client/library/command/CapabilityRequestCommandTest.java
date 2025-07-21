package org.kitteh.irc.client.library.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.mockito.Mockito;

/**
 * Tests the CapabilityRequestCommand class using mocks.
 *
 * @see CapabilityRequestCommand
 */
public class CapabilityRequestCommandTest {
    /**
     * Wrapping to a new message occurs after this length.
     */
    public static final int CAPABILITY_REQUEST_SOFT_LIMIT = 200;

    /**
     * Tests enabling a capability.
     */
    @Test
    public void testEnable() {
        Client ircClient = getClientMock();
        CapabilityRequestCommand command = new CapabilityRequestCommand(ircClient);

        command.enable("meow");
        command.execute();

        Mockito.verify(ircClient).sendRawLineImmediately("CAP REQ :meow ");
    }

    /**
     * Tests enabling a capability.
     */
    @Test
    public void testDisable() {
        Client ircClient = getClientMock();
        CapabilityRequestCommand command = new CapabilityRequestCommand(ircClient);

        command.disable("meow");
        command.execute();

        Mockito.verify(ircClient).sendRawLineImmediately("CAP REQ :-meow ");
    }

    /**
     * Ensure if we request more capabilities than fit in the maximum
     * message length that the request is split up.
     */
    @Test
    public void testManyCapabilitiesResultsInMultipleRequestMessages() {
        // arrange
        Client ircClientMock = getClientMock();
        CapabilityRequestCommand sut = new CapabilityRequestCommand(ircClientMock);

        // act

        // Each added capability takes " " and then the string given - 10 char total
        // We want to hit just over 200.
        for (int i = 0; i <= ((CAPABILITY_REQUEST_SOFT_LIMIT / 10) + 1); i++) {
            sut.enable("ABCDEFGHI");
        }

        sut.execute();

        // assert
        Mockito.verify(ircClientMock, Mockito.times(2)).sendRawLineImmediately(Mockito.anyString());
    }

    /**
     * Testing this method completely up to you, but I personally found
     * <a href="http://stackoverflow.com/a/12528461/5056459">http://stackoverflow.com/a/12528461/5056459</a> compelling
     */
    @Test
    public void testToString() {
        // arrange
        Client ircClientMock = getClientMock();
        Mockito.when(ircClientMock.toString()).thenReturn("testClientToString");
        CapabilityRequestCommand sut = new CapabilityRequestCommand(ircClientMock);

        // act
        sut.enable("testCapability");

        // assert
        Assertions.assertEquals("CapabilityRequestCommand (client=testClientToString, requests=[testCapability])", sut.toString());
    }

    /**
     * Gets the mock for the Client interface.
     *
     * @return Client mock.
     */
    private static Client getClientMock() {
        return Mockito.mock(Client.class);
    }
}
