/*
 * * Copyright (C) 2013-2014 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.util;

public final class StringUtil {
    public static String combineSplit(String[] split, int start, int length, String delimiter) {
        final StringBuilder builder = new StringBuilder();
        for (int x = start; (x < split.length) && (x < (start + length)); x++) {
            builder.append(split[x]).append(delimiter);
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - delimiter.length());
        }
        return builder.toString();
    }

    public static String combineSplit(String[] split, int start) {
        return StringUtil.combineSplit(split, start, split.length - start, " ");
    }

    public static String getNick(String fullname) {
        final int i = fullname.indexOf("!");
        return fullname.substring(0, i > 0 ? i : fullname.length());
    }
}