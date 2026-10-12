package org.kitteh.irc.client.library.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Test the pants off the CTCP conversion.
 */
public class CtcpUtilTest {
    // Backslashes and 0x16 (reverse formatting) are not quoting characters
    private static final String UNCONVERTED_1 = "ACTION opens C:\\Users \u0016reversed\u0016 \\a \\\\ \u0016n";
    private static final String CONVERTED_1 = "\u0001" + UNCONVERTED_1 + "\u0001";
    private static final String SNIP_1 = CONVERTED_1 + "Meow";
    private static final String NO_TRAILING_1 = "\u0001" + UNCONVERTED_1;

    private static final String CONVERTED_2 = "\u0001Meow\u0001";
    private static final String UNCONVERTED_2 = "Meow";

    /**
     * Tests converting from CTCP.
     */
    @Test
    public void fromCTCP() {
        Assertions.assertEquals(UNCONVERTED_1, CtcpUtil.fromCtcp(CONVERTED_1));
    }

    /**
     * Tests converting from CTCP and back.
     */
    @Test
    public void thereAndBackAgain() {
        Assertions.assertEquals(UNCONVERTED_1, CtcpUtil.fromCtcp(CtcpUtil.toCtcp(UNCONVERTED_1))); // For fun
    }

    /**
     * Tests the conversion for snipping after the first 0x01 grouping.
     */
    @Test
    public void snip() {
        Assertions.assertEquals(UNCONVERTED_1, CtcpUtil.fromCtcp(SNIP_1));
    }

    /**
     * Tests converting from CTCP without the optional trailing delimiter.
     */
    @Test
    public void noTrailingDelimiter() {
        Assertions.assertEquals(UNCONVERTED_1, CtcpUtil.fromCtcp(NO_TRAILING_1));
    }

    /**
     * Tests converting from an empty CTCP message.
     */
    @Test
    public void emptyFromCTCP() {
        Assertions.assertEquals("", CtcpUtil.fromCtcp("\u0001"));
        Assertions.assertEquals("", CtcpUtil.fromCtcp("\u0001\u0001"));
    }

    /**
     * Tests converting to CTCP.
     */
    @Test
    public void toCTCP() {
        Assertions.assertEquals(CONVERTED_1, CtcpUtil.toCtcp(UNCONVERTED_1));
    }

    /**
     * Tests that characters which can't be sent are rejected.
     */
    @Test
    public void toCTCPRejects() {
        for (String bad : new String[]{"Me\u0001ow", "Me\now", "Me\row", "Me\u0000ow"}) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> CtcpUtil.toCtcp(bad));
        }
    }

    /**
     * Tests isCtcp matcher with positive result.
     */
    @Test
    public void isCTCPTrue() {
        Assertions.assertTrue(CtcpUtil.isCtcp(CONVERTED_1));
    }

    /**
     * Tests isCtcp matcher with negative result.
     */
    @Test
    public void isCTCPFalse() {
        Assertions.assertFalse(CtcpUtil.isCtcp(UNCONVERTED_1));
        Assertions.assertFalse(CtcpUtil.isCtcp(""));
    }

    /**
     * Tests conversion of a plain message.
     */
    @Test
    public void noEscapeToCTCP() {
        Assertions.assertEquals(CONVERTED_2, CtcpUtil.toCtcp(UNCONVERTED_2));
    }

    /**
     * Tests conversion of a plain message.
     */
    @Test
    public void noEscapeFromCTCP() {
        Assertions.assertEquals(UNCONVERTED_2, CtcpUtil.fromCtcp(CONVERTED_2));
    }

    /**
     * Private constructors are fun!
     */
    @Test
    public void testConstruction() {
        try {
            Constructor<CtcpUtil> constructor = CtcpUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new AssertionError("Halp", e);
        }
    }
}
