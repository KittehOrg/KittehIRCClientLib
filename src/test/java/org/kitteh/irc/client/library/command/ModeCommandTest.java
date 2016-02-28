package org.kitteh.irc.client.library.command;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ChannelMode;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.kitteh.irc.client.library.element.User;
import org.mockito.Mockito;

import javax.annotation.Nonnull;

/**
 * @see ModeCommand
 */
public class ModeCommandTest {

    @Test
    public void testWithNoModeChanges() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedClient(clientMock, "#test"));
        sut.execute();
        Mockito.verify(clientMock, Mockito.never()).sendRawLine(Mockito.anyString());

        Assert.assertFalse(sut.toString().isEmpty());
    }

    @Test
    public void testWithOneSimpleModeChange() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedClient(clientMock, "#test"));
        ChannelMode mode = new ChannelMode() {
            @Override
            public char getChar() {
                return 'A';
            }

            @Nonnull
            @Override
            public Client getClient() {
                return clientMock;
            }

            @Nonnull
            @Override
            public Type getType() {
                return Type.A_MASK;
            }
        };
        sut.add(true, mode);
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE #test +A");
    }

    @Test
    public void testWithAddAndRemove() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedClient(clientMock, "#test"));
        ChannelMode modeA = new ChannelMode() {
            @Override
            public char getChar() {
                return 'A';
            }

            @Nonnull
            @Override
            public Client getClient() {
                return clientMock;
            }

            @Nonnull
            @Override
            public Type getType() {
                return Type.A_MASK;
            }
        };
        ChannelMode modeB = new ChannelMode() {
            @Override
            public char getChar() {
                return 'B';
            }

            @Nonnull
            @Override
            public Client getClient() {
                return clientMock;
            }

            @Nonnull
            @Override
            public Type getType() {
                return Type.A_MASK;
            }
        };
        sut.add(true, modeA);
        sut.add(false, modeB);
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE #test +A-B");
    }

    @Test
    public void testWithAddParameterisedAndSimpleMode() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedClient(clientMock, "#test"));
        ChannelMode modeA = new ChannelMode() {
            @Override
            public char getChar() {
                return 'A';
            }

            @Nonnull
            @Override
            public Client getClient() {
                return clientMock;
            }

            @Nonnull
            @Override
            public Type getType() {
                return Type.A_MASK;
            }
        };
        ChannelMode modeB = new ChannelMode() {
            @Override
            public char getChar() {
                return 'B';
            }

            @Nonnull
            @Override
            public Client getClient() {
                return clientMock;
            }

            @Nonnull
            @Override
            public Type getType() {
                return Type.B_PARAMETER_ALWAYS;
            }
        };
        sut.add(true, modeA);
        sut.add(true, modeB, "test");
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE #test +AB test");
    }

    @Test
    public void testAddModeWithParameter() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedClient(clientMock, "#test"));
        ChannelMode mode = new ChannelMode() {
            @Override
            public char getChar() {
                return 'A';
            }

            @Nonnull
            @Override
            public Client getClient() {
                return clientMock;
            }

            @Nonnull
            @Override
            public Type getType() {
                return Type.B_PARAMETER_ALWAYS;
            }
        };
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
        ModeCommand sut = new ModeCommand(clientMock, this.getMockedClient(clientMock, "#test"));
        ChannelUserMode mode = new ChannelUserMode() {
            @Override
            public char getChar() {
                return 'A';
            }

            @Nonnull
            @Override
            public Client getClient() {
                return clientMock;
            }

            /**
             * Gets the nickname prefix character.
             *
             * @return the character displayed in front of a nickname
             */
            @Override
            public char getNickPrefix() {
                return '\u039b';
            }

            @Nonnull
            @Override
            public Type getType() {
                return Type.B_PARAMETER_ALWAYS;
            }
        };
        sut.add(true, mode, userMock);
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE #test +A kitteh");
    }

    @Test
    public void testRemoveModeWithParameter() {
        Client clientMock = Mockito.mock(Client.class);

        ModeCommand sut = new ModeCommand(clientMock, this.getMockedClient(clientMock, "#test"));
        ChannelMode mode = new ChannelMode() {
            @Override
            public char getChar() {
                return 'A';
            }

            @Nonnull
            @Override
            public Client getClient() {
                return clientMock;
            }

            @Nonnull
            @Override
            public Type getType() {
                return Type.B_PARAMETER_ALWAYS;
            }
        };
        sut.add(false, mode, "foo");
        sut.execute();
        Mockito.verify(clientMock, Mockito.times(1)).sendRawLine("MODE #test -A foo");
    }

    private Channel getMockedClient(Client client, String name) {
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(channel.getClient()).thenReturn(client);
        Mockito.when(channel.getName()).thenReturn(name);
        return channel;
    }
}
