package org.kitteh.irc.client.library.command;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.ModeStatus;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * @see ChannelModeCommand
 */
public class ChannelModeCommandTest {
    private static final String CHANNEL = "#targetchannel";

    private Client client;

    /**
     * And then Kitteh said, let there be test!
     */
    @BeforeEach
    public void before() {
        this.client = Mockito.mock(Client.class);
        Mockito.when(this.client.getChannel(CHANNEL)).thenReturn(Optional.of(Mockito.mock(Channel.class)));
        ServerInfo serverInfo = Mockito.mock(ServerInfo.class);
        Mockito.when(this.client.getServerInfo()).thenReturn(serverInfo);
        Mockito.when(serverInfo.isValidChannel(Mockito.any())).thenReturn(true);
        ISupportParameter.Modes modes = Mockito.mock(ISupportParameter.Modes.class);
        Mockito.when(serverInfo.getISupportParameter("MODES", ISupportParameter.Modes.class)).thenReturn(Optional.of(modes));
        Mockito.when(modes.getInteger()).thenReturn(OptionalInt.of(3));
    }

    @Test
    public void testWithNoModeChanges() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL);

        Assertions.assertFalse(sut.toString().isEmpty());
    }

    @Test
    public void testWithOneSimpleModeChange() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelMode mode = this.getChannelMode('A', this.client, ChannelMode.Type.A_MASK);
        sut.add(ModeStatus.Action.ADD, mode);
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +A");
    }

    @Test
    public void testWithFourSimpleModeChanges() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        sut.add(ModeStatus.Action.ADD, this.getChannelMode('A', this.client, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.add(ModeStatus.Action.ADD, this.getChannelMode('B', this.client, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.add(ModeStatus.Action.ADD, this.getChannelMode('C', this.client, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.add(ModeStatus.Action.ADD, this.getChannelMode('D', this.client, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.execute();
        InOrder inOrder = Mockito.inOrder(this.client, this.client);
        inOrder.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +ABCD");
    }

    @Test
    public void testWithFourComplexModeChanges() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        sut.add(ModeStatus.Action.ADD, this.getChannelMode('A', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "hi");
        sut.add(ModeStatus.Action.ADD, this.getChannelMode('B', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "there");
        sut.add(ModeStatus.Action.ADD, this.getChannelMode('C', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "kitten");
        sut.add(ModeStatus.Action.ADD, this.getChannelMode('D', this.client, ChannelMode.Type.D_PARAMETER_NEVER));
        sut.execute();
        InOrder inOrder = Mockito.inOrder(this.client, this.client);
        inOrder.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +ABCD hi there kitten");
    }

    @Test
    public void testWithFourParameterizedModeChanges() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        sut.add(ModeStatus.Action.ADD, this.getChannelMode('A', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "hi");
        sut.add(ModeStatus.Action.ADD, this.getChannelMode('B', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "there");
        sut.add(ModeStatus.Action.ADD, this.getChannelMode('C', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "kitten");
        sut.add(ModeStatus.Action.ADD, this.getChannelMode('D', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS), "meow");
        sut.execute();
        InOrder inOrder = Mockito.inOrder(this.client, this.client);
        inOrder.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +ABC hi there kitten");
        inOrder.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +D meow");
    }

    @Test
    public void testWithOneSimpleModeChangeButWrongClient() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelMode mode = this.getChannelMode('A', Mockito.mock(Client.class), ChannelMode.Type.A_MASK);
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.add(ModeStatus.Action.ADD, mode));
    }

    @Test
    public void testWithAddAndRemove() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelMode modeA = this.getChannelMode('A', this.client, ChannelMode.Type.A_MASK);
        ChannelMode modeB = this.getChannelMode('B', this.client, ChannelMode.Type.A_MASK);
        sut.add(ModeStatus.Action.ADD, modeA);
        sut.add(ModeStatus.Action.REMOVE, modeB);
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +A-B");
    }

    @Test
    public void testWithAddParameterisedAndSimpleMode() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelMode modeA = this.getChannelMode('A', this.client, ChannelMode.Type.A_MASK);
        ChannelMode modeB = this.getChannelMode('B', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(ModeStatus.Action.ADD, modeA);
        sut.add(ModeStatus.Action.ADD, modeB, "test");
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +AB test");
    }

    @Test
    public void testAddModeWithParameter() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelMode mode = this.getChannelMode('A', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(ModeStatus.Action.ADD, mode, "foo");
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
        sut.add(ModeStatus.Action.ADD, mode, userMock);
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " +A kitteh");
    }

    @Test
    public void testAddModeWithParameterViaUserButWrongClient() {
        User userMock = Mockito.mock(User.class);
        Mockito.when(userMock.getClient()).thenReturn(Mockito.mock(Client.class));
        Mockito.when(userMock.getNick()).thenReturn("kitteh");
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelUserMode mode = this.getChannelUserMode('A', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS);
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.add(ModeStatus.Action.ADD, mode, userMock));
    }

    @Test
    public void testRemoveModeWithParameter() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelMode mode = this.getChannelMode('A', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(ModeStatus.Action.REMOVE, mode, "foo");
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL + " -A foo");
    }

    private ChannelMode getChannelMode(char c, Client client, ChannelMode.Type type) {
        return new ChannelMode() {
            @Override
            public char getChar() {
                return c;
            }

            @Override
            public @NonNull Client getClient() {
                return client;
            }

            @Override
            public @NonNull Type getType() {
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

            @Override
            public @NonNull Client getClient() {
                return client;
            }

            @Override
            public char getNickPrefix() {
                return '\u039b';
            }

            @Override
            public @NonNull Type getType() {
                return type;
            }
        };
    }
}
