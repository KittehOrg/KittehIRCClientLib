package org.kitteh.irc.client.library.defaults.feature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportChanModes;
import org.kitteh.irc.client.library.defaults.element.mode.DefaultChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.mockito.Mockito;

import java.util.List;

/**
 * A test for DefaultServerInfo
 */
public class ServerInfoTest {
    /**
     * Tests modes.
     */
    @Test
    public void testChannelModes() {
        final Client client = Mockito.mock(Client.class);
        final DefaultServerInfo serverInfo = new DefaultServerInfo(client);
        serverInfo.addISupportParameter(new DefaultISupportChanModes(client, DefaultISupportChanModes.NAME, "b,k,l,imnDdRcC"));
        List<ChannelMode> modes = serverInfo.getChannelModes();
        Assertions.assertEquals(11, modes.size());
        Assertions.assertEquals(1, modes.stream().filter(mode -> mode.getType() == ChannelMode.Type.A_MASK).count());
        Assertions.assertEquals(1, modes.stream().filter(mode -> mode.getType() == ChannelMode.Type.B_PARAMETER_ALWAYS).count());
        Assertions.assertEquals(1, modes.stream().filter(mode -> mode.getType() == ChannelMode.Type.C_PARAMETER_ON_SET).count());
        Assertions.assertEquals(8, modes.stream().filter(mode -> mode.getType() == ChannelMode.Type.D_PARAMETER_NEVER).count());
        serverInfo.addCustomChannelMode(new DefaultChannelMode(client, 'z', ChannelMode.Type.D_PARAMETER_NEVER));
        serverInfo.addCustomChannelMode(new DefaultChannelMode(client, 'd', ChannelMode.Type.C_PARAMETER_ON_SET));
        List<ChannelMode> modesRedux = serverInfo.getChannelModes();
        Assertions.assertEquals(12, modesRedux.size());
        Assertions.assertEquals(1, modesRedux.stream().filter(mode -> mode.getType() == ChannelMode.Type.A_MASK).count());
        Assertions.assertEquals(1, modesRedux.stream().filter(mode -> mode.getType() == ChannelMode.Type.B_PARAMETER_ALWAYS).count());
        Assertions.assertEquals(2, modesRedux.stream().filter(mode -> mode.getType() == ChannelMode.Type.C_PARAMETER_ON_SET).count());
        Assertions.assertEquals(8, modesRedux.stream().filter(mode -> mode.getType() == ChannelMode.Type.D_PARAMETER_NEVER).count());
        Assertions.assertEquals(ChannelMode.Type.C_PARAMETER_ON_SET, serverInfo.getChannelMode('d').get().getType());
    }
}
