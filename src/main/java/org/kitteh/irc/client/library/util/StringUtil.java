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

import org.kitteh.irc.client.library.feature.Format;

import javax.annotation.Nonnull;

/**
 * String tools!
 */
public final class StringUtil {
    private static final Format[] DEFAULT_RAINBOW = {Format.RED, Format.BROWN, Format.OLIVE, Format.YELLOW, Format.DARK_GREEN, Format.GREEN, Format.TEAL, Format.BLUE, Format.MAGENTA, Format.PURPLE};

    private StringUtil() {
    }

    /**
     * Combines an array into a super string!
     * <p>
     * Invalid index or length, or a length too long for the array, will be
     * ignored.
     *
     * @param split the split array
     * @param start index at which to start
     * @param length how many elements to include
     * @param delimiter delimiter
     * @return the combined string
     * @throws IllegalArgumentException for a null array, a length less than
     * 1, an index less than 0, or a null delimiter
     */
    @Nonnull
    public static String combineSplit(@Nonnull String[] split, int start, int length, @Nonnull String delimiter) {
        Sanity.nullCheck((Object) split, "Cannot combine a null array");
        Sanity.nullCheck(delimiter, "Delimiter cannot be null");
        Sanity.truthiness(start >= 0, "Negative array indexes are not valid");
        Sanity.truthiness(length > 0, "Cannot combine less than one element of an array");

        final StringBuilder builder = new StringBuilder((5 + delimiter.length()) * length);
        for (int x = start; (x < split.length) && (x < (start + length)); x++) {
            builder.append(split[x]).append(delimiter);
        }
        builder.setLength(builder.length() - delimiter.length());
        return builder.toString();
    }

    /**
     * Combines an array into a space-delimited string from a starting index.
     * <p>
     * Invalid starting index will result
     *
     * @param split the split array
     * @param start index at which to start
     * @return the combined string
     * @throws IllegalArgumentException for null array or index less than 0
     */
    @Nonnull
    public static String combineSplit(@Nonnull String[] split, int start) {
        Sanity.nullCheck((Object) split, "Cannot combine a null array");
        return StringUtil.combineSplit(split, start, split.length - start, " ");
    }

    /**
     * Turns a message into a rainbow.
     *
     * @param message message to become rainbow
     * @return the colorful new message
     * @throws IllegalArgumentException for null message
     */
    @Nonnull
    public static String makeRainbow(@Nonnull String message) {
        return StringUtil.makeRainbow(message, DEFAULT_RAINBOW);
    }

    /**
     * Turns a message into a rainbow.
     *
     * @param message message to become rainbow
     * @param colorOrder order of colors to send
     * @return the colorful new message
     * @throws IllegalArgumentException for null parameters, null entries in
     * array, or non-color entries in array
     */
    @Nonnull
    public static String makeRainbow(@Nonnull String message, @Nonnull Format[] colorOrder) {
        Sanity.safeMessageCheck(message);
        Sanity.nullCheck(colorOrder, "Color order cannot be null");
        for (Format format : colorOrder) {
            if (!format.isColor()) {
                throw new IllegalArgumentException("Color order must contain only colors");
            }
        }

        StringBuilder builder = new StringBuilder(message.length() * 3);
        int count = 0;
        for (char c : message.toCharArray()) {
            if (!((c == ' ') || (c == '\t'))) {
                builder.append(colorOrder[count++ % colorOrder.length].toString());
            }
            builder.append(c);
        }
        return builder.toString();
    }
}
