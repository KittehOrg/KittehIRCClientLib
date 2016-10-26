package org.kitteh.irc.client.library.util;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;

/**
 * Tests the StringUtil class.
 */
public class StringUtilTest {
    /**
     * Tests combining splits.
     */
    @Test
    public void combineSplitWithSpaces() {
        Assert.assertEquals("item one two", StringUtil.combineSplit(new String[]{"item", "one", "two"}, 0));
        Assert.assertEquals("one two", StringUtil.combineSplit(new String[]{"item", "one", "two"}, 1));
    }

    /**
     * Tests combining splits with a custom delimiter.
     */
    @Test
    public void combineSplitCustomDelimiter() {
        Assert.assertEquals("one  two", StringUtil.combineSplit(new String[]{"item", "one", "two", "three"}, 1, 2, "  "));
    }

    /**
     * Tests an empty array.
     */
    @Test(expected = IllegalArgumentException.class)
    public void combineSplitEmpty() {
        StringUtil.combineSplit(new String[]{}, 0);
    }

    /**
     * Tests negative index.
     */
    @Test(expected = IllegalArgumentException.class)
    public void combineSplitNegative() {
        StringUtil.combineSplit(new String[]{"one"}, -1);
    }

    /**
     * Why are there so many tests about rainbows?
     */
    @Test
    public void rainbowSuccess() {
        StringUtil.makeRainbow("La da da dee \t da da doo");
    }

    /**
     * And what happens if one fails?
     */
    @Test(expected = IllegalArgumentException.class)
    public void rainbowNullMessage() {
        StringUtil.makeRainbow(null);
    }

    /**
     * Rainbows are Strings, but just for good input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void rainbowNullOrder() {
        StringUtil.makeRainbow("La da de dee da dee da doo", null);
    }

    /**
     * And users are hard to predict.
     */
    @Test(expected = IllegalArgumentException.class)
    public void rainbowNotAColor() {
        StringUtil.makeRainbow("Someday we'll find it, the rainbow test coverage, the coders, the users, and me.", new Format[]{Format.UNDERLINE});
    }

    /**
     * Tests the private constructor.
     *
     * @throws Exception when it goes bad
     */
    @Test
    public void testConstructorIsPrivate() throws Exception {
        Constructor<StringUtil> constructor = StringUtil.class.getDeclaredConstructor();
        Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    /**
     * Test the parseSeparatedKeyValueString function.
     */
    @Test
    public void testParseSeparatedKeyValueString() {
        String myStr = "foo,bar=cat,kitten=dog";
        Map<String, Optional<String>> retVal = StringUtil.parseSeparatedKeyValueString(",", myStr);
        Assert.assertTrue(retVal.containsKey("foo"));
        Assert.assertTrue(retVal.containsKey("bar"));
        Assert.assertTrue(retVal.containsKey("kitten"));
        Assert.assertFalse(retVal.get("foo").isPresent());

        Assert.assertTrue(retVal.get("kitten").isPresent());
        Assert.assertEquals(retVal.get("kitten").get(), "dog");
    }

    /**
     * Tests parseSeparatedKeyValueString with invalid (null) input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseSeparatedKeyValueStringWithNull() {
        StringUtil.parseSeparatedKeyValueString(",", null);
    }
}
