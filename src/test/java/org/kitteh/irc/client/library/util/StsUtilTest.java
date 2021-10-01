package org.kitteh.irc.client.library.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.feature.sts.StsPolicy;

/**
 * Tests the STSUtil class.
 */
public class StsUtilTest {
    /**
     * Test the parseSeparatedKeyValueString function.
     */
    @Test
    public void testParseSeparatedKeyValueString() {
        String myStr = "foo,bar=cat,kitten=dog";
        StsPolicy retVal = StsUtil.getStsPolicyFromString(",", myStr);
        Assertions.assertTrue(retVal.getFlags().contains("foo"));
        Assertions.assertTrue(retVal.getOptions().containsKey("bar"));
        Assertions.assertTrue(retVal.getOptions().containsKey("kitten"));
        Assertions.assertFalse(retVal.getOptions().containsKey("foo"));

        Assertions.assertEquals(retVal.getOptions().get("kitten"), "dog");
    }

    /**
     * Tests parseSeparatedKeyValueString with invalid (null) input.
     */
    @Test
    public void testParseSeparatedKeyValueStringWithNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> StsUtil.getStsPolicyFromString(",", null));
    }
}
