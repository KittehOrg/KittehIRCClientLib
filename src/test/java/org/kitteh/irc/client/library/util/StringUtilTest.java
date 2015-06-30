package org.kitteh.irc.client.library.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the StringUtil class.
 */
public class StringUtilTest {
    /**
     * Tests combining splits.
     */
    @Test
    public void testCombineSplitWithSpaces() {
        Assert.assertEquals("item one two", StringUtil.combineSplit(new String[]{"item", "one", "two"}, 0));
        Assert.assertEquals("one two", StringUtil.combineSplit(new String[]{"item", "one", "two"}, 1));
    }

    /**
     * Tests combining splits with a custom delimiter.
     */
    @Test
    public void testCombineSplit() {
        Assert.assertEquals("one  two", StringUtil.combineSplit(new String[]{"item", "one", "two", "three"}, 1, 2, "  "));
    }
}