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
package org.kitteh.irc.client.library.util;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.CaseMapping;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A thread-safe hash map with case insensitive keys tied to {@link Client}'s
 * {@link CaseMapping}. Note that some methods do not behave like all maps.
 */
public class CIKeyMap<Value> implements Map<String, Value> {
    private final Client client;
    private CaseMapping lastCaseMapping;
    private final Map<String, Pair<String, Value>> map = new ConcurrentHashMap<>();

    /**
     * Constructs a map tied to a client.
     *
     * @param client the client to which this map is tied
     */
    public CIKeyMap(Client client) {
        this.client = client;
    }

    /**
     * Converts a given input to lower case based on the current {@link
     * CaseMapping}.
     *
     * @param input input to convert
     * @return lower cased input
     */
    @Nonnull
    protected final synchronized String toLowerCase(@Nonnull String input) {
        CaseMapping caseMapping = this.client.getServerInfo().getCaseMapping();
        if (caseMapping != this.lastCaseMapping) {
            Set<Entry<String, Value>> entrySet = this.entrySet();
            this.lastCaseMapping = caseMapping;
            this.map.clear();
            entrySet.forEach(entry -> this.put(entry.getKey(), entry.getValue()));
        }
        return caseMapping.toLowerCase(input);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return (key instanceof String) && this.map.containsKey(this.toLowerCase((String) key));
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        for (Pair<String, Value> pair : this.map.values()) {
            if ((value == null) ? (pair.getRight() == null) : value.equals(pair.getRight())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public Value get(@Nullable Object key) {
        if (key instanceof String) {
            Pair<String, Value> pair = this.map.get(this.toLowerCase((String) key));
            return (pair == null) ? null : pair.getRight();
        }
        return null;
    }

    @Nullable
    @Override
    public Value put(@Nonnull String key, @Nullable Value value) {
        Sanity.nullCheck(key, "Key cannot be null");
        Pair<String, Value> pair = this.map.put(this.toLowerCase(key), new Pair<>(key, value));
        return (pair == null) ? null : pair.getRight();
    }

    @Nullable
    @Override
    public Value remove(@Nullable Object key) {
        if (key instanceof String) {
            Pair<String, Value> pair = this.map.remove(this.toLowerCase((String) key));
            return (pair == null) ? null : pair.getRight();
        }
        return null;
    }

    @Override
    public void putAll(@Nonnull Map<? extends String, ? extends Value> m) {
        Sanity.nullCheck(m, "Map cannot be null");
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    /**
     * Gets an UNCHANGING representation of the keys.
     *
     * @return set of keys
     */
    @Nonnull
    @Override
    public Set<String> keySet() {
        return this.map.values().stream().map(Pair::getLeft).collect(Collectors.toSet());
    }

    /**
     * Gets an UNCHANGING representation of the values.
     *
     * @return list of values
     */
    @Nonnull
    @Override
    public Collection<Value> values() {
        return this.map.values().stream().map(Pair::getRight).collect(Collectors.toList());
    }

    /**
     * Gets an UNCHANGING representation of the entries.
     *
     * @return set of entries
     */
    @Nonnull
    @Override
    public Set<Entry<String, Value>> entrySet() {
        return this.map.values().stream().map(pair -> new AbstractMap.SimpleImmutableEntry<>(pair.getLeft(), pair.getRight())).collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("client", this.client).add("map", this.map.values().stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight))).toString();
    }
}
