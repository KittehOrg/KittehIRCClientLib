package org.kitteh.irc.client.library.command;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.mockito.InOrder;
import org.mockito.Mockito;

import javax.annotation.Nonnull;

/**
 * @see UserModeCommand
 */
public class UserModeCommandTest {
    private static final String USER = "targetuser";

    @Test
    public void testWithNoModeChanges() {
        Client clientMock = Mockito.mock(Client.class);
        Mockito.when(clientMock.getNick()).thenReturn(USER);

        UserModeCommand sut = new UserModeCommand(clientMock);
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + USER);

        Assert.assertFalse(sut.toString().isEmpty());
    }

    @Test
    public void testWithOneSimpleModeChange() {
        Client clientMock = Mockito.mock(Client.class);
        Mockito.when(clientMock.getNick()).thenReturn(USER);

        UserModeCommand sut = new UserModeCommand(clientMock);
        UserMode mode = this.getUserMode('A', clientMock);
        sut.add(true, mode);
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + USER + " +A");
    }

    @Test
    public void testWithFourSimpleModeChanges() {
        Client clientMock = Mockito.mock(Client.class);
        Mockito.when(clientMock.getNick()).thenReturn(USER);

        UserModeCommand sut = new UserModeCommand(clientMock);
        sut.add(true, this.getUserMode('A', clientMock));
        sut.add(true, this.getUserMode('B', clientMock));
        sut.add(true, this.getUserMode('C', clientMock));
        sut.add(true, this.getUserMode('D', clientMock));
        sut.execute();
        InOrder inOrder = Mockito.inOrder(clientMock, clientMock);
        inOrder.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + USER + " +ABCD");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithOneSimpleModeChangeButWrongClient() {
        Client clientMock = Mockito.mock(Client.class);
        Mockito.when(clientMock.getNick()).thenReturn(USER);

        UserModeCommand sut = new UserModeCommand(clientMock);
        UserMode mode = this.getUserMode('A', Mockito.mock(Client.class));
        sut.add(true, mode);
    }

    @Test
    public void testWithAddAndRemove() {
        Client clientMock = Mockito.mock(Client.class);
        Mockito.when(clientMock.getNick()).thenReturn(USER);

        UserModeCommand sut = new UserModeCommand(clientMock);
        UserMode modeA = this.getUserMode('A', clientMock);
        UserMode modeB = this.getUserMode('B', clientMock);
        sut.add(true, modeA);
        sut.add(false, modeB);
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + USER + " +A-B");
    }

    @Test
    public void testWithAddParameterisedAndSimpleMode() {
        Client clientMock = Mockito.mock(Client.class);
        Mockito.when(clientMock.getNick()).thenReturn(USER);

        UserModeCommand sut = new UserModeCommand(clientMock);
        UserMode modeA = this.getUserMode('A', clientMock);
        UserMode modeB = this.getUserMode('B', clientMock);
        sut.add(true, modeA);
        sut.add(true, modeB, "test");
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + USER + " +AB test");
    }

    @Test
    public void testAddModeWithParameter() {
        Client clientMock = Mockito.mock(Client.class);
        Mockito.when(clientMock.getNick()).thenReturn(USER);

        UserModeCommand sut = new UserModeCommand(clientMock);
        UserMode mode = this.getUserMode('A', clientMock);
        sut.add(true, mode, "foo");
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + USER + " +A foo");
    }

    @Test
    public void testRemoveModeWithParameter() {
        Client clientMock = Mockito.mock(Client.class);
        Mockito.when(clientMock.getNick()).thenReturn(USER);

        UserModeCommand sut = new UserModeCommand(clientMock);
        UserMode mode = this.getUserMode('A', clientMock);
        sut.add(false, mode, "foo");
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + USER + " -A foo");
    }

    private UserMode getUserMode(char c, Client client) {
        return new UserMode() {
            @Override
            public char getChar() {
                return c;
            }

            @Nonnull
            @Override
            public Client getClient() {
                return client;
            }
        };
    }
}
