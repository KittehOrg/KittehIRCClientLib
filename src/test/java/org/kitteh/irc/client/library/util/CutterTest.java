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
        Assertions.assertEquals("Hello world!", output.getFirst());
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
     * Tests a multi-byte word longer than the limit in bytes but not in chars.
     */
    @Test
    public void cutMultiByteWord() {
        String message = "中".repeat(600);
        List<String> output = new Cutter.DefaultWordCutter().split(message, 400);
        Assertions.assertEquals(message, String.join("", output));
        output.forEach(line -> Assertions.assertTrue(utf8(line) <= 400, "Line too long: " + utf8(line)));
        Assertions.assertEquals(133, output.getFirst().length());
    }

    /**
     * Tests that multi-byte words are measured in bytes.
     */
    @Test
    public void cutMultiByteWords() {
        String message = ("é".repeat(30) + " ").repeat(10).trim();
        List<String> output = new Cutter.DefaultWordCutter().split(message, 100);
        Assertions.assertEquals(message.replace(" ", ""), String.join("", output).replace(" ", ""));
        output.forEach(line -> Assertions.assertTrue(utf8(line) <= 100, "Line too long: " + utf8(line)));
    }

    /**
     * Tests that surrogate pairs are not split.
     */
    @Test
    public void cutSurrogates() {
        String message = "a b" + "😀".repeat(60);
        List<String> output = new Cutter.DefaultWordCutter().split(message, 101);
        Assertions.assertEquals(message.replace(" ", ""), String.join("", output).replace(" ", ""));
        for (String line : output) {
            Assertions.assertTrue(utf8(line) <= 101, "Line too long: " + utf8(line));
            Assertions.assertFalse(Character.isHighSurrogate(line.charAt(line.length() - 1)), "Split surrogate pair");
            Assertions.assertFalse(Character.isLowSurrogate(line.charAt(0)), "Split surrogate pair");
        }
    }

    /**
     * Tests a limit smaller than a single code point.
     */
    @Test
    public void cutTinyLimit() {
        List<String> output = new Cutter.DefaultWordCutter().split("😀😀", 2);
        Assertions.assertEquals(List.of("😀", "😀"), output);
    }

    private static int utf8(String string) {
        return string.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
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
