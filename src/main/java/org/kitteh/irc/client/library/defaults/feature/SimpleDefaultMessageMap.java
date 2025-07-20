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
package org.kitteh.irc.client.library.defaults.feature;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageMap;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageType;
import org.kitteh.irc.client.library.util.Sanity;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Provides a way to set default messages.
 *
 * @see DefaultMessageType
 */
public class SimpleDefaultMessageMap implements DefaultMessageMap {
    /**
     * Defaults stored.
     */
    protected final Map<DefaultMessageType, String> defaults = new EnumMap<>(DefaultMessageType.class);

    /**
     * Creates a default message map and sets all messages to a single value.
     *
     * @param defaultString value to be set for all strings
     */
    public SimpleDefaultMessageMap(@Nullable String defaultString) {
        for (DefaultMessageType defaultMessageType : DefaultMessageType.values()) {
            this.defaults.put(defaultMessageType, defaultString);
        }
    }

    /**
     * Creates a default message map.
     */
    public SimpleDefaultMessageMap() {
    }

    @Override
    public @NonNull SimpleDefaultMessageMap setDefault(@NonNull DefaultMessageType key, @Nullable String defaultString) {
        this.defaults.put(Sanity.nullCheck(key, "Key"), defaultString);
        return this;
    }

    @Override
    public @NonNull Optional<String> getDefault(DefaultMessageType key) {
        return this.getDefault(Sanity.nullCheck(key, "Key"), key.getFallback());
    }

    @Override
    public @NonNull Optional<String> getDefault(@NonNull DefaultMessageType key, @Nullable String defaultValue) {
        Sanity.nullCheck(key, "Key");
        return Optional.ofNullable(this.defaults.getOrDefault(key, defaultValue));
    }

    @Override
    public @NonNull Map<DefaultMessageType, String> getDefaults() {
        return Collections.unmodifiableMap(new EnumMap<>(this.defaults));
    }
}
