package org.kitteh.irc.client.library;

import org.junit.Assert;
import org.junit.Test;

/**
 * Make sure nothing has gone horribly wrong at the format factory.
 */
public class IRCFormatTest {
    /**
     * Tests valid background input.
     */
    @Test
    public void background() {
        Assert.assertEquals("\u000309,01", IRCFormat.GREEN.withBackground(IRCFormat.BLACK));
    }

    /**
     * Tests invalid background input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void backgroundNonColorBackground() {
        IRCFormat.GREEN.withBackground(IRCFormat.BOLD);
    }

    /**
     * Tests invalid background input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void backgroundNonColorForeground() {
        IRCFormat.BOLD.withBackground(IRCFormat.GREEN);
    }

    /**
     * Tests invalid background input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void backgroundNull() {
        IRCFormat.GREEN.withBackground(null);
    }

    /**
     * Tests for colors of incorrect length.
     */
    @Test
    public void format() {
        String colorChar = IRCFormat.COLOR_CHAR + "";
        for (IRCFormat format : IRCFormat.values()) {
            Assert.assertTrue("Color format with wrong length: " + format.name(), !format.toString().startsWith(colorChar) || (format.toString().length() == 3));
        }
    }

    /**
     * Tests color stripper.
     */
    @Test
    public void stripColor() {
        Assert.assertEquals(IRCFormat.stripColor(IRCFormat.GREEN + "meow"), "meow");
        Assert.assertEquals(IRCFormat.stripColor(IRCFormat.BOLD + "purr"), IRCFormat.BOLD + "purr");
    }

    /**
     * Tests format stripper.
     */
    @Test
    public void stripFormat() {
        Assert.assertEquals(IRCFormat.stripFormatting(IRCFormat.GREEN + "meow"), IRCFormat.GREEN + "meow");
        Assert.assertEquals(IRCFormat.stripFormatting(IRCFormat.BOLD + "purr"), "purr");
    }

    /**
     * Tests color validity.
     */
    @Test
    public void validColors() {
        for (IRCFormat format : IRCFormat.values()) {
            if (format.isColor()) {
                Assert.assertTrue("Invalid IRCFormat color char " + format.name(), (format.getColorChar() & 15) == format.getColorChar());
            } else {
                Assert.assertEquals("Invalid IRCFormat format " + format.name(), format.getColorChar(), -1);
            }
        }
    }
}
