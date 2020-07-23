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
 * Default implementation of {@link CaseMapping}.
 */
public class DefaultISupportMaxList extends DefaultISupportParameterValueRequired implements ISupportParameter.MaxList {
    /**
     * Default implementation of {@link MaxList.LimitData}.
     */
    public class DefaultLimitData implements MaxList.LimitData {
        private final int limit;
        private final Set<Character> modes;

        /**
         * Constructs this limit data.
         *
         * @param modes modes
         * @param limit limit
         */
        public DefaultLimitData(@NonNull Set<Character> modes, int limit) {
            this.limit = limit;
            this.modes = Collections.unmodifiableSet(modes);
        }

        @Override
        public int getLimit() {
            return this.limit;
        }

        @Override
        public @NonNull Set<Character> getModes() {
            return this.modes;
        }
    }

    private final Set<MaxList.LimitData> data;

    /**
     * Constructs the object.
     *
     * @param client client
     * @param name parameter name
     * @param value parameter value, if present
     */
    public DefaultISupportMaxList(@NonNull Client client, @NonNull String name, @Nullable String value) {
        super(client, name, value);
        Set<MaxList.LimitData> limitData = new HashSet<>();
        for (String limit : value.split(",")) {
            String[] split = limit.split(":");
            Set<Character> modes = new HashSet<>();
            for (char c : split[0].toCharArray()) {
                modes.add(c);
            }
            limitData.add(new DefaultLimitData(modes, Integer.parseInt(split[1])));
        }
        this.data = Collections.unmodifiableSet(limitData);
    }

    @Override
    public @NonNull Set<LimitData> getAllLimitData() {
        return this.data;
    }

    @Override
    public int getLimit(char c) {
        return this.data.stream().filter(d -> d.getModes().contains(c)).mapToInt(LimitData::getLimit).findFirst().orElse(-1);
    }

    @Override
    public @NonNull Optional<LimitData> getLimitData(char c) {
        return this.data.stream().filter(d -> d.getModes().contains(c)).findFirst();
    }
}
