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
package org.kitteh.irc.client.library.util;

import org.jspecify.annotations.NonNull;

/*
 * A note on CTCP handling:
 * Per the modern CTCP spec (https://modern.ircdocs.horse/ctcp), a CTCP
 * message is the entire message body, wrapped in \u0001 delimiters. The
 * trailing delimiter is optional when receiving.
 *
 * The 1994 low-level (M-QUOTE) and CTCP-level (backslash) quoting is not
 * implemented by modern clients, so none is applied in either direction.
 * Characters that can't be sent (CR, LF, NUL, and the delimiter itself)
 * are rejected instead.
 */

/**
 * A utility class for CTCP handling, though this is <i>done by KICL methods
 * already in pretty much all cases</i>.
 */
public final class CtcpUtil {
    private static final char CTCP_DELIMITER = '\u0001';

    private CtcpUtil() {
    }

    /**
     * Converts a given message from CTCP formatting, removing the leading
     * delimiter and the trailing delimiter if present. Anything after the
     * trailing delimiter is discarded.
     *
     * @param message message to convert, starting with the CTCP delimiter
     * @return converted message
     */
    public static @NonNull String fromCtcp(@NonNull String message) {
        int secondDelimiter = message.indexOf(CtcpUtil.CTCP_DELIMITER, 1);
        return message.substring(1, (secondDelimiter == -1) ? message.length() : secondDelimiter);
    }

    /**
     * Gets if a given message is a CTCP message.
     *
     * @param message message to test
     * @return true if the message is a CTCP message
     */
    public static boolean isCtcp(@NonNull String message) {
        return !message.isEmpty() && (message.charAt(0) == CtcpUtil.CTCP_DELIMITER);
    }

    /**
     * Converts a given message to CTCP formatting by wrapping it in CTCP
     * delimiters. No quoting is applied.
     *
     * @param message message to convert
     * @return converted message
     * @throws IllegalArgumentException if the message is null or contains
     * CR, LF, NUL, or the CTCP delimiter (0x01)
     */
    public static @NonNull String toCtcp(@NonNull String message) {
        CtcpUtil.checkMessage(message, "Message");
        return CtcpUtil.CTCP_DELIMITER + message + CtcpUtil.CTCP_DELIMITER;
    }

    /**
     * Checks that a message can be sent as the content of a CTCP message.
     *
     * @param message message to check
     * @param name name of the message, for the exception
     * @return the message
     * @throws IllegalArgumentException if the message is null or contains
     * CR, LF, NUL, or the CTCP delimiter (0x01)
     */
    public static @NonNull String checkMessage(@NonNull String message, @NonNull String name) {
        Sanity.safeMessageCheck(message, name);
        Sanity.truthiness(message.indexOf(CtcpUtil.CTCP_DELIMITER) == -1, name + " cannot contain the CTCP delimiter (0x01)");
        return message;
    }
}
