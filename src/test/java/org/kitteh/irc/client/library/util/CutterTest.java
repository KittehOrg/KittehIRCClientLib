package org.kitteh.irc.client.library.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests {@link Cutter.DefaultWordCutter}.
 */
public class CutterTest {
    /**
     * Tests most functionality of the cutter.
     */
    @Test
    public void cut() {
        List<String> output = new Cutter.DefaultWordCutter().split("0 purrrrr 1 1 1 Meow meow mreow hisssssssss", 5);
        Assert.assertEquals(9, output.size());
        Assert.assertEquals("0 pur", output.get(0));
        Assert.assertEquals("rrrr", output.get(1));
        Assert.assertEquals("1 1 1", output.get(2));
        Assert.assertEquals("Meow", output.get(3));
        Assert.assertEquals("meow", output.get(4));
        Assert.assertEquals("mreow", output.get(5));
        Assert.assertEquals("hisss", output.get(6));
        Assert.assertEquals("sssss", output.get(7));
        Assert.assertEquals("s", output.get(8));
    }

    /**
     * Tests cutting when not necessary.
     */
    @Test
    public void cutShort() {
        List<String> output = new Cutter.DefaultWordCutter().split("Hello world!", 15);
        Assert.assertEquals(1, output.size());
        Assert.assertEquals("Hello world!", output.get(0));
    }

    /**
     * Tests ability to fail.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cutFailNegative() {
        new Cutter.DefaultWordCutter().split("Hi", -2);
    }

    /**
     * Tests ability to fail.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cutFailNull() {
        new Cutter.DefaultWordCutter().split(null, 2);
    }

    /**
     * Spaaaaaaaaace!
     */
    @Test
    public void cutSpace() {
        List<String> output = new Cutter.DefaultWordCutter().split("                     ", 3);
        Assert.assertEquals(0, output.size());
    }
}
