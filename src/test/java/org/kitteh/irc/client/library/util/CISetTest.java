package org.kitteh.irc.client.library.util;

import org.checkerframework.checker.nullness.qual.NonNull;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tests CISet.
 */
public class CISetTest {
    /**
     * Tests ascii fun.
     */
    @Test
    public void testWithAscii() {
        Client client = this.getMockClientWithCaseMapping(CaseMapping.ASCII);
        CISet sut = new CISet(client);
        Assert.assertTrue(sut.isEmpty());
        Assert.assertTrue(sut.add("CAT"));
        Assert.assertTrue(sut.contains("cat"));
        Assert.assertTrue(sut.contains("CAT"));
        Assert.assertTrue(sut.contains("CAt"));
        Assert.assertFalse(sut.contains(null));
        Assert.assertTrue(sut.remove("cat"));
        Assert.assertFalse(sut.remove("cat"));
        Assert.assertFalse(sut.remove(null));
        Assert.assertFalse(sut.iterator().hasNext());

        sut.add("dog");
        Assert.assertEquals("dog", sut.iterator().next());
        Assert.assertEquals(1, sut.size());
        sut.clear();
        Assert.assertEquals(0, sut.size());

        List<String> list = Arrays.asList("cat", "magpie", "rhino");
        sut.addAll(list);

        Assert.assertEquals(3, sut.size());
        for (Object item : sut.toArray()) {
            Assert.assertTrue(list.contains(item));
        }

        String[] foobar = new String[3];
        sut.toArray(foobar);

        Assert.assertFalse(foobar[0].isEmpty());
        Assert.assertTrue(sut.containsAll(list));
        List<String> listlist = Arrays.asList("cat", "magpie", "rhino", "kangaroo");
        Assert.assertFalse(sut.containsAll(listlist));
        Assert.assertFalse(sut.toString().isEmpty());

        sut.retainAll(Collections.singletonList("cat"));
        Assert.assertEquals(1, sut.size());

        sut.removeAll(listlist);
        Assert.assertTrue(sut.isEmpty());
    }

    /**
     * Tests the RFCstuff.
     */
    @Test
    public void testWithRfc1459() {
        Client client = this.getMockClientWithCaseMapping(CaseMapping.RFC1459);
        CISet sut = new CISet(client);
        sut.add("[cat]^");
        Assert.assertTrue(sut.contains("{cat}~"));
    }

    /**
     * Tests the RFCstuff, but stricter.
     */
    @Test
    public void testWithRfc1459Strict() {
        Client client = this.getMockClientWithCaseMapping(CaseMapping.STRICT_RFC1459);
        CISet sut = new CISet(client);
        sut.add("[cat]");
        Assert.assertTrue(sut.contains("{cat}"));
    }

    /**
     * Gets a mock client with a certain casemapping.
     *
     * @param mapping case mapping requested
     * @return requested client
     */
    private Client getMockClientWithCaseMapping(CaseMapping mapping) {
        Client clientMock = Mockito.mock(Client.class);
        Mockito.when(clientMock.getServerInfo()).thenReturn(new StubServerInfo(mapping));
        return clientMock;
    }

    class StubServerInfo implements ServerInfo {
        private final CaseMapping caseMapping;

        StubServerInfo(CaseMapping caseMapping) {
            this.caseMapping = caseMapping;
        }

        @NonNull
        @Override
        public Optional<String> getAddress() {
            return Optional.empty();
        }

        @NonNull
        @Override
        public CaseMapping getCaseMapping() {
            return this.caseMapping;
        }

        @Override
        public int getChannelLengthLimit() {
            return 0;
        }

        @NonNull
        @Override
        public Map<Character, Integer> getChannelLimits() {
            return Collections.emptyMap();
        }

        @NonNull
        @Override
        public List<ChannelMode> getChannelModes() {
            return Collections.emptyList();
        }

        @NonNull
        @Override
        public List<Character> getChannelPrefixes() {
            return Collections.emptyList();
        }

        @NonNull
        @Override
        public List<ChannelUserMode> getChannelUserModes() {
            return Collections.emptyList();
        }

        @NonNull
        @Override
        public Optional<ISupportParameter> getISupportParameter(@NonNull String name) {
            return Optional.empty();
        }

        @NonNull
        @Override
        public Map<String, ISupportParameter> getISupportParameters() {
            return null;
        }

        @NonNull
        @Override
        public Optional<List<String>> getMotd() {
            return Optional.empty();
        }

        @NonNull
        @Override
        public Optional<String> getNetworkName() {
            return Optional.empty();
        }

        @Override
        public int getNickLengthLimit() {
            return 0;
        }

        @NonNull
        @Override
        public List<UserMode> getUserModes() {
            return null;
        }

        @NonNull
        @Override
        public Optional<String> getVersion() {
            return Optional.empty();
        }

        @Override
        public boolean hasWhoXSupport() {
            return false;
        }

        @Override
        public boolean isValidChannel(@NonNull String name) {
            return false;
        }
    }
}
