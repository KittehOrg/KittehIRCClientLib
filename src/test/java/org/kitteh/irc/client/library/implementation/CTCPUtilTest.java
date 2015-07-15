package org.kitteh.irc.client.library.implementation;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.implementation.CTCPUtil;

/**
 * Test the pants off the CTCP conversion.
 */
public class CTCPUtilTest {
    private static final String CONVERTED = "\u0001Meow\u0016n\u0016rMe\u00160w\\aMeow\u0016\u0016\\\\\u0001";
    private static final String SNIP = CONVERTED + "Meow";
    private static final String UNCONVERTED = "Meow\n\rMe\u0000w\u0001Meow\u0016\\";

    /**
     * Tests converting from CTCP.
     */
    @Test
    public void fromCTCP() {
        Assert.assertEquals(UNCONVERTED, CTCPUtil.fromCTCP(CONVERTED));
    }

    /**
     * Tests converting from CTCP and back.
     */
    @Test
    public void thereAndBackAgain() {
        Assert.assertEquals(UNCONVERTED, CTCPUtil.fromCTCP(CTCPUtil.toCTCP(UNCONVERTED))); // For fun
    }

    /**
     * Tests the conversion for snipping after the first 0x01 grouping.
     */
    @Test
    public void snip() {
        Assert.assertEquals(UNCONVERTED, CTCPUtil.fromCTCP(SNIP));
    }

    /**
     * Tests converting to CTCP.
     */
    @Test
    public void toCTCP() {
        Assert.assertEquals(CONVERTED, CTCPUtil.toCTCP(UNCONVERTED));
    }
}