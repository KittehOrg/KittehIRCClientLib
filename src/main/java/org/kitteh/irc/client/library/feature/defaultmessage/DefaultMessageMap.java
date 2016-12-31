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
package org.kitteh.irc.client.library.feature.defaultmessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

/**
 * A mapping of default message types to their corresponding default message.
 */
public interface DefaultMessageMap {
    /**
     * Sets a new default value for a given default message type.
     *
     * @param key message type
     * @param defaultString default value
     * @return self to permit chaining
     */
    @Nonnull
    DefaultMessageMap setDefault(DefaultMessageType key, @Nullable String defaultString);

    /**
     * Retrieves the default string value for the specified default message
     * type, using {@link DefaultMessageType#getFallback()} if not set in
     * this map.
     *
     * @param key message type
     * @return default message
     */
    @Nonnull
    Optional<String> getDefault(DefaultMessageType key);

    /**
     * Retrieves the default string value for the specified default message
     * type, using a provided default value if not set in this map.
     *
     * @param key message type
     * @param defaultValue a fallback value for if no default is set
     * @return default message
     */
    @Nonnull
    Optional<String> getDefault(DefaultMessageType key, String defaultValue);

    /**
     * Retrieves the full list of all {@link DefaultMessageType} to String
     * mappings.
     *
     * @return an immutable copy of the map
     */
    @Nonnull
    Map<DefaultMessageType, String> getDefaults();
}
