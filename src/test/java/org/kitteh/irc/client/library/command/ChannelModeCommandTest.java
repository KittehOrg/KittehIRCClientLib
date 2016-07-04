package org.kitteh.irc.client.library.command;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.mockito.InOrder;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * @see ChannelModeCommand
 */
public class ChannelModeCommandTest {
    private static final String CHANNEL = "#targetchannel";

    private Client getClient() {
        Client client = Mockito.mock(Client.class);
        Mockito.when(client.getChannel(CHANNEL)).thenReturn(Optional.of(Mockito.mock(Channel.class)));
        ServerInfo serverInfo = Mockito.mock(ServerInfo.class);
        Mockito.when(client.getServerInfo()).thenReturn(serverInfo);
        ISupportParameter.Modes modes = Mockito.mock(ISupportParameter.Modes.class);
        Mockito.when(serverInfo.getISupportParameter("MODES", ISupportParameter.Modes.class)).thenReturn(Optional.of(modes));
        Mockito.when(modes.getInteger()).thenReturn(3);
        return client;
    }

    @Test
    public void testWithNoModeChanges() {
        Client clientMock = this.getClient();

        ChannelModeCommand sut = new ChannelModeCommand(clientMock, CHANNEL);
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + CHANNEL);

        Assert.assertFalse(sut.toString().isEmpty());
    }

    @Test
    public void testWithOneSimpleModeChange() {
        Client clientMock = this.getClient();

        ChannelModeCommand sut = new ChannelModeCommand(clientMock, CHANNEL);
        ChannelMode mode = this.getChannelMode('A', clientMock, ChannelMode.Type.A_MASK);
        sut.add(true, mode);
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +A");
    }

    @Test
    public void testWithFourSimpleModeChanges() {
        Client clientMock = this.getClient();

        ChannelModeCommand sut = new ChannelModeCommand(clientMock, CHANNEL);
        sut.add(true, this.getChannelMode('A', clientMock, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.add(true, this.getChannelMode('B', clientMock, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.add(true, this.getChannelMode('C', clientMock, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.add(true, this.getChannelMode('D', clientMock, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.execute();
        InOrder inOrder = Mockito.inOrder(clientMock, clientMock);
        inOrder.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +ABCD");
    }

    @Test
    public void testWithFourComplexModeChanges() {
        Client clientMock = this.getClient();

        ChannelModeCommand sut = new ChannelModeCommand(clientMock, CHANNEL);
        sut.add(true, this.getChannelMode('A', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS), "hi");
        sut.add(true, this.getChannelMode('B', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS), "there");
        sut.add(true, this.getChannelMode('C', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS), "kitten");
        sut.add(true, this.getChannelMode('D', clientMock, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.execute();
        InOrder inOrder = Mockito.inOrder(clientMock, clientMock);
        inOrder.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +ABCD hi there kitten");
    }

    @Test
    public void testWithFourParameterizedModeChanges() {
        Client clientMock = this.getClient();

        ChannelModeCommand sut = new ChannelModeCommand(clientMock, CHANNEL);
        sut.add(true, this.getChannelMode('A', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS), "hi");
        sut.add(true, this.getChannelMode('B', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS), "there");
        sut.add(true, this.getChannelMode('C', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS), "kitten");
        sut.add(true, this.getChannelMode('D', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS), "meow");
        sut.execute();
        InOrder inOrder = Mockito.inOrder(clientMock, clientMock);
        inOrder.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +ABC hi there kitten");
        inOrder.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +D meow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithOneSimpleModeChangeButWrongClient() {
        Client clientMock = this.getClient();

        ChannelModeCommand sut = new ChannelModeCommand(clientMock, CHANNEL);
        ChannelMode mode = this.getChannelMode('A', Mockito.mock(Client.class), ChannelMode.Type.A_MASK);
        sut.add(true, mode);
    }

    @Test
    public void testWithAddAndRemove() {
        Client clientMock = this.getClient();

        ChannelModeCommand sut = new ChannelModeCommand(clientMock, CHANNEL);
        ChannelMode modeA = this.getChannelMode('A', clientMock, ChannelMode.Type.A_MASK);
        ChannelMode modeB = this.getChannelMode('B', clientMock, ChannelMode.Type.A_MASK);
        sut.add(true, modeA);
        sut.add(false, modeB);
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +A-B");
    }

    @Test
    public void testWithAddParameterisedAndSimpleMode() {
        Client clientMock = this.getClient();

        ChannelModeCommand sut = new ChannelModeCommand(clientMock, CHANNEL);
        ChannelMode modeA = this.getChannelMode('A', clientMock, ChannelMode.Type.A_MASK);
        ChannelMode modeB = this.getChannelMode('B', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(true, modeA);
        sut.add(true, modeB, "test");
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +AB test");
    }

    @Test
    public void testAddModeWithParameter() {
        Client clientMock = this.getClient();

        ChannelModeCommand sut = new ChannelModeCommand(clientMock, CHANNEL);
        ChannelMode mode = this.getChannelMode('A', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(true, mode, "foo");
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +A foo");
    }

    @Test
    public void testAddModeWithParameterViaUser() {
        Client clientMock = this.getClient();
        User userMock = Mockito.mock(User.class);
        Mockito.when(userMock.getClient()).thenReturn(clientMock);
        Mockito.when(userMock.getNick()).thenReturn("kitteh");

        ChannelModeCommand sut = new ChannelModeCommand(clientMock, CHANNEL);

        ChannelUserMode mode = this.getChannelUserMode('A', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(true, mode, userMock);
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +A kitteh");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddModeWithParameterViaUserButWrongClient() {
        Client clientMock = this.getClient();
        User userMock = Mockito.mock(User.class);
        Mockito.when(userMock.getClient()).thenReturn(Mockito.mock(Client.class));
        Mockito.when(userMock.getNick()).thenReturn("kitteh");
        ChannelModeCommand sut = new ChannelModeCommand(clientMock, CHANNEL);
        ChannelUserMode mode = this.getChannelUserMode('A', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(true, mode, userMock);
    }

    @Test
    public void testRemoveModeWithParameter() {
        Client clientMock = this.getClient();

        ChannelModeCommand sut = new ChannelModeCommand(clientMock, CHANNEL);
        ChannelMode mode = this.getChannelMode('A', clientMock, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(false, mode, "foo");
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " -A foo");
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
