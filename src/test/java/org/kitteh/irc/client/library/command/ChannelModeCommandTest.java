package org.kitteh.irc.client.library.command;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Assert;
import org.junit.Before;
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

import java.util.Optional;

/**
 * @see ChannelModeCommand
 */
public class ChannelModeCommandTest {
    private static final String CHANNEL = "#targetchannel";

    private Client client;

    /**
     * And then Kitteh said, let there be test!
     */
    @Before
    public void before() {
        this.client = Mockito.mock(Client.class);
        Mockito.when(this.client.getChannel(CHANNEL)).thenReturn(Optional.of(Mockito.mock(Channel.class)));
        ServerInfo serverInfo = Mockito.mock(ServerInfo.class);
        Mockito.when(this.client.getServerInfo()).thenReturn(serverInfo);
        Mockito.when(serverInfo.isValidChannel(Mockito.any())).thenReturn(true);
        ISupportParameter.Modes modes = Mockito.mock(ISupportParameter.Modes.class);
        Mockito.when(serverInfo.getISupportParameter("MODES", ISupportParameter.Modes.class)).thenReturn(Optional.of(modes));
        Mockito.when(modes.getInteger()).thenReturn(3);
    }

    @Test
    public void testWithNoModeChanges() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL);

        Assert.assertFalse(sut.toString().isEmpty());
    }

    @Test
    public void testWithOneSimpleModeChange() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelMode mode = this.getChannelMode('A', this.client, ChannelMode.Type.A_MASK);
        sut.add(true, mode);
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +A");
    }

    @Test
    public void testWithFourSimpleModeChanges() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        sut.add(true, this.getChannelMode('A', this.client, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.add(true, this.getChannelMode('B', this.client, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.add(true, this.getChannelMode('C', this.client, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.add(true, this.getChannelMode('D', this.client, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.execute();
        InOrder inOrder = Mockito.inOrder(this.client, this.client);
        inOrder.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +ABCD");
    }

    @Test
    public void testWithFourComplexModeChanges() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        sut.add(true, this.getChannelMode('A', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "hi");
        sut.add(true, this.getChannelMode('B', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "there");
        sut.add(true, this.getChannelMode('C', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "kitten");
        sut.add(true, this.getChannelMode('D', this.client, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.execute();
        InOrder inOrder = Mockito.inOrder(this.client, this.client);
        inOrder.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +ABCD hi there kitten");
    }

    @Test
    public void testWithFourParameterizedModeChanges() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        sut.add(true, this.getChannelMode('A', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "hi");
        sut.add(true, this.getChannelMode('B', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "there");
        sut.add(true, this.getChannelMode('C', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "kitten");
        sut.add(true, this.getChannelMode('D', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "meow");
        sut.execute();
        InOrder inOrder = Mockito.inOrder(this.client, this.client);
        inOrder.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +ABC hi there kitten");
        inOrder.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +D meow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithOneSimpleModeChangeButWrongClient() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelMode mode = this.getChannelMode('A', Mockito.mock(Client.class), ChannelMode.Type.A_MASK);
        sut.add(true, mode);
    }

    @Test
    public void testWithAddAndRemove() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelMode modeA = this.getChannelMode('A', this.client, ChannelMode.Type.A_MASK);
        ChannelMode modeB = this.getChannelMode('B', this.client, ChannelMode.Type.A_MASK);
        sut.add(true, modeA);
        sut.add(false, modeB);
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +A-B");
    }

    @Test
    public void testWithAddParameterisedAndSimpleMode() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelMode modeA = this.getChannelMode('A', this.client, ChannelMode.Type.A_MASK);
        ChannelMode modeB = this.getChannelMode('B', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(true, modeA);
        sut.add(true, modeB, "test");
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +AB test");
    }

    @Test
    public void testAddModeWithParameter() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelMode mode = this.getChannelMode('A', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(true, mode, "foo");
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +A foo");
    }

    @Test
    public void testAddModeWithParameterViaUser() {
        User userMock = Mockito.mock(User.class);
        Mockito.when(userMock.getClient()).thenReturn(this.client);
        Mockito.when(userMock.getNick()).thenReturn("kitteh");

        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);

        ChannelUserMode mode = this.getChannelUserMode('A', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(true, mode, userMock);
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +A kitteh");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddModeWithParameterViaUserButWrongClient() {
        User userMock = Mockito.mock(User.class);
        Mockito.when(userMock.getClient()).thenReturn(Mockito.mock(Client.class));
        Mockito.when(userMock.getNick()).thenReturn("kitteh");
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelUserMode mode = this.getChannelUserMode('A', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(true, mode, userMock);
    }

    @Test
    public void testRemoveModeWithParameter() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelMode mode = this.getChannelMode('A', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(false, mode, "foo");
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " -A foo");
    }

    private ChannelMode getChannelMode(char c, Client client, ChannelMode.Type type) {
        return new ChannelMode() {
            @Override
            public char getChar() {
                return c;
            }

            @NonNull
            @Override
            public Client getClient() {
                return client;
            }

            @NonNull
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

            @NonNull
            @Override
            public Client getClient() {
                return client;
            }

            @Override
            public char getNickPrefix() {
                return '\u039b';
            }

            @NonNull
            @Override
            public Type getType() {
                return type;
            }
        };
    }
}
