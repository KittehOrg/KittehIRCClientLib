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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Do you know the definition of sanity?
 */
public final class Sanity {
    private Sanity() {
    }

    /**
     * Checks if an object is null.
     *
     * @param object object to check
     * @param failMessage message to throw
     * @param <Type> type of object checked
     * @return the sane object
     * @throws IllegalArgumentException if the object is null
     */
    @Nonnull
    public static <Type> Type nullCheck(@Nullable Type object, @Nonnull String failMessage) {
        if (object == null) {
            throw new IllegalArgumentException(failMessage);
        }
        return object;
    }

    /**
     * Checks if an array is null or contains null elements.
     *
     * @param array array to check
     * @param failMessage message to throw
     * @param <Type> type of object checked
     * @return the sane object
     * @throws IllegalArgumentException if null or contains null elements
     */
    @Nonnull
    public static <Type> Type[] nullCheck(@Nullable Type[] array, @Nonnull String failMessage) {
        Sanity.nullCheck((Object) array, failMessage);
        for (Object element : array) {
            Sanity.nullCheck(element, failMessage);
        }
        return array;
    }

    /**
     * Checks if a boolean is true.
     *
     * @param bool value to test
     * @param failMessage message to throw
     * @throws IllegalArgumentException if false
     */
    public static void truthiness(boolean bool, @Nonnull String failMessage) {
        if (!bool) {
            throw new IllegalArgumentException(failMessage);
        }
    }

    /**
     * Checks if a message contains CR, LF, or NUL.
     *
     * @param message message to check
     * @return the safe message
     * @throws IllegalArgumentException if found
     */
    @Nonnull
    public static String safeMessageCheck(@Nonnull String message) {
        return Sanity.safeMessageCheck(message, "Message");
    }

    /**
     * Checks if a string contains CR, LF, or NUL.
     *
     * @param message string to check
     * @param name name of the string
     * @return the safe message
     * @throws IllegalArgumentException if found
     */
    @Nonnull
    public static String safeMessageCheck(@Nullable String message, @Nonnull String name) {
        Sanity.nullCheck(message, name + " cannot be null");
        for (char ch : message.toCharArray()) {
            if ((ch == '\n') || (ch == '\r') || (ch == '\0')) {
                throw new IllegalArgumentException(name + " cannot contain CR, LF, or NUL");
            }
        }
        return message;
    }
}
