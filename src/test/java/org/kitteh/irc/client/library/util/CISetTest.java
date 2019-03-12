package org.kitteh.irc.client.library.util;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.CaseMapping;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
}
