package org.kitteh.irc.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the StringUtil class.
 */
public class StringUtilTest {
    @Test
    public void testCombineSplitWithSpaces() {
        Assert.assertEquals("item one two", StringUtil.combineSplit(new String[]{"item", "one", "two"}, 0));
        Assert.assertEquals("one two", StringUtil.combineSplit(new String[]{"item", "one", "two"}, 1));
    }

    @Test
    public void testCombineSplit() {
        Assert.assertEquals("one  two", StringUtil.combineSplit(new String[]{"item", "one", "two", "three"}, 1, 2, "  "));
    }
}