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

import org.jspecify.annotations.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ClientLinked;
import org.kitteh.irc.client.library.feature.CaseMapping;

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
    public static @NonNull String combineSplit(@NonNull String[] split, int start, int length, @NonNull String delimiter) {
        Sanity.nullCheck((Object) split, "Cannot combine a null array");
        Sanity.nullCheck(delimiter, "Delimiter");
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
    public static @NonNull String combineSplit(@NonNull String[] split, int start) {
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
    public static @NonNull String makeRainbow(@NonNull String message) {
        return StringUtil.makeRainbow(message, StringUtil.DEFAULT_RAINBOW);
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
    public static @NonNull String makeRainbow(@NonNull String message, @NonNull Format[] colorOrder) {
        Sanity.safeMessageCheck(message);
        Sanity.nullCheck(colorOrder, "Color order");
        for (Format format : colorOrder) {
            if (!format.isColor()) {
                throw new IllegalArgumentException("Color order must contain only colors");
            }
        }

        StringBuilder builder = new StringBuilder(message.length() * 3);
        int count = 0;
        for (char c : message.toCharArray()) {
            if (!((c == ' ') || (c == '\t'))) {
                builder.append(colorOrder[count++ % colorOrder.length]);
            }
            builder.append(c);
        }
        return builder.toString();
    }

    /**
     * Converts a given String to lowercase per spec.
     *
     * @param linked the client provider
     * @param input string to be lowercased
     * @return lowercased string
     * @throws IllegalArgumentException if client provider is null
     * @throws IllegalArgumentException if input is null
     * @see CaseMapping#toLowerCase
     */
    public static @NonNull String toLowerCase(@NonNull ClientLinked linked, @NonNull String input) {
        Sanity.nullCheck(linked, "ClientLinked");
        return StringUtil.toLowerCase(linked.getClient(), input);
    }

    /**
     * Converts a given String to lowercase per spec.
     *
     * @param client the client
     * @param input string to be lowercased
     * @return lowercased string
     * @throws IllegalArgumentException if client is null
     * @throws IllegalArgumentException if input is null
     * @see CaseMapping#toLowerCase
     */
    public static @NonNull String toLowerCase(@NonNull Client client, @NonNull String input) {
        Sanity.nullCheck(client, "Client");
        Sanity.nullCheck(input, "Input");
        return client.getServerInfo().getCaseMapping().toLowerCase(input);
    }

    /**
     * Converts all characters of a password to asterisks.
     *
     * @param password password
     * @return filtered password
     */
    @SuppressWarnings("ReplaceAllDot")
    public static @NonNull String filterPassword(@NonNull String password) {
        Sanity.nullCheck(password, "Password");
        return password.replaceAll(".", "*");
    }
}
