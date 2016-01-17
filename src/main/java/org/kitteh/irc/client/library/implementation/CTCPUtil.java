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
package org.kitteh.irc.client.library.implementation;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * A note on CTCP handling:
 * According to the original 'documentation' on the matter,
 * there can be multiple CTCP messages as well as normal messages
 * spread throughout. However, from what I can tell, most folks
 * don't bother with that in their implementations. So, we're
 * going to stick with having a single CTCP message at the start
 * of a message.
 *
 * CTCP is magic!
 *
 * Messages are defined as being between two \u0001 characters.
 *
 * The following characters are escaped by the MQUOTE character: \u0016
 *   \n     -> MQUOTE n
 *   \r     -> MQUOTE r
 *   \u0000 -> MQUOTE 0
 *   MQUOTE -> MQUOTE MQUOTE
 * When converting, anything else escaped with the MQUOTE should just strip the MQUOTE
 *
 * The following characters are escaped by a backslash
 *   \u0001 -> \a
 *   \      -> \\
 * When converting, anything else escaped with a backslash should just strip the backslash
 */

/**
 * A utility class for CTCP handling.
 * <p>
 * Stored in this package so it can be package private to avoid confusion.
 * This stuff is all handled internally by the client; no client user needs
 * to know how to do this.
 */
final class CTCPUtil {
    private static final char CTCP_DELIMITER = '\u0001';
    private static final char CTCP_MQUOTE = '\u0016';

    private static final Pattern CTCP_ESCAPABLE_CHAR = Pattern.compile("[\n\r\u0000" + CTCP_DELIMITER + CTCP_MQUOTE + "\\\\]");
    private static final Pattern CTCP_ESCAPED_CHAR = Pattern.compile("([" + CTCP_MQUOTE + "\\\\])(.)");
    private static final Pattern CTCP_MESSAGE = Pattern.compile(CTCP_DELIMITER + "([^" + CTCP_DELIMITER + "]*)" + CTCP_DELIMITER + "[^" + CTCP_DELIMITER + "]*");

    private CTCPUtil() {
    }

    /**
     * Converts a given message from CTCP escaping.
     *
     * @param message message to convert
     * @return converted message
     */
    @Nonnull
    static String fromCTCP(@Nonnull String message) {
        final String ctcpContent = message.substring(1, message.indexOf(CTCP_DELIMITER, 1)); // Strip the delimiters
        StringBuilder builder = new StringBuilder(ctcpContent.length());
        int currentIndex = 0;
        Matcher matcher = CTCP_ESCAPED_CHAR.matcher(ctcpContent);
        while (matcher.find()) {
            if (matcher.start() > currentIndex) {
                builder.append(ctcpContent.substring(currentIndex, matcher.start()));
            }
            switch (matcher.group(1)) {
                case CTCP_MQUOTE + "":
                    switch (matcher.group(2)) {
                        case "n":
                            builder.append('\n');
                            break;
                        case "r":
                            builder.append('\r');
                            break;
                        case "0":
                            builder.append('\u0000');
                            break;
                        default:
                            builder.append(matcher.group(2)); // If not one of the above, disregard the MQUOTE. If MQUOTE, it's covered here anyway.
                    }
                    break;
                case "\\":
                    switch (matcher.group(2)) {
                        case "a":
                            builder.append(CTCP_DELIMITER);
                            break;
                        default:
                            builder.append(matcher.group(2)); // If not one of the above, disregard the \. If \, it's covered here anyway.
                    }
            }
            currentIndex = matcher.end();
        }
        if (currentIndex < ctcpContent.length()) {
            builder.append(ctcpContent.substring(currentIndex));
        }
        return builder.toString();
    }

    /**
     * Gets if a given message is a CTCP message.
     *
     * @param message message to test
     * @return true if the message is a CTCP message
     */
    static boolean isCTCP(@Nonnull String message) {
        return CTCP_MESSAGE.matcher(message).matches();
    }

    /**
     * Converts a given message to CTCP formatting.
     *
     * @param message message to convert
     * @return converted message
     */
    @Nonnull
    static String toCTCP(@Nonnull String message) {
        StringBuilder builder = new StringBuilder(message.length());
        builder.append(CTCP_DELIMITER);
        int currentIndex = 0;
        Matcher matcher = CTCP_ESCAPABLE_CHAR.matcher(message);
        while (matcher.find()) {
            if (matcher.start() > currentIndex) {
                builder.append(message.substring(currentIndex, matcher.start()));
            }
            switch (matcher.group()) {
                case "\n":
                    builder.append(CTCP_MQUOTE).append('n');
                    break;
                case "\r":
                    builder.append(CTCP_MQUOTE).append('r');
                    break;
                case "\u0000":
                    builder.append(CTCP_MQUOTE).append('0');
                    break;
                case CTCP_MQUOTE + "":
                    builder.append(CTCP_MQUOTE).append(CTCP_MQUOTE);
                    break;
                case CTCP_DELIMITER + "":
                    builder.append("\\a");
                    break;
                case "\\":
                    builder.append("\\\\");
                    break;
            }
            currentIndex = matcher.end();
        }
        if (currentIndex < message.length()) {
            builder.append(message.substring(currentIndex));
        }
        builder.append(CTCP_DELIMITER);
        return builder.toString();
    }
}
