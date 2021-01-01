/*
 * * Copyright (C) 2013-2021 Matt Baxter https://kitteh.org
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
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A toString helper.
 */
public class ToStringer {
    private final List<Pair<String, Object>> list = new ArrayList<>();
    private final String name;

    /**
     * Creates a toString helper.
     *
     * @param object object that is being toString'd
     */
    public ToStringer(@NonNull Object object) {
        Sanity.nullCheck(object, "Object");
        this.name = object.getClass().getSimpleName();
    }

    /**
     * Adds an object.
     *
     * @param name name of the object
     * @param object object to add
     * @return this instance
     */
    public @NonNull ToStringer add(@NonNull String name, @Nullable Object object) {
        Sanity.nullCheck(name, "Name");
        this.list.add(new Pair<>(name, object));
        return this;
    }

    /**
     * Adds a boolean.
     *
     * @param name name of the boolean
     * @param b boolean to add
     * @return this instance
     */
    public @NonNull ToStringer add(@NonNull String name, boolean b) {
        this.add(name, String.valueOf(b));
        return this;
    }

    /**
     * Adds a byte.
     *
     * @param name name of the byte
     * @param b byte to add
     * @return this instance
     */
    public @NonNull ToStringer add(@NonNull String name, byte b) {
        this.add(name, String.valueOf(b));
        return this;
    }

    /**
     * Adds a char.
     *
     * @param name name of the char
     * @param c char to add
     * @return this instance
     */
    public @NonNull ToStringer add(@NonNull String name, char c) {
        this.add(name, String.valueOf(c));
        return this;
    }

    /**
     * Adds a double.
     *
     * @param name name of the double
     * @param d double to add
     * @return this instance
     */
    public @NonNull ToStringer add(@NonNull String name, double d) {
        this.add(name, String.valueOf(d));
        return this;
    }

    /**
     * Adds a float.
     *
     * @param name name of the float
     * @param f float to add
     * @return this instance
     */
    public @NonNull ToStringer add(@NonNull String name, float f) {
        this.add(name, String.valueOf(f));
        return this;
    }

    /**
     * Adds an int.
     *
     * @param name name of the int
     * @param i int to add
     * @return this instance
     */
    public @NonNull ToStringer add(@NonNull String name, int i) {
        this.add(name, String.valueOf(i));
        return this;
    }

    /**
     * Adds a long.
     *
     * @param name name of the long
     * @param l long to add
     * @return this instance
     */
    public @NonNull ToStringer add(@NonNull String name, long l) {
        this.add(name, String.valueOf(l));
        return this;
    }

    /**
     * Adds a short.
     *
     * @param name name of the short
     * @param s short to add
     * @return this instance
     */
    public @NonNull ToStringer add(@NonNull String name, short s) {
        this.add(name, String.valueOf(s));
        return this;
    }

    @Override
    public @NonNull String toString() {
        StringBuilder builder = new StringBuilder(this.name.length() + (this.list.size() * 10));
        builder.append(this.name).append(" (");
        boolean first = true;
        for (Pair<String, Object> pair : this.list) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append(pair.getLeft()).append('=');
            if ((pair.getRight() != null) && pair.getRight().getClass().isArray()) {
                String arr = Arrays.deepToString(new Object[]{pair.getRight()});
                builder.append(arr, 1, arr.length() - 1);
            } else {
                builder.append(pair.getRight());
            }
        }
        builder.append(')');
        return builder.toString();
    }
}
