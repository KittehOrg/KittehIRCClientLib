package org.kitteh.irc.client.library.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Do you know the definition of testing?
 */
public class SanityTest {
    /**
     * Tests nullCheck with valid info.
     */
    @Test
    public void nullCheckPass() {
        Sanity.nullCheck(new Object(), "Pass failed!");
    }

    /**
     * Tests nullCheck with invalid info.
     */
    @Test
    public void nullCheckFail() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sanity.nullCheck((Object) null, "Failure!"));
    }

    /**
     * Tests nullCheck with valid array info.
     */
    @Test
    public void nullCheckArrayPass() {
        Sanity.nullCheck(new Object[]{"meow", "Meow"}, "Pass failed!");
    }

    /**
     * Tests nullCheck with invalid array info.
     */
    @Test
    public void nullCheckArrayFail() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sanity.nullCheck((Object[]) null, "Failure!"));
    }

    /**
     * Tests nullCheck with invalid array info.
     */
    @Test
    public void nullCheckArrayFailElement() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sanity.nullCheck(new Object[]{"meow", null}, "Failure!"));
    }

    /**
     * Tests truthiness with valid info.
     */
    @Test
    public void truthPass() {
        Sanity.truthiness(true, "Pass");
    }

    /**
     * Tests truthiness with invalid info.
     */
    @Test
    public void truthFail() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sanity.truthiness(false, "Fail"));
    }

    /**
     * Tests safeMessageCheck with valid info.
     */
    @Test
    public void safeMessagePass() {
        Sanity.safeMessageCheck("Meow");
    }

    /**
     * Tests safeMessageCheck with invalid info.
     */
    @Test
    public void safeMessageFailNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sanity.safeMessageCheck(null));
    }

    /**
     * Tests safeMessageCheck with invalid info.
     */
    @Test
    public void safeMessageFailLF() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sanity.safeMessageCheck("Me\now"));
    }

    /**
     * Tests safeMessageCheck with invalid info.
     */
    @Test
    public void safeMessageFailCR() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sanity.safeMessageCheck("Me\row"));
    }

    /**
     * Tests safeMessageCheck with invalid info.
     */
    @Test
    public void safeMessageFailNul() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sanity.safeMessageCheck("Me\0ow"));
    }

    @Test
    public void noSpacesPass() {
        Sanity.noSpaces("Cat", "");
    }

    @Test
    public void noSpacesFail() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sanity.noSpaces("Cat ", ""));
    }

    /**
     * Tests the private constructor.
     *
     * @throws Exception if oops
     */
    @Test
    public void testConstructorIsPrivate() throws Exception {
        Constructor<Sanity> constructor = Sanity.class.getDeclaredConstructor();
        Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
