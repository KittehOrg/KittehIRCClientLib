/*
 * * Copyright (C) 2013-2023 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.defaults.element.isupport;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ISupportParameter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Default implementation of {@link ExtBan}.
 */
public class DefaultISupportExtBan extends DefaultISupportParameterValueRequired implements ISupportParameter.ExtBan {
    private final Character prefix;
    private final Set<Character> types;

    /**
     * Constructs the object.
     *
     * @param client client
     * @param name parameter name
     * @param value parameter value, if present
     */
    public DefaultISupportExtBan(@NonNull Client client, @NonNull String name, @Nullable String value) {
        super(client, name, value);
        String[] split = value.split(",");
        this.prefix = split[0].isEmpty() ? null : split[0].charAt(0);
        Set<Character> types = new HashSet<>();
        for (char c : split[1].toCharArray()) {
            types.add(c);
        }
        this.types = Collections.unmodifiableSet(types);
    }

    @Override
    public @NonNull Optional<Character> getPrefix() {
        return Optional.of(this.prefix);
    }

    @Override
    public @NonNull Set<Character> getTypes() {
        return this.types;
    }
}
