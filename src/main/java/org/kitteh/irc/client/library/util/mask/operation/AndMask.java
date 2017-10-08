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

/**
 * A mask that requires both {@link AndMask#a} and {@link AndMask#b} to be
 * {@code true}.
 */
public final class AndMask implements Mask {
    private final Mask a;
    private final Mask b;

    /**
     * TODO
     * @param a
     * @param b
     */
    public AndMask(final @NonNull Mask a, final @NonNull Mask b) {
        this.a = Sanity.nullCheck(a, "a");
        this.b = Sanity.nullCheck(b, "b");
    }

    @Override
    public boolean test(final @NonNull User user) {
        return this.a.test(user) && this.b.test(user);
    }

    @Override
    public boolean test(final @NonNull String string) {
        return this.a.test(string) && this.b.test(string);
    }

    @Override
    public String toString() {
        return new ToStringer(this).add("a", this.a).add("b", this.b).toString();
    }
}
