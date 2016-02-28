package org.kitteh.irc.client.library;

import org.junit.Assert;
import org.junit.Test;
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
        Assert.assertEquals("\u000309,01", Format.GREEN.withBackground(Format.BLACK));
    }

    /**
     * Tests invalid background input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void backgroundNonColorBackground() {
        Format.GREEN.withBackground(Format.BOLD);
    }

    /**
     * Tests invalid background input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void backgroundNonColorForeground() {
        Format.BOLD.withBackground(Format.GREEN);
    }

    /**
     * Tests invalid background input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void backgroundNull() {
        Format.GREEN.withBackground(null);
    }

    /**
     * Tests for colors of incorrect length.
     */
    @Test
    public void format() {
        String colorChar = Format.COLOR_CHAR + "";
        for (Format format : Format.values()) {
            Assert.assertTrue("Color format with wrong length: " + format.name(), !format.toString().startsWith(colorChar) || (format.toString().length() == 3));
        }
    }

    /**
     * Tests color stripper.
     */
    @Test
    public void stripColor() {
        Assert.assertEquals(Format.stripColor(Format.GREEN + "meow"), "meow");
        Assert.assertEquals(Format.stripColor(Format.BOLD + "purr"), Format.BOLD + "purr");
    }

    /**
     * Tests format stripper.
     */
    @Test
    public void stripFormat() {
        Assert.assertEquals(Format.stripFormatting(Format.GREEN + "meow"), Format.GREEN + "meow");
        Assert.assertEquals(Format.stripFormatting(Format.BOLD + "purr"), "purr");
    }

    /**
     * Tests color validity.
     */
    @Test
    public void validColors() {
        for (Format format : Format.values()) {
            if (format.isColor()) {
                Assert.assertTrue("Invalid IRCFormat color char " + format.name(), (format.getColorChar() & 15) == format.getColorChar());
            } else {
                Assert.assertEquals("Invalid IRCFormat format " + format.name(), format.getColorChar(), -1);
            }
        }
    }
}
