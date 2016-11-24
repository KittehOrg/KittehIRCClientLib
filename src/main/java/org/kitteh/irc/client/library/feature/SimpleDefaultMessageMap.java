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

import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Provides a way to set default messages for certain events, for example during
 * a reconnect, quit, or kick.
 */
public class SimpleDefaultMessageMap implements DefaultMessageMap {
    protected final Map<DefaultMessage, String> defaults = new HashMap<>();
    protected final String defaultString;

    /**
     * Creates a SimpleDefaultMessageMap object with a custom string to default
     * to when an DefaultMessage key is not set.
     *
     * @param defaultString a default string to use for when a key is not set
     * @throws IllegalArgumentException Will throw an IllegalArgumentException is you set the
     * defaultString value to "". Use null instead.
     */
    public SimpleDefaultMessageMap(@Nullable String defaultString) {
        this.defaultString = this.emptyStringCheck(defaultString);
    }

    /**
     * Creates a SimpleDefaultMessageMap object with predefined string to default
     * to when an DefaultMessage key is not set.
     */
    public SimpleDefaultMessageMap() {
        this.defaultString = null;
    }

    /**
     * Sets a default message for the DefaultMessage.
     *
     * @param key The default message key to set a value for
     * @param defaultString The default value to assume when nothing is
     * provided for this DefaultMessage
     * @return Returns this to permit chaining
     * @throws IllegalArgumentException if defaultString is an empty String. Use null instead.
     */
    public SimpleDefaultMessageMap setDefault(@Nonnull DefaultMessage key, @Nullable String defaultString) {
        Sanity.nullCheck(key, "DefaultMessage key must not be null");

        this.defaults.put(key, this.emptyStringCheck(defaultString));

        return this;
    }

    /**
     * Retrieves the default string value for the specified
     * DefaultMessage.
     *
     * @param key the default DefaultMessage key to obtain the
     * default of
     * @return the string of the default value, or the predefined
     * defaultString
     * @see {@link #getDefault(DefaultMessage, String)}
     */
    public Optional<String> getDefault(DefaultMessage key) {
        return this.getDefault(key, this.defaultString);
    }

    /**
     * Retrieves the default string value for the specified
     * DefaultMessage, but override the defaultString
     * value with our own message if the value is not set.
     *
     * @param key default DefaultMessage key to get the default of
     * @param defaultValue instead of referencing to the defaultString,
     * use this value instead if it is not set.
     * @return string of the default value, or the second parameter
     * if the key is not set.
     */
    public Optional<String> getDefault(@Nonnull DefaultMessage key, @Nullable String defaultValue) {
        Sanity.nullCheck(key, "DefaultMessage key must not be null");

        String value = this.defaults.get(key);

        return value != null ? Optional.of(value) : Optional.ofNullable(defaultValue);
    }


    /**
     * Retrieves a list of all currently-defined defaults, filling in the
     * unset keys with the defaultString defined in the object's constructor.
     *
     * @return a map with default strings for all default messages
     */
    public Map<DefaultMessage, String> getDefaults() {
        for (DefaultMessage key : DefaultMessage.values()) {
            this.defaults.computeIfAbsent(key, value -> this.defaultString);
        }

        return Collections.unmodifiableMap(this.defaults);
    }

    /**
     * Checks if defaultString is not null and is empty, then
     * throws an IllegalArgumentException.
     *
     * @param defaultString string to check
     * @return defaultString as it was passed
     * @throws IllegalArgumentException if the defaultString is not null and is empty
     */
    protected String emptyStringCheck(@Nullable String defaultString) {
        if (defaultString != null && defaultString.isEmpty()) {
            // Both "" and null are legal here, technically. But for KICL to
            // perform best (and to avoid != null && string.isEmpty() checks)
            // we enforce a requirement on null.
            throw new IllegalArgumentException("defaultString may not be an empty string, use null instead.");
        }

        return defaultString;
    }
}
