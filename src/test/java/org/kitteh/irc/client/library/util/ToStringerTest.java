package org.kitteh.irc.client.library.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests ToStringer.
 */
public class ToStringerTest {
    /**
     * Tests a valid toStringer input.
     */
    @Test
    public void toStringer() {
        Assert.assertNotNull(new ToStringer(this)
                .add("boolean", true)
                .add("byte", (byte) 0x00)
                .add("char", 'c')
                .add("double", 1D)
                .add("float", 2F)
                .add("int", 3)
                .add("long", 4L)
                .add("object", new Object())
                .add("array", new String[]{"arr", "I'm a pirate"})
                .add("short", (short) 5)
                .add("null", null)
                .toString());
    }

    /**
     * Tests a null addition parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void toStringerNullAddition() {
        new ToStringer(this).add(null, "test");
    }

    /**
     * Tests a null constructor parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void toStringerNullConstructor() {
        new ToStringer(null);
    }
}
