package org.kitteh.irc.client.library.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Tests the StringUtil class.
 */
public class StringUtilTest {
    /**
     * Tests combining splits.
     */
    @Test
    public void combineSplitWithSpaces() {
        Assertions.assertEquals("item one two", StringUtil.combineSplit(new String[]{"item", "one", "two"}, 0));
        Assertions.assertEquals("one two", StringUtil.combineSplit(new String[]{"item", "one", "two"}, 1));
    }

    /**
     * Tests combining splits with a custom delimiter.
     */
    @Test
    public void combineSplitCustomDelimiter() {
        Assertions.assertEquals("one  two", StringUtil.combineSplit(new String[]{"item", "one", "two", "three"}, 1, 2, "  "));
    }

    /**
     * Tests an empty array.
     */
    @Test
    public void combineSplitEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> StringUtil.combineSplit(new String[]{}, 0));
    }

    /**
     * Tests negative index.
     */
    @Test
    public void combineSplitNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> StringUtil.combineSplit(new String[]{"one"}, -1));
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
    @Test
    public void rainbowNullMessage() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> StringUtil.makeRainbow(null));
    }

    /**
     * Rainbows are Strings, but just for good input.
     */
    @Test
    public void rainbowNullOrder() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> StringUtil.makeRainbow("La da de dee da dee da doo", null));
    }

    /**
     * And users are hard to predict.
     */
    @Test
    public void rainbowNotAColor() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> StringUtil.makeRainbow("Someday we'll find it, the rainbow test coverage, the coders, the users, and me.", new Format[]{Format.UNDERLINE}));
    }

    /**
     * Tests the private constructor.
     *
     * @throws Exception when it goes bad
     */
    @Test
    public void testConstructorIsPrivate() throws Exception {
        Constructor<StringUtil> constructor = StringUtil.class.getDeclaredConstructor();
        Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
