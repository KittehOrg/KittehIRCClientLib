package org.kitteh.irc.client.library.util;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Tests the RiskyBusiness class.
 */
public class RiskyBusinessTest {
    /**
     * Functions.
     */
    @Test
    public void calculatedRisk() {
        Assert.assertEquals("purr", RiskyBusiness.assertSafe(input -> "purr", "meow"));
    }

    /**
     * Falls over.
     */
    @Test(expected = AssertionError.class)
    public void calculatedFailure() {
        RiskyBusiness.assertSafe(input -> {
            throw new Exception();
        }, "meow");
    }

    /**
     * Test the private constructor.
     *
     * @throws Exception if something goes horrifically wrong
     */
    @Test
    public void testConstructorIsPrivate() throws Exception {
        Constructor<RiskyBusiness> constructor = RiskyBusiness.class.getDeclaredConstructor();
        Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
