package org.kitteh.irc.client.library.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
        Assertions.assertTrue(sut.isEmpty());
        Assertions.assertTrue(sut.add("CAT"));
        Assertions.assertTrue(sut.contains("cat"));
        Assertions.assertTrue(sut.contains("CAT"));
        Assertions.assertTrue(sut.contains("CAt"));
        Assertions.assertFalse(sut.contains(null));
        Assertions.assertTrue(sut.remove("cat"));
        Assertions.assertFalse(sut.remove("cat"));
        Assertions.assertFalse(sut.remove(null));
        Assertions.assertFalse(sut.iterator().hasNext());

        sut.add("dog");
        Assertions.assertEquals("dog", sut.iterator().next());
        Assertions.assertEquals(1, sut.size());
        sut.clear();
        Assertions.assertEquals(0, sut.size());

        List<String> list = Arrays.asList("cat", "magpie", "rhino");
        sut.addAll(list);

        Assertions.assertEquals(3, sut.size());
        for (Object item : sut.toArray()) {
            Assertions.assertTrue(list.contains(item));
        }

        String[] foobar = new String[3];
        sut.toArray(foobar);

        Assertions.assertFalse(foobar[0].isEmpty());
        Assertions.assertTrue(sut.containsAll(list));
        List<String> listlist = Arrays.asList("cat", "magpie", "rhino", "kangaroo");
        Assertions.assertFalse(sut.containsAll(listlist));
        Assertions.assertFalse(sut.toString().isEmpty());

        sut.retainAll(Collections.singletonList("cat"));
        Assertions.assertEquals(1, sut.size());

        listlist.forEach(sut::remove);
        Assertions.assertTrue(sut.isEmpty());
    }

    /**
     * Tests the RFCstuff.
     */
    @Test
    public void testWithRfc1459() {
        Client client = this.getMockClientWithCaseMapping(CaseMapping.RFC1459);
        CISet sut = new CISet(client);
        sut.add("[cat]^");
        Assertions.assertTrue(sut.contains("{cat}~"));
    }

    /**
     * Tests the RFCstuff, but stricter.
     */
    @Test
    public void testWithRfc1459Strict() {
        Client client = this.getMockClientWithCaseMapping(CaseMapping.STRICT_RFC1459);
        CISet sut = new CISet(client);
        sut.add("[cat]");
        Assertions.assertTrue(sut.contains("{cat}"));
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
