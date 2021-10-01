package org.kitteh.irc.client.library.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals(9, output.size());
        Assertions.assertEquals("0 pur", output.get(0));
        Assertions.assertEquals("rrrr", output.get(1));
        Assertions.assertEquals("1 1 1", output.get(2));
        Assertions.assertEquals("Meow", output.get(3));
        Assertions.assertEquals("meow", output.get(4));
        Assertions.assertEquals("mreow", output.get(5));
        Assertions.assertEquals("hisss", output.get(6));
        Assertions.assertEquals("sssss", output.get(7));
        Assertions.assertEquals("s", output.get(8));
    }

    /**
     * Tests a loooong start.
     */
    @Test
    public void cutLong() {
        List<String> output = new Cutter.DefaultWordCutter().split("meoooow", 5);
        Assertions.assertEquals(2, output.size());
        Assertions.assertEquals("meooo", output.get(0));
        Assertions.assertEquals("ow", output.get(1));
    }

    /**
     * Tests cutting when not necessary.
     */
    @Test
    public void cutShort() {
        List<String> output = new Cutter.DefaultWordCutter().split("Hello world!", 15);
        Assertions.assertEquals(1, output.size());
        Assertions.assertEquals("Hello world!", output.get(0));
    }

    /**
     * Tests ability to fail.
     */
    @Test
    public void cutFailNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Cutter.DefaultWordCutter().split("Hi", -2));
    }

    /**
     * Tests ability to fail.
     */
    @Test
    public void cutFailNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Cutter.DefaultWordCutter().split(null, 2));
    }

    /**
     * Spaaaaaaaaace!
     */
    @Test
    public void cutSpace() {
        List<String> output = new Cutter.DefaultWordCutter().split("                     ", 3);
        Assertions.assertEquals(0, output.size());
    }
}
