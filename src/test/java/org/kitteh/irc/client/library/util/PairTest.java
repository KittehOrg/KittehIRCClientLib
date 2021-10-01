package org.kitteh.irc.client.library.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * It's a pair test.
 */
public class PairTest {
    /**
     * Tests pairstuff.
     */
    @Test
    public void testPair() {
        Pair<String, Integer> sut = new Pair<>("Cats", 4);
        Assertions.assertEquals("Cats", sut.getLeft());
        Assertions.assertEquals(4, sut.getRight().intValue());
        Assertions.assertTrue(sut.toString().contains("Cats"));
    }

    /**
     * Tests more pairstuff.
     */
    @Test
    public void testPairMore() {
        Pair<String, Integer> sut = Pair.of("Cats", 4);
        Assertions.assertEquals("Cats", sut.getLeft());
        Assertions.assertEquals(4, sut.getRight().intValue());
        Assertions.assertTrue(sut.toString().contains("Cats"));
    }
}
