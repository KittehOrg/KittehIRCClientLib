package org.kitteh.irc.client.library;

import org.junit.Assert;
import org.junit.Test;

/**
 * Make sure nothing has gone horribly wrong at the format factory.
 */
public class IRCFormatTest {
    @Test
    public void format() {
        String colorChar = IRCFormat.COLOR_CHAR + "";
        for (IRCFormat format : IRCFormat.values()) {
            Assert.assertTrue("Color format with wrong length: " + format.name(), !format.toString().startsWith(colorChar) || format.toString().length() == 3);
        }
    }
}