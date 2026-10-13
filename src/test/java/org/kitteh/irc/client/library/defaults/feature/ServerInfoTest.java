package org.kitteh.irc.client.library.defaults.feature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportCaseMapping;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportChanModes;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportChanTypes;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportUTF8Mapping;
import org.kitteh.irc.client.library.defaults.element.mode.DefaultChannelMode;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.feature.CaseMapping;
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

    /**
     * Tests channel validity follows CHANTYPES.
     */
    @Test
    public void testValidChannel() {
        final Client client = Mockito.mock(Client.class);
        final DefaultServerInfo serverInfo = new DefaultServerInfo(client);
        Assertions.assertTrue(serverInfo.isValidChannel("#foo"));
        Assertions.assertTrue(serverInfo.isValidChannel("&foo"));
        Assertions.assertFalse(serverInfo.isValidChannel("!foo"));
        Assertions.assertFalse(serverInfo.isValidChannel("#"));
        Assertions.assertFalse(serverInfo.isValidChannel("#foo bar"));
        Assertions.assertFalse(serverInfo.isValidChannel("#foo,bar"));
        Assertions.assertFalse(serverInfo.isValidChannel("#foo\u0007"));
        serverInfo.addISupportParameter(new DefaultISupportChanTypes(client, DefaultISupportChanTypes.NAME, "#~"));
        Assertions.assertTrue(serverInfo.isValidChannel("~foo"));
        Assertions.assertTrue(serverInfo.isValidChannel("#foo"));
        Assertions.assertFalse(serverInfo.isValidChannel("&foo"));
    }

    /**
     * Tests UTF8MAPPING takes precedence over CASEMAPPING.
     */
    @Test
    public void testCaseMapping() {
        final Client client = Mockito.mock(Client.class);
        final DefaultServerInfo serverInfo = new DefaultServerInfo(client);
        Assertions.assertEquals(CaseMapping.RFC1459, serverInfo.getCaseMapping());
        serverInfo.addISupportParameter(new DefaultISupportCaseMapping(client, ISupportParameter.CaseMapping.NAME, "ascii"));
        Assertions.assertEquals(CaseMapping.ASCII, serverInfo.getCaseMapping());
        serverInfo.addISupportParameter(new DefaultISupportUTF8Mapping(client, ISupportParameter.UTF8Mapping.NAME, "rfc8265"));
        Assertions.assertEquals(CaseMapping.RFC8265, serverInfo.getCaseMapping());
        serverInfo.removeISupportParameter(ISupportParameter.UTF8Mapping.NAME);
        Assertions.assertEquals(CaseMapping.ASCII, serverInfo.getCaseMapping());
    }
}
