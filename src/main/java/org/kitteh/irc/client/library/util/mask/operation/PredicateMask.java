/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.irc.client.library.util.mask.operation;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;
import org.kitteh.irc.client.library.util.mask.Mask;

import java.util.function.Predicate;

/**
 * A wrapper around a {@link Predicate}.
 */
public final class PredicateMask implements Mask {
    /**
     * Gets a wrapper around {@code predicate}.
     *
     * @param predicate the predicate
     * @return the mask
     */
    public static @NonNull Mask forPredicate(final @NonNull Predicate<? super User> predicate) {
        Sanity.nullCheck(predicate, "predicate");
        return (predicate instanceof Mask) ? (Mask) predicate : new PredicateMask(predicate);
    }

    private final Predicate<? super User> predicate;

    /**
     * TODO
     * @param predicate
     */
    public PredicateMask(final @NonNull Predicate<? super User> predicate) {
        this.predicate = Sanity.nullCheck(predicate, "predicate");
    }

    @Override
    public boolean test(final @NonNull User user) {
        return this.predicate.test(user);
    }

    @Override
    public boolean test(final @NonNull String string) {
        return (this.predicate instanceof Mask) && ((Mask) this.predicate).test(string);
    }

    @Override
    public String toString() {
        return new ToStringer(this).add("predicate", this.predicate).toString();
    }
}
