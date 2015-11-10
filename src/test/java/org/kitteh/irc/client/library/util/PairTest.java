package org.kitteh.irc.client.library.util;

import org.junit.Assert;
import org.junit.Test;

public class PairTest {

    @Test
    public void testPair() {
        Pair<String, Integer> sut = new Pair<>("Cats", 4);
        Assert.assertEquals("Cats", sut.getLeft());
        Assert.assertEquals(4, sut.getRight().intValue());
        Assert.assertTrue(sut.toString().contains("Cats"));
    }
}
