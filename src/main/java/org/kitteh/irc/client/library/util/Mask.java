/*
 * * Copyright (C) 2013-2025 Matt Baxter https://kitteh.org
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.element.User;

/**
 * Represents a mask that can match a {@link User}.
 */
public class Mask {
    /**
     * Creates a Mask from a given String.
     *
     * @param string string
     * @return mask from string
     */
    public static @NonNull Mask fromString(@NonNull String string) {
        return new Mask(Sanity.nullCheck(string, "String"));
    }

    private final String string;

    private Mask(@NonNull String string) {
        this.string = string;
    }

    /**
     * Gets the String representation of this mask.
     *
     * @return string
     */
    public @NonNull String asString() {
        return this.string;
    }

    @Override
    public int hashCode() {
        return (2 * this.string.hashCode()) + 5;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Mask) && ((Mask) o).string.equals(this.string);
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).add("string", this.string).toString();
    }
}
