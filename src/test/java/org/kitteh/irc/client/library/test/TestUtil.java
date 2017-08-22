package org.kitteh.irc.client.library.test;

import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Optional;

public final class TestUtil {

    /**
     * Assert that the provided {@link Optional} is present, and matches the expected value.
     *
     * @param optional optional
     * @param expected expected value
     * @param <T> type of value
     */
    public static <T> void assertOptionalEquals(@Nonnull Optional<T> optional, @Nonnull T expected) {
        if (!optional.isPresent()) {
            Assert.fail("Value is not present in the provided Optional");
        } else {
            Assert.assertEquals(expected, optional.get());
        }
    }

    @Test
    public void testPresentOptionalCorrectValue() {
        Optional<String> optional = Optional.of("kittens");
        assertOptionalEquals(optional, "kittens");
    }

    @Test(expected = ComparisonFailure.class)
    public void testPresentOptionalWrongValue() {
        Optional<String> optional = Optional.of("cats");
        assertOptionalEquals(optional, "kittens");
    }

    @Test(expected = AssertionError.class)
    public void testEmptyOptional() {
        Optional<String> optional = Optional.empty();
        assertOptionalEquals(optional, "kittens");
    }
}
