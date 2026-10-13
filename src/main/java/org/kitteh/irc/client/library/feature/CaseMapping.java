/*
 * * Copyright (C) 2013-2026 Matt Baxter https://kitteh.org
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

import java.text.Normalizer;
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
     * PRECIS UsernameCaseMapped (RFC 8265) comparison, as used by servers that advertise
     * {@code UTF8MAPPING=rfc8265} (such as Ergo). Applies width mapping, Unicode lowercasing
     * (not case folding, so ß stays ß), and NFC normalization, repeated until stable.
     * Does not validate, as the server rejects names that fail PRECIS.
     */
    final class Rfc8265 implements CaseMapping {
        private static final int MAX_PASSES = 4;

        private Rfc8265() {
        }

        @Override
        public String toLowerCase(String input) {
            Sanity.nullCheck(input, "Input");
            if (isAscii(input)) {
                return ASCII.toLowerCase(input);
            }
            String current = input;
            for (int i = 0; i < MAX_PASSES; i++) {
                String next = Normalizer.normalize(toLowerCaseNoFinalSigma(widthMap(current)), Normalizer.Form.NFC);
                if (next.equals(current)) {
                    break;
                }
                current = next;
            }
            return current;
        }

        private static boolean isAscii(String input) {
            for (int i = 0; i < input.length(); i++) {
                if (input.charAt(i) > 0x7F) {
                    return false;
                }
            }
            return true;
        }

        private static String widthMap(String input) {
            StringBuilder builder = new StringBuilder(input.length());
            input.codePoints().forEach(cp -> {
                // Every <wide> or <narrow> decomposition is U+3000 or in U+FF01-U+FFEE
                if ((cp == 0x3000) || ((cp >= 0xFF01) && (cp <= 0xFFEE))) {
                    builder.append(Normalizer.normalize(Character.toString(cp), Normalizer.Form.NFKC));
                } else {
                    builder.appendCodePoint(cp);
                }
            });
            return builder.toString();
        }

        private static String toLowerCaseNoFinalSigma(String input) {
            // Matches Ergo implementation
            return input.replace('Σ', 'σ').toLowerCase(Locale.ROOT);
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
    /**
     * PRECIS UsernameCaseMapped, per RFC 8265
     */
    CaseMapping.Rfc8265 RFC8265 = new CaseMapping.Rfc8265();

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
            // Taking the reverse version in case a new server follows the wrong modern-irc docs
            case "STRICT-RFC1459", "RFC1459-STRICT" -> STRICT_RFC1459;
            case "RFC7613", "RFC8265" -> RFC8265;
            default -> null;
        });
    }
}
