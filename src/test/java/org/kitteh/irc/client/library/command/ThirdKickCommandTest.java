package org.kitteh.irc.client.library.command;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.feature.SimpleDefaultMessageMap;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.mockito.Mockito;

/**
 * Tests the KickCommand
 */
public class ThirdKickCommandTest {
    private static final String CHANNEL = "#targetchannel";
    private static final String USER = "targetuser";
    private static final String REASON = "bad breath";

    private Client client;
    private User user;

    /**
     * And then Kitteh said, let there be test!
     */
    @Before
    public void before() {
        this.client = Mockito.mock(Client.class);
        ServerInfo serverInfo = Mockito.mock(ServerInfo.class);
        this.user = Mockito.mock(User.class);
        Mockito.when(this.client.getServerInfo()).thenReturn(serverInfo);
        Mockito.when(serverInfo.isValidChannel(Mockito.any())).thenReturn(true);
        Mockito.when(this.client.getDefaultMessageMap()).thenReturn(new SimpleDefaultMessageMap(null));
        Mockito.when(this.client.toString()).thenReturn("CLIENT OMG");
    }


    /**
     * Confirms toString fires, has info in it.
     */
    @Test
    public void toStringer() {
        KickCommand command = new KickCommand(this.client, CHANNEL);

        Assert.assertTrue(command.toString().contains(CHANNEL));
    }


}
