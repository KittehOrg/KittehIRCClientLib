/*
 * * Copyright (C) 2013-2020 Matt Baxter https://kitteh.org
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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Instant;
import java.util.Optional;

/**
 * Reflects a message tag.
 */
public interface MessageTag {
    /**
     * Represents the `msgid` tag as specified by the Message IDs spec.
     */
    interface MsgId extends MessageTag {
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
        return this.getValue().map(s -> s
                .replace(";", "\\:")
                .replace(" ", "\\s")
                .replace("\\", "\\\\")
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
