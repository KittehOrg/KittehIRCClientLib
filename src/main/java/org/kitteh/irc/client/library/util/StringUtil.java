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

/**
 * String tools!
 */
public final class StringUtil {
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
     * 1 or an index less than 0
     */
    public static String combineSplit(String[] split, int start, int length, String delimiter) {
        Sanity.nullCheck((Object) split, "Cannot combine a null array");
        Sanity.truthiness(start >= 0, "Negative array indexes are not valid");
        Sanity.truthiness(length > 0, "Cannot combine less than one element of an array");

        final StringBuilder builder = new StringBuilder();
        for (int x = start; (x < split.length) && (x < (start + length)); x++) {
            builder.append(split[x]).append(delimiter);
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - delimiter.length());
        }
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
    public static String combineSplit(String[] split, int start) {
        Sanity.nullCheck((Object) split, "Cannot combine a null array");
        return StringUtil.combineSplit(split, start, split.length - start, " ");
    }
}