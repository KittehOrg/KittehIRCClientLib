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
        Cutter cutter = new Cutter.DefaultWordCutter();
        List<String> output = cutter.split("1 1 1 Meow meow mreow purrrrrr hisssssssss", 5);
        Assert.assertEquals(9, output.size());
        Assert.assertEquals("1 1 1", output.get(0));
        Assert.assertEquals("Meow", output.get(1));
        Assert.assertEquals("meow", output.get(2));
        Assert.assertEquals("mreow", output.get(3));
        Assert.assertEquals("purrr", output.get(4));
        Assert.assertEquals("rrr", output.get(5));
        Assert.assertEquals("hisss", output.get(6));
        Assert.assertEquals("sssss", output.get(7));
        Assert.assertEquals("s", output.get(8));
    }

    /**
     * Tests cutting when not necessary.
     */
    @Test
    public void cutShort() {
        Cutter cutter = new Cutter.DefaultWordCutter();
        List<String> output = cutter.split("Hello world!", 15);
        Assert.assertEquals(1, output.size());
        Assert.assertEquals("Hello world!", output.get(0));
    }
}
