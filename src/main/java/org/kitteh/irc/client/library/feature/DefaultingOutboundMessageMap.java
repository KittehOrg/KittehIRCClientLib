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
package org.kitteh.irc.client.library.feature;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

/**
 * A minimalistic interface of what an outbound message map should at least
 * include.
 *
 * Note that for KICL to perform best, null should be passed instead of "" if
 * we want to pass an empty string.
 */
public interface DefaultingOutboundMessageMap {
    /**
     * Sets a new value for the specified outbound message, so that when the
     * value is requested via #getDefault later it will return this value.
     *
     * @param key outbound message to set a value for
     * @param defaultString default value to assume when nothing is provided
     * for this DefaultingOutboundMessage
     * @return self to permit chaining
     */
    DefaultingOutboundMessageMap setDefault(DefaultingOutboundMessage key, @Nullable String defaultString);

    /**
     * Retrieves the value that is attached to the DefaultingOutboundMessage
     * key.
     *
     * @param key default DefaultingOutboundMessage key to obtain the message
     * of
     * @return default message that was set
     */
    Optional<String> getDefault(DefaultingOutboundMessage key);

    /**
     * Retrieves the default string value for the specified
     * DefaultingOutboundMessage, but override the defaultString value with
     * our own message if the value is not set.
     *
     * @param key The default DefaultingOutboundMessage key to obtain the
     * message of
     * @param defaultValue Instead of referencing to the defaultString,
     * use this value instead if it is not set.
     * @return the string of the default value, or the second parameter
     * if the key is not set.
     */
    Optional<String> getDefault(DefaultingOutboundMessage key, String defaultValue);

    /**
     * Retrieves the full list of all DefaultingOutboundMessage <-> String mappings.
     *
     * @return retrieve the map
     */
    Map<DefaultingOutboundMessage, String> getDefaults();
}
