/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Optional;

/**
 * Reflects a message tag.
 */
public interface MessageTag {
    /**
     * Represents the 'time' tag as specified by the 'server-time' extension.
     */
    interface Time extends MessageTag {
        /**
         * Gets the instant in time specified by this tag.
         *
         * @return instant in time
         */
        @Nonnull
        Instant getTime();
    }

    /**
     * Gets the name of the tag.
     *
     * @return tag name
     */
    @Nonnull
    String getName();

    /**
     * Gets the unescaped, but otherwise unprocessed, value of the tag.
     *
     * @return tag value if set
     */
    @Nonnull
    Optional<String> getValue();
}
