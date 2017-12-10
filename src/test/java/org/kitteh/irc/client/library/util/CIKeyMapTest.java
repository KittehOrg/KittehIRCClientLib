package org.kitteh.irc.client.library.util;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.feature.CaseMapping;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tests the CIKeyMap.
 */
public class CIKeyMapTest {
    /**
     * Tests with ascii.
     */
    @Test
    public void testWithAscii() {
        Client client = this.getMockClientWithCaseMapping(CaseMapping.ASCII);
        CIKeyMap<String> sut = new CIKeyMap<>(client);
        Assert.assertTrue(sut.isEmpty());
        sut.put("KITTEN", "foobar");

        Assert.assertTrue(sut.containsKey("KITTEN"));
        Assert.assertTrue(sut.containsKey("kitten"));
        Assert.assertEquals("foobar", sut.get("kitten"));
        Assert.assertEquals(1, sut.size());
        Assert.assertTrue(sut.containsValue("foobar"));
        Assert.assertFalse(sut.isEmpty());
        Assert.assertArrayEquals(new String[]{"KITTEN"}, sut.keySet().toArray());
        Assert.assertArrayEquals(new String[]{"foobar"}, sut.values().toArray());
        Assert.assertFalse(sut.toString().isEmpty());

        final Exception ex = new Exception("Not a valid key!");
        Assert.assertFalse(sut.containsKey(ex));
        Assert.assertNull(sut.get(ex));
        Assert.assertNull(sut.remove(ex));
        Assert.assertNull(sut.remove("somestring"));
        Assert.assertNull(sut.put("somestring", "somevalue"));
        Assert.assertEquals("somevalue", sut.put("somestring", "someothervalue"));
        sut.clear();
        Assert.assertTrue(sut.isEmpty());

        Map<String, String> map = new HashMap<>(2);
        map.put("one", "two");
        map.put("three", "four");

        sut.putAll(map);
        Assert.assertEquals(2, sut.size());
        Assert.assertTrue(sut.keySet().containsAll(map.keySet()));

        sut.put("key", null);
        Assert.assertTrue(sut.containsValue(null));
    }

    /**
     * Tests with RFC.
     */
    @Test
    public void testWithRfc1459() {
        Client client = this.getMockClientWithCaseMapping(CaseMapping.RFC1459);
        CIKeyMap<String> sut = new CIKeyMap<>(client);

        sut.put("[cat]", "kitten");
        Assert.assertTrue(sut.containsKey("[cat]"));
        Assert.assertTrue(sut.containsKey("{cat}"));
        Assert.assertEquals("kitten", sut.get("{cat}"));

        sut.put("[cat]^", "kitteh");
        Assert.assertTrue(sut.containsKey("[cat]~"));
        Assert.assertTrue(sut.containsKey("{cat}^"));
        Assert.assertEquals("kitteh", sut.get("{cat}^"));
        Assert.assertEquals(2, sut.size());

        Assert.assertEquals("kitteh", sut.remove("[cat]^"));
        Assert.assertEquals(1, sut.size());
    }

    /**
     * Tests with RFC... strictly speaking.
     */
    @Test
    public void testWithRfc1459Strict() {
        Client client = this.getMockClientWithCaseMapping(CaseMapping.STRICT_RFC1459);
        CIKeyMap<String> sut = new CIKeyMap<>(client);

        sut.put("[cat]^", "kitteh");
        Assert.assertTrue(sut.containsKey("[cat]^"));
        Assert.assertTrue(sut.containsKey("{cat}^"));

        Assert.assertFalse(sut.containsKey("{cat}~"));
        Assert.assertFalse(sut.containsValue("cat"));
        Assert.assertNull(sut.get("kitty"));
        Assert.assertEquals("kitteh", sut.get("{cat}^"));
        Assert.assertEquals(1, sut.size());
    }

    /**
     * Gets a mock client with a certain casemapping.
     *
     * @param mapping case mapping requested
     * @return requested client
     */
    public Client getMockClientWithCaseMapping(CaseMapping mapping) {
        Client clientMock = Mockito.mock(Client.class);
        Mockito.when(clientMock.getServerInfo()).thenReturn(new StubServerInfo(mapping));
        return clientMock;
    }

    class StubServerInfo implements ServerInfo {
        private final CaseMapping caseMapping;

        StubServerInfo(CaseMapping caseMapping) {
            this.caseMapping = caseMapping;
        }

        @Nonnull
        @Override
        public Optional<String> getAddress() {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public CaseMapping getCaseMapping() {
            return this.caseMapping;
        }

        @Override
        public int getChannelLengthLimit() {
            return 0;
        }

        @Nonnull
        @Override
        public Map<Character, Integer> getChannelLimits() {
            return Collections.emptyMap();
        }

        @Nonnull
        @Override
        public List<ChannelMode> getChannelModes() {
            return Collections.emptyList();
        }

        @Nonnull
        @Override
        public List<Character> getChannelPrefixes() {
            return Collections.emptyList();
        }

        @Nonnull
        @Override
        public List<ChannelUserMode> getChannelUserModes() {
            return Collections.emptyList();
        }

        @Nonnull
        @Override
        public Optional<ISupportParameter> getISupportParameter(@Nonnull String name) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Map<String, ISupportParameter> getISupportParameters() {
            return null;
        }

        @Nonnull
        @Override
        public Optional<List<String>> getMotd() {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<String> getNetworkName() {
            return Optional.empty();
        }

        @Override
        public int getNickLengthLimit() {
            return 0;
        }

        @Nonnull
        @Override
        public List<UserMode> getUserModes() {
            return null;
        }

        @Nonnull
        @Override
        public Optional<String> getVersion() {
            return Optional.empty();
        }

        @Override
        public boolean hasWhoXSupport() {
            return false;
        }

        @Override
        public boolean isValidChannel(@Nonnull String name) {
            return false;
        }
    }
}
