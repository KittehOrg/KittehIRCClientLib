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
        public @NonNull List<String> split(@NonNull String message, int size) {
            Sanity.nullCheck(message, "Message");
            Sanity.truthiness(size > 0, "Size must be positive");
            List<String> list = new ArrayList<>();
            if (this.encodedLength(message) <= size) {
                list.add(message);
                return list;
            }
            StringBuilder builder = new StringBuilder(size);
            for (String word : message.split(" ")) {
                int builderLen = this.encodedLength(builder);
                if ((builderLen + this.encodedLength(word) + ((builderLen == 0) ? 0 : 1)) > size) {
                    if ((word.length() > size) && ((builderLen + 1) < size)) {
                        if (builderLen > 0) {
                            builder.append(' ');
                            builderLen++;
                        }
                        int cut = size - builderLen;
                        builder.append(word, 0, cut);
                        word = word.substring(cut);
                    }
                    list.add(builder.toString().trim());
                    builder.setLength(0);
                    while (this.encodedLength(word) > size) {
                        list.add(word.substring(0, size));
                        word = word.substring(size);
                    }
                }
                if (this.encodedLength(builder) > 0) {
                    builder.append(' ');
                }
                builder.append(word);
            }
            if (this.encodedLength(builder) > 0) {
                list.add(builder.toString().trim());
            }
            return list;
        }

        /*
         * The below two methods are from Guava's Utf8 class, licensed Apache 2.0 (see NOTICE file for more)
         * As this will always be small with minimal consequence, exceptions for large or malformed text are stripped.
         */

        /**
         * Returns the number of bytes in the UTF-8-encoded form of {@code sequence}. For a string, this
         * method is equivalent to {@code string.getBytes(UTF_8).length}, but is more efficient in both
         * time and space.
         */
        private int encodedLength(CharSequence sequence) {
            // Warning to maintainers: this implementation is highly optimized.
            int utf16Length = sequence.length();
            int utf8Length = utf16Length;
            int i = 0;

            // This loop optimizes for pure ASCII.
            while (i < utf16Length && sequence.charAt(i) < 0x80) {
                i++;
            }

            // This loop optimizes for chars less than 0x800.
            for (; i < utf16Length; i++) {
                char c = sequence.charAt(i);
                if (c < 0x800) {
                    utf8Length += ((0x7f - c) >>> 31); // branch free!
                } else {
                    utf8Length += this.encodedLengthGeneral(sequence, i);
                    break;
                }
            }

            return utf8Length;
        }

        private int encodedLengthGeneral(CharSequence sequence, int start) {
            int utf16Length = sequence.length();
            int utf8Length = 0;
            for (int i = start; i < utf16Length; i++) {
                char c = sequence.charAt(i);
                if (c < 0x800) {
                    utf8Length += (0x7f - c) >>> 31; // branch free!
                } else {
                    utf8Length += 2;
                    if (Character.isSurrogate(c)) {
                        i++;
                    }
                }
            }
            return utf8Length;
        }
    }

    /**
     * Splits a message into items no longer than the size limit.
     *
     * @param message message to split
     * @param size    size limit per returned string
     * @return split up string
     * @throws IllegalArgumentException if size is less than 1 or if
     *                                  message is null
     */
    @NonNull List<String> split(@NonNull String message, int size);
}
