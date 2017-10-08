package org.kitteh.irc.client.library;

import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class TestUtil {
    /**
     * Assert that the provided {@link Optional} is present, and matches the expected value.
     *
     * @param optional optional
     * @param expected expected value
     * @param <T> type of value
     */
    public static <T> void assertOptionalEquals(@Nonnull final Optional<T> optional, @Nonnull final T expected) {
        if (!optional.isPresent()) {
            Assert.fail("Value is not present in the provided Optional");
        } else {
            Assert.assertEquals(expected, optional.get());
        }
    }

    public static <T> void assertContainsOnly(final OngoingStubbing<Collection<T>> stubbing, final Supplier<Collection<? extends T>> supplier, final List<? extends T> yes, final List<? extends T> no) {
        final List<T> all = new ArrayList<>(yes.size() + no.size());
        all.addAll(yes);
        all.addAll(no);
        stubbing.thenReturn(all);
        final Collection<? extends T> matches = supplier.get();
        Assert.assertTrue(matches.size() == yes.size());
        yes.forEach(entry -> Assert.assertTrue("should contain " + entry, matches.contains(entry)));
        no.forEach(entry -> Assert.assertFalse("should not contain " + entry, matches.contains(entry)));
    }

    @Test
    public void testPresentOptionalCorrectValue() {
        final Optional<String> optional = Optional.of("kittens");
        assertOptionalEquals(optional, "kittens");
    }

    @Test(expected = ComparisonFailure.class)
    public void testPresentOptionalWrongValue() {
        final Optional<String> optional = Optional.of("cats");
        assertOptionalEquals(optional, "kittens");
    }

    @Test(expected = AssertionError.class)
    public void testEmptyOptional() {
        final Optional<String> optional = Optional.empty();
        assertOptionalEquals(optional, "kittens");
    }
}
