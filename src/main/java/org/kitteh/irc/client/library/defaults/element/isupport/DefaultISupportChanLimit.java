/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.exception.KittehServerISupportException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link ISupportParameter.ChanLimit}.
 */
public class DefaultISupportChanLimit extends DefaultISupportParameterValueRequired implements ISupportParameter.ChanLimit {
    private final Map<Character, Integer> limits;

    /**
     * Constructs the object.
     *
     * @param client client
     * @param name parameter name
     * @param value parameter value, if present
     */
    public DefaultISupportChanLimit(@NonNull Client client, @NonNull String name, @Nullable String value) {
        super(client, name, value);
        if (value == null) {
            throw new KittehServerISupportException(name, "No limits defined");
        }
        String[] pairs = value.split(",");
        Map<Character, Integer> limits = new HashMap<>();
        for (String p : pairs) {
            String[] pair = p.split(":");
            if (pair.length != 2) {
                throw new KittehServerISupportException(name, "Invalid format");
            }
            int limit;
            try {
                limit = Integer.parseInt(pair[1]);
            } catch (Exception e) {
                throw new KittehServerISupportException(name, "Non-integer limit", e);
            }
            for (char prefix : pair[0].toCharArray()) {
                limits.put(prefix, limit);
            }
        }
        this.limits = Collections.unmodifiableMap(limits);
    }

    @Override
    public @NonNull Map<Character, Integer> getLimits() {
        return this.limits;
    }
}
