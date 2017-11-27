package org.kitteh.irc.client.library.util;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Test the pants off the CTCP conversion.
 */
public class CTCPUtilTest {
    private static final String CONVERTED_1 = "\u0001Meow\u0016n\u0016rMe\u00160w\\aMeow\u0016\u0016\\\\\u0001";
    private static final String SNIP_1 = CONVERTED_1 + "Meow";
    private static final String UNCONVERTED_1 = "Meow\n\rMe\u0000w\u0001Meow\u0016\\";

    private static final String CONVERTED_2 = "\u0001Meow\u0001";
    private static final String UNCONVERTED_2 = "Meow";

    /**
     * Tests converting from CTCP.
     */
    @Test
    public void fromCTCP() {
        Assert.assertEquals(UNCONVERTED_1, CTCPUtil.fromCTCP(CONVERTED_1));
    }

    /**
     * Tests converting from CTCP and back.
     */
    @Test
    public void thereAndBackAgain() {
        Assert.assertEquals(UNCONVERTED_1, CTCPUtil.fromCTCP(CTCPUtil.toCTCP(UNCONVERTED_1))); // For fun
    }

    /**
     * Tests the conversion for snipping after the first 0x01 grouping.
     */
    @Test
    public void snip() {
        Assert.assertEquals(UNCONVERTED_1, CTCPUtil.fromCTCP(SNIP_1));
    }

    /**
     * Tests converting to CTCP.
     */
    @Test
    public void toCTCP() {
        Assert.assertEquals(CONVERTED_1, CTCPUtil.toCTCP(UNCONVERTED_1));
    }

    /**
     * Tests isCTCP matcher with positive result.
     */
    @Test
    public void isCTCPTrue() {
        Assert.assertTrue(CTCPUtil.isCTCP(CONVERTED_1));
    }

    /**
     * Tests isCTCP matcher with positive result.
     */
    @Test
    public void isCTCPFalse() {
        Assert.assertFalse(CTCPUtil.isCTCP(UNCONVERTED_1));
    }

    /**
     * Tests conversion where no escaping is necessary.
     */
    @Test
    public void noEscapeToCTCP() {
        Assert.assertEquals(CONVERTED_2, CTCPUtil.toCTCP(UNCONVERTED_2));
    }

    /**
     * Tests conversion where no escaping is necessary.
     */
    @Test
    public void noEscapeFromCTCP() {
        Assert.assertEquals(UNCONVERTED_2, CTCPUtil.fromCTCP(CONVERTED_2));
    }

    /**
     * Private constructors are fun!
     */
    @Test
    public void testConstruction() {
        try {
            Constructor<CTCPUtil> constructor = CTCPUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new AssertionError("Halp", e);
        }
    }
}
