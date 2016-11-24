package org.kitteh.irc.client.library.util;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.feature.sts.STSPolicy;

public class STSUtilTest {

    /**
     * Test the parseSeparatedKeyValueString function.
     */
    @Test
    public void testParseSeparatedKeyValueString() {
        String myStr = "foo,bar=cat,kitten=dog";
        STSPolicy retVal = STSUtil.getSTSPolicyFromString(",", myStr);
        Assert.assertTrue(retVal.getFlags().contains("foo"));
        Assert.assertTrue(retVal.getOptions().containsKey("bar"));
        Assert.assertTrue(retVal.getOptions().containsKey("kitten"));
        Assert.assertFalse(retVal.getOptions().containsKey("foo"));

        Assert.assertEquals(retVal.getOptions().get("kitten"), "dog");
    }

    /**
     * Tests parseSeparatedKeyValueString with invalid (null) input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseSeparatedKeyValueStringWithNull() {
        STSUtil.getSTSPolicyFromString(",", null);
    }
}
