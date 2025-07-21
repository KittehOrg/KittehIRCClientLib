package org.kitteh.irc.client.library;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.util.Format;

/**
 * Make sure nothing has gone horribly wrong at the format factory.
 */
public class FormatTest {
    /**
     * Tests valid background input.
     */
    @Test
    public void background() {
        Assertions.assertEquals("\u000309,01", Format.GREEN.withBackground(Format.BLACK));
    }

    /**
     * Tests invalid background input.
     */
    @Test
    public void backgroundNonColorBackground() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Format.GREEN.withBackground(Format.BOLD));
    }

    /**
     * Tests invalid background input.
     */
    @Test
    public void backgroundNonColorForeground() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Format.BOLD.withBackground(Format.GREEN));
    }

    /**
     * Tests invalid background input.
     */
    @Test
    public void backgroundNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Format.GREEN.withBackground(null));
    }

    /**
     * Tests for colors of incorrect length.
     */
    @Test
    public void format() {
        String colorChar = Format.COLOR_CHAR + "";
        for (Format format : Format.values()) {
            Assertions.assertTrue(!format.toString().startsWith(colorChar) || (format.toString().length() == 3), "Color format with wrong length: " + format.name());
        }
    }

    /**
     * Tests color stripper.
     */
    @Test
    public void stripColor() {
        Assertions.assertEquals("meow", Format.stripColor(Format.GREEN + "meow"));
        Assertions.assertEquals(Format.BOLD + "purr", Format.stripColor(Format.BOLD + "purr"));
    }

    /**
     * Tests format stripper.
     */
    @Test
    public void stripFormat() {
        Assertions.assertEquals(Format.GREEN + "meow", Format.stripFormatting(Format.GREEN + "meow"));
        Assertions.assertEquals("purr", Format.stripFormatting(Format.BOLD + "purr"));
    }

    /**
     * Tests color validity.
     */
    @Test
    public void validColors() {
        for (Format format : Format.values()) {
            if (format.isColor()) {
                Assertions.assertEquals((format.getColorChar() & 15), format.getColorChar(), "Invalid IRCFormat color char " + format.name());
            } else {
                Assertions.assertEquals(-1, format.getColorChar(), "Invalid IRCFormat format " + format.name());
            }
        }
    }
}
