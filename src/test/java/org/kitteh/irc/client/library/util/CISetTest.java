package org.kitteh.irc.client.library.util;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.CaseMapping;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.ServerInfo;
import org.kitteh.irc.client.library.element.ChannelMode;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CISetTest {

    @Test
    public void testWithAscii() {
        Client client = getMockClientWithCaseMapping(CaseMapping.ASCII);
        CISet sut = new CISet(client);
        Assert.assertTrue(sut.isEmpty());
        Assert.assertTrue(sut.add("CAT"));
        Assert.assertTrue(sut.contains("cat"));
        Assert.assertTrue(sut.contains("CAT"));
        Assert.assertTrue(sut.contains("CAt"));
        Assert.assertTrue(sut.remove("cat"));
        Assert.assertFalse(sut.remove("cat"));
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
        list = Arrays.asList("cat", "magpie", "rhino", "kangaroo");
        Assert.assertFalse(sut.containsAll(list));
        Assert.assertFalse(sut.toString().isEmpty());

        sut.retainAll(Collections.singletonList("cat"));
        Assert.assertEquals(1, sut.size());

        sut.removeAll(list);
        Assert.assertTrue(sut.isEmpty());
    }

    @Test
    public void testWithRfc1459() {
        Client client = getMockClientWithCaseMapping(CaseMapping.RFC1459);
        CISet sut = new CISet(client);
        sut.add("[cat]^");
        Assert.assertTrue(sut.contains("{cat}~"));
    }

    @Test
    public void testWithRfc1459Strict() {
        Client client = getMockClientWithCaseMapping(CaseMapping.STRICT_RFC1459);
        CISet sut = new CISet(client);
        sut.add("[cat]");
        Assert.assertTrue(sut.contains("{cat}"));
    }


    public Client getMockClientWithCaseMapping(CaseMapping mapping) {
        Client clientMock = Mockito.mock(Client.class);
        Mockito.when(clientMock.getServerInfo()).thenReturn(new StubServerInfo(mapping));
        return clientMock;
    }

    class StubServerInfo implements ServerInfo {
        private CaseMapping caseMapping;

        public StubServerInfo(CaseMapping caseMapping) {
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
        public Optional<List<String>> getMOTD() {
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
        public Optional<String> getVersion() {
            return null;
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
