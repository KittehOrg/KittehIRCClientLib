package org.kitteh.irc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the pants off the CTCP conversion.
 */
public class CTCPUtilTest {
    private static final String CONVERTED = "\u0001Meow\u0016n\u0016rMe\u00160w\\aMeow\u0016\u0016\\\\\u0001";
    private static final String SNIP = CONVERTED + "Meow";
    private static final String UNCONVERTED = "Meow\n\rMe\u0000w\u0001Meow\u0016\\";

    @Test
    public void fromCTCP() {
        Assert.assertEquals(UNCONVERTED, CTCPUtil.fromCTCP(CONVERTED));
    }

    @Test
    public void thereAndBackAgain() {
        Assert.assertEquals(UNCONVERTED, CTCPUtil.fromCTCP(CTCPUtil.toCTCP(UNCONVERTED))); // For fun
    }

    @Test
    public void snip() {
        Assert.assertEquals(UNCONVERTED, CTCPUtil.fromCTCP(SNIP));
    }

    @Test
    public void toCTCP() {
        Assert.assertEquals(CONVERTED, CTCPUtil.toCTCP(UNCONVERTED));
    }
}