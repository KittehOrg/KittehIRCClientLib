package org.kitteh.irc.client.library.command;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ChannelMode;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.kitteh.irc.client.library.element.User;
import org.mockito.InOrder;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * @see ModeCommand
 */
public class ModeCommandTest {
    @Test
    public void testWithNoModeChanges() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedChannel(clientMock, "#test"));
        sut.execute();
        Mockito.verify(clientMock, Mockito.never()).sendRawLine(Mockito.anyString());

        Assert.assertFalse(sut.toString().isEmpty());
    }

    @Test
    public void testWithNoModeChangesWithStringChannel() {
        Client clientMock = Mockito.mock(Client.class);
        Mockito.when(clientMock.getChannel("#test")).thenReturn(Optional.of(Mockito.mock(Channel.class)));

        ModeCommand sut = new ModeCommand(clientMock, "#test");
        sut.execute();
        Mockito.verify(clientMock, Mockito.never()).sendRawLine(Mockito.anyString());

        Assert.assertFalse(sut.toString().isEmpty());
    }

    @Test
    public void testWithOneSimpleModeChange() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedChannel(clientMock, "#test"));
        ChannelMode mode = this.getChannelMode('A', clientMock, ChannelMode.Type.A_MASK);
        sut.add(true, mode);
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE #test +A");
    }

    @Test
    public void testWithFourSimpleModeChanges() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedChannel(clientMock, "#test"));
        sut.add(true, this.getChannelMode('A', clientMock, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.add(true, this.getChannelMode('B', clientMock, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.add(true, this.getChannelMode('C', clientMock, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.add(true, this.getChannelMode('D', clientMock, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.execute();
        InOrder inOrder = Mockito.inOrder(clientMock, clientMock);
        inOrder.verify(clientMock, Mockito.times(1)).sendRawLine("MODE #test +ABC");
        inOrder.verify(clientMock, Mockito.times(1)).sendRawLine("MODE #test +D");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithOneSimpleModeChangeButWrongClient() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedChannel(clientMock, "#test"));
        ChannelMode mode = this.getChannelMode('A', Mockito.mock(Client.class), ChannelMode.Type.A_MASK);
        sut.add(true, mode);
    }

    @Test
    public void testWithAddAndRemove() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedChannel(clientMock, "#test"));
        ChannelMode modeA = this.getChannelMode('A', clientMock, ChannelMode.Type.A_MASK);
        ChannelMode modeB = this.getChannelMode('B', clientMock, ChannelMode.Type.A_MASK);
        sut.add(true, modeA);
        sut.add(false, modeB);
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE #test +A-B");
    }

    @Test
    public void testWithAddParameterisedAndSimpleMode() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedChannel(clientMock, "#test"));
        ChannelMode modeA = this.getChannelMode('A', clientMock, ChannelMode.Type.A_MASK);
        ChannelMode modeB = this.getChannelMode('B', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(true, modeA);
        sut.add(true, modeB, "test");
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE #test +AB test");
    }

    @Test
    public void testAddModeWithParameter() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedChannel(clientMock, "#test"));
        ChannelMode mode = this.getChannelMode('A', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(true, mode, "foo");
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE #test +A foo");
    }

    @Test
    public void testAddModeWithParameterViaUser() {
        Client clientMock = Mockito.mock(Client.class);
        User userMock = Mockito.mock(User.class);
        Mockito.when(userMock.getClient()).thenReturn(clientMock);
        Mockito.when(userMock.getNick()).thenReturn("kitteh");
        ModeCommand sut = new ModeCommand(clientMock, this.getMockedChannel(clientMock, "#test"));
        ChannelUserMode mode = this.getChannelUserMode('A', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(true, mode, userMock);
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE #test +A kitteh");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddModeWithParameterViaUserButWrongClient() {
        Client clientMock = Mockito.mock(Client.class);
        User userMock = Mockito.mock(User.class);
        Mockito.when(userMock.getClient()).thenReturn(Mockito.mock(Client.class));
        Mockito.when(userMock.getNick()).thenReturn("kitteh");
        ModeCommand sut = new ModeCommand(clientMock, this.getMockedChannel(clientMock, "#test"));
        ChannelUserMode mode = this.getChannelUserMode('A', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(true, mode, userMock);
    }

    @Test
    public void testRemoveModeWithParameter() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedChannel(clientMock, "#test"));
        ChannelMode mode = this.getChannelMode('A', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(false, mode, "foo");
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE #test -A foo");
    }

    private Channel getMockedChannel(Client client, String name) {
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(channel.getClient()).thenReturn(client);
        Mockito.when(channel.getName()).thenReturn(name);
        return channel;
    }

    private ChannelMode getChannelMode(char c, Client client, ChannelMode.Type type) {
        return new ChannelMode() {
            @Override
            public char getChar() {
                return c;
            }

            @Nonnull
            @Override
            public Client getClient() {
                return client;
            }

            @Nonnull
            @Override
            public Type getType() {
                return type;
            }
        };
    }

    private ChannelUserMode getChannelUserMode(char c, Client client, ChannelMode.Type type) {
        return new ChannelUserMode() {
            @Override
            public char getChar() {
                return c;
            }

            @Nonnull
            @Override
            public Client getClient() {
                return client;
            }

            @Override
            public char getNickPrefix() {
                return '\u039b';
            }

            @Nonnull
            @Override
            public Type getType() {
                return type;
            }
        };
    }
}
