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
package org.kitteh.irc.client.library.feature;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kitteh.irc.client.library.util.Sanity;

import java.util.Locale;
import java.util.Optional;

/**
 * ISUPPORT CASEMAPPING.
 */
@NullMarked
public interface CaseMapping {
    record Ranged(char upperbound) implements CaseMapping {
        @Override
        public String toLowerCase(String input) {
            Sanity.nullCheck(input, "Input");
            char[] arr = input.toCharArray();
            for (int i = 0; i < arr.length; i++) {
                char c = arr[i];
                if ((c >= 'A') && (c <= this.upperbound)) {
                    arr[i] += (char) 32;
                }
            }
            return new String(arr);
        }
    }

    /**
     * A-Z become a-z
     */
    CaseMapping.Ranged ASCII = new CaseMapping.Ranged('Z');
    /**
     * A-Z become a-z, []^ become {}~
     */
    CaseMapping.Ranged RFC1459 = new CaseMapping.Ranged('^');
    /**
     * A-Z become a-z, [] become {}
     */
    CaseMapping.Ranged STRICT_RFC1459 = new CaseMapping.Ranged(']');

    default boolean areEqualIgnoringCase(String one,String two) {
        return this.toLowerCase(one).equals(this.toLowerCase(two));
    }

    /**
     * Converts a given String to lowercase per spec.
     *
     * @param input string to be lowercased
     * @return lowercased string
     * @throws IllegalArgumentException if input is null
     */
    String toLowerCase(String input);

    /**
     * Gets a CaseMapping by name, case-insensitive.
     *
     * @param name the name of the CaseMapping to get
     * @return the matching CaseMapping if one exists
     */
    static @NonNull Optional<CaseMapping> getByName(@Nullable String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(switch (name.toUpperCase(Locale.ROOT)) {
            case "ASCII" -> ASCII;
            case "RFC1459" -> RFC1459;
            case "STRICT-RFC1459" -> STRICT_RFC1459;
            default -> null;
        });
    }
}
