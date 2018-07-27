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
package org.kitteh.irc.client.library.util;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * It slices, it dices, it breaks apart a message into a list of items
 * wherein each item is no longer than the defined size limit.
 */
@FunctionalInterface
public interface Cutter {
    /**
     * Cuts by words, unless word is too long.
     */
    class DefaultWordCutter implements Cutter {
        @Override
        public @NonNull List<String> split(@NonNull String message, @NonNegative int size) {
            Sanity.nullCheck(message, "Message cannot be null");
            Sanity.truthiness(size > 0, "Size must be positive");
            List<String> list = new ArrayList<>();
            if (message.length() <= size) {
                list.add(message);
                return list;
            }
            StringBuilder builder = new StringBuilder(size);
            for (String word : message.split(" ")) {
                if ((builder.length() + word.length() + ((builder.length() == 0) ? 0 : 1)) > size) {
                    if ((word.length() > size) && ((builder.length() + 1) < size)) {
                        if (builder.length() > 0) {
                            builder.append(' ');
                        }
                        int cut = size - builder.length();
                        builder.append(word.substring(0, cut));
                        word = word.substring(cut);
                    }
                    list.add(builder.toString().trim());
                    builder.setLength(0);
                    while (word.length() > size) {
                        list.add(word.substring(0, size));
                        word = word.substring(size);
                    }
                }
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(word);
            }
            if (builder.length() > 0) {
                list.add(builder.toString().trim());
            }
            return list;
        }
    }

    /**
     * Splits a message into items no longer than the size limit.
     *
     * @param message message to split
     * @param size size limit per returned string
     * @return split up string
     * @throws IllegalArgumentException if size is less than 1 or if
     * message is null
     */
    @NonNull List<String> split(@NonNull String message, @NonNegative int size);
}
