package org.kitteh.irc.client.library.util;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals("purr", RiskyBusiness.assertSafe(input -> "purr", "meow"));
    }

    /**
     * Falls over.
     */
    @Test
    public void calculatedFailure() {
        Assertions.assertThrows(AssertionError.class, () ->
            RiskyBusiness.assertSafe(input -> {
                throw new Exception();
            }, "meow")
        );
    }

    /**
     * Test the private constructor.
     *
     * @throws Exception if something goes horrifically wrong
     */
    @Test
    public void testConstructorIsPrivate() throws Exception {
        Constructor<RiskyBusiness> constructor = RiskyBusiness.class.getDeclaredConstructor();
        Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
