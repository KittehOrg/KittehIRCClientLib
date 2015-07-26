/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A pair of objects!
 *
 * @param <Left> Type of the first object
 * @param <Right> Type of the second object
 */
public final class Pair<Left, Right> {
    private final Left left;
    private final Right right;

    /**
     * Constructs a pair of objects
     *
     * @param left first object
     * @param right second object
     */
    public Pair(@Nullable Left left, @Nullable Right right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Gets the first object of this pair.
     *
     * @return first object
     */
    @Nullable
    public Left getLeft() {
        return this.left;
    }

    /**
     * Gets the second object of this pair.
     *
     * @return second object
     */
    @Nullable
    public Right getRight() {
        return this.right;
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("left", this.left).add("right", this.right).toString();
    }
}