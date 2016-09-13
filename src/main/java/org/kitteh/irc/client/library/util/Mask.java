/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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

import org.kitteh.irc.client.library.element.User;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

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
    @Nonnull
    public static Mask fromString(@Nonnull String string) {
        Sanity.nullCheck(string, "String cannot be null");
        return new Mask(string);
    }

    private final Pattern pattern;
    private final String string;

    private Mask(@Nonnull String string) {
        this.pattern = StringUtil.wildcardToPattern(string);
        this.string = string;
    }

    /**
     * Gets the String representation of this mask.
     *
     * @return string
     */
    @Nonnull
    public String asString() {
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

    /**
     * Gets if a string matches this mask.
     *
     * @param string string to test
     * @return true if a match
     */
    public boolean matches(@Nonnull String string) {
        return this.pattern.matcher(string).matches();
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("string", this.string).toString();
    }
}
