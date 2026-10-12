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
package org.kitteh.irc.client.library.element;

import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.Optional;

/**
 * Reflects a message tag.
 */
public interface MessageTag {
    /**
     * Represents the `bot` tag, flagging any sender with {@link org.kitteh.irc.client.library.element.ISupportParameter.Bot} mode.
     */
    interface Bot extends MessageTag {

    }

    /**
     * Represents the `label` tag as specified by the labeled responses spec.
     */
    interface Label extends MessageTag {
        /**
         * Gets the label.
         *
         * @return the label
         */
        @NonNull String getLabel();
    }

    /**
     * Represents the `msgid` tag as specified by the Message IDs spec.
     */
    interface MsgId extends MessageTag {
        /**
         * Gets the message id.
         *
         * @return the message id
         */
        @NonNull String getId();
    }

    /**
     * Represents the 'time' tag as specified by the 'server-time' extension.
     */
    interface Time extends MessageTag {
        /**
         * Gets the instant in time specified by this tag.
         *
         * @return instant in time
         */
        @NonNull Instant getTime();
    }

    /**
     * Represents the `typing` client tag as specified here:
     * <a href="https://ircv3.net/specs/client-tags/typing#format">https://ircv3.net/specs/client-tags/typing#format</a>
     */
    interface Typing extends MessageTag {
        /**
         * Typing states.
         */
        enum State {
            /**
             * The user is actively typing.
             */
            ACTIVE,
            /**
             * The user has cleared the text field.
             */
            DONE,
            /**
             * The user has paused typing with text in the text input field.
             */
            PAUSED
        }

        /**
         * Gets the typing state.
         *
         * @return typing state
         */
        @NonNull State getState();
    }

    /**
     * Gets if a name is a valid tag key to send: an optional client-only {@code +} prefix,
     * an optional vendor followed by {@code /}, then a non-empty name of ASCII letters, digits, and hyphens.
     * <p>
     * Received tags are not validated, because the spec forbids rejecting a message over an invalid tag key.
     *
     * @param name name to test
     * @return true if valid
     */
    static boolean isValidName(@NonNull String name) {
        int start = (!name.isEmpty() && (name.charAt(0) == '+')) ? 1 : 0;
        int slash = name.indexOf('/', start);
        if (slash != -1) { // sweet char of mine
            if (slash == start) {
                return false;
            }
            for (int i = start; i < slash; i++) {
                char c = name.charAt(i);
                if (!MessageTag.isKeyNameChar(c) && (c != '.')) {
                    return false;
                }
            }
            start = slash + 1;
        }
        if (start == name.length()) {
            return false;
        }
        for (int i = start; i < name.length(); i++) {
            if (!MessageTag.isKeyNameChar(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isKeyNameChar(char c) {
        return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')) || ((c >= '0') && (c <= '9')) || (c == '-');
    }

    /**
     * Gets the name of the tag.
     *
     * @return tag name
     */
    @NonNull String getName();

    /**
     * Gets the unescaped, but otherwise unprocessed, value of the tag.
     *
     * @return tag value if set
     */
    @NonNull Optional<String> getValue();

    /**
     * Gets the escaped value of the tag.
     *
     * @return escaped tag value if set
     */
    default @NonNull Optional<String> getEscapedValue() {
        // Backslash first, so the backslashes added by the other escapes aren't doubled
        return this.getValue().map(s -> s
                .replace("\\", "\\\\")
                .replace(";", "\\:")
                .replace(" ", "\\s")
                .replace("\r", "\\r")
                .replace("\n", "\\n"));
    }

    /**
     * Gets if this message tag is a client-only tag.
     *
     * @return true if the tag name starts with a {@code +} character
     */
    default boolean isClientOnly() {
        return !this.getName().isEmpty() && this.getName().charAt(0) == '+';
    }

    /**
     * Gets the tag in the String format sent over the IRC protocol.
     *
     * @return tag in String form
     */
    default @NonNull String getAsString() {
        return this.getEscapedValue().map(s -> this.getName() + '=' + s).orElseGet(this::getName);
    }
}
