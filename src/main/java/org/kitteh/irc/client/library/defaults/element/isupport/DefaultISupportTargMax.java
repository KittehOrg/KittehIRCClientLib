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
package org.kitteh.irc.client.library.defaults.element.isupport;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.DefaultISupportParameter;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.util.CIKeyMap;
import org.kitteh.irc.client.library.util.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Default implementation of {@link org.kitteh.irc.client.library.element.ISupportParameter.TargMax}.
 */
public class DefaultISupportTargMax extends DefaultISupportParameter implements ISupportParameter.TargMax {
    private final CIKeyMap<Pair<String, OptionalInt>> map;

    /**
     * Constructs the object.
     *
     * @param client client
     * @param name parameter name
     * @param value parameter value, if present
     */
    public DefaultISupportTargMax(@NonNull Client client, @NonNull String name, @Nullable String value) {
        super(client, name, value);
        this.map = new CIKeyMap<>(client);
        if (value != null) {
            for (String string : value.split(",")) {
                String[] parts = string.split(":");
                this.map.put(parts[0], Pair.of(parts[0], parts.length == 1 ? OptionalInt.empty() : OptionalInt.of(Integer.parseInt(parts[1]))));
            }
        }
    }

    @Override
    public @NonNull Set<Pair<String, OptionalInt>> getEntries() {
        return Collections.unmodifiableSet(new HashSet<>(this.map.values()));
    }

    @Override
    public @NonNull OptionalInt getMax(@NonNull String command) {
        Pair<String, OptionalInt> pair = this.map.get(command);
        return pair == null ? OptionalInt.empty() : pair.getRight();
    }
}
