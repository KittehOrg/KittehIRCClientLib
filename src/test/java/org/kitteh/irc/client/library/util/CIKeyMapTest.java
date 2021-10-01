package org.kitteh.irc.client.library.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.CaseMapping;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

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
        Assertions.assertTrue(sut.isEmpty());
        sut.put("KITTEN", "foobar");

        Assertions.assertTrue(sut.containsKey("KITTEN"));
        Assertions.assertTrue(sut.containsKey("kitten"));
        Assertions.assertEquals("foobar", sut.get("kitten"));
        Assertions.assertEquals(1, sut.size());
        Assertions.assertTrue(sut.containsValue("foobar"));
        Assertions.assertFalse(sut.isEmpty());
        Assertions.assertArrayEquals(new String[]{"KITTEN"}, sut.keySet().toArray());
        Assertions.assertArrayEquals(new String[]{"foobar"}, sut.values().toArray());
        Assertions.assertFalse(sut.toString().isEmpty());

        final Exception ex = new Exception("Not a valid key!");
        Assertions.assertFalse(sut.containsKey(ex));
        Assertions.assertNull(sut.get(ex));
        Assertions.assertNull(sut.remove(ex));
        Assertions.assertNull(sut.remove("somestring"));
        Assertions.assertNull(sut.put("somestring", "somevalue"));
        Assertions.assertEquals("somevalue", sut.put("somestring", "someothervalue"));
        sut.clear();
        Assertions.assertTrue(sut.isEmpty());

        Map<String, String> map = new HashMap<>(2);
        map.put("one", "two");
        map.put("three", "four");

        sut.putAll(map);
        Assertions.assertEquals(2, sut.size());
        Assertions.assertTrue(sut.keySet().containsAll(map.keySet()));

        sut.put("key", null);
        Assertions.assertTrue(sut.containsValue(null));
    }

    /**
     * Tests with RFC.
     */
    @Test
    public void testWithRfc1459() {
        Client client = this.getMockClientWithCaseMapping(CaseMapping.RFC1459);
        CIKeyMap<String> sut = new CIKeyMap<>(client);

        sut.put("[cat]", "kitten");
        Assertions.assertTrue(sut.containsKey("[cat]"));
        Assertions.assertTrue(sut.containsKey("{cat}"));
        Assertions.assertEquals("kitten", sut.get("{cat}"));

        sut.put("[cat]^", "kitteh");
        Assertions.assertTrue(sut.containsKey("[cat]~"));
        Assertions.assertTrue(sut.containsKey("{cat}^"));
        Assertions.assertEquals("kitteh", sut.get("{cat}^"));
        Assertions.assertEquals(2, sut.size());

        Assertions.assertEquals("kitteh", sut.remove("[cat]^"));
        Assertions.assertEquals(1, sut.size());
    }

    /**
     * Tests with RFC... strictly speaking.
     */
    @Test
    public void testWithRfc1459Strict() {
        Client client = this.getMockClientWithCaseMapping(CaseMapping.STRICT_RFC1459);
        CIKeyMap<String> sut = new CIKeyMap<>(client);

        sut.put("[cat]^", "kitteh");
        Assertions.assertTrue(sut.containsKey("[cat]^"));
        Assertions.assertTrue(sut.containsKey("{cat}^"));

        Assertions.assertFalse(sut.containsKey("{cat}~"));
        Assertions.assertFalse(sut.containsValue("cat"));
        Assertions.assertNull(sut.get("kitty"));
        Assertions.assertEquals("kitteh", sut.get("{cat}^"));
        Assertions.assertEquals(1, sut.size());
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
}
