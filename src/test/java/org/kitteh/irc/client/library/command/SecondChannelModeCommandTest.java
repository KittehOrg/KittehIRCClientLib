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
import org.kitteh.irc.client.library.element.mode.ModeStatus;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * @see ChannelModeCommand
 */
public class SecondChannelModeCommandTest {
    private static final String CHANNEL = "#targetchannel";

    private Client client;

    /**
     * And then Kitteh said, let there be test!
     */
    @Before
    public void before() {
        this.client = Mockito.mock(Client.class);
        ServerInfo serverInfo = Mockito.mock(ServerInfo.class);
        Mockito.when(this.client.getServerInfo()).thenReturn(serverInfo);
        Mockito.when(serverInfo.isValidChannel(Mockito.any())).thenReturn(true);
        ISupportParameter.Modes modes = Mockito.mock(ISupportParameter.Modes.class);
    }

    @Test
    public void testWithNoModeChanges() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        sut.execute();
        Mockito.verify(this.client, Mockito.times(1)).sendRawLine("MODE " + CHANNEL);

        Assert.assertFalse(sut.toString().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithOneSimpleModeChangeButWrongClient() {
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelMode mode = this.getChannelMode('A', Mockito.mock(Client.class), ChannelMode.Type.A_MASK);
        sut.add(ModeStatus.Action.ADD, mode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddModeWithParameterViaUserButWrongClient() {
        User userMock = Mockito.mock(User.class);
        Mockito.when(userMock.getClient()).thenReturn(Mockito.mock(Client.class));
        ChannelModeCommand sut = new ChannelModeCommand(this.client, CHANNEL);
        ChannelUserMode mode = this.getChannelUserMode('A', this.client, ChannelMode.Type.B_PARAMETER_ALWAYS);
        sut.add(ModeStatus.Action.ADD, mode, userMock);
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
