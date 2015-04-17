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
package org.kitteh.irc.client.library.util;

import org.kitteh.irc.client.library.CaseMapping;
import org.kitteh.irc.client.library.Client;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A threadsafe hash map with case insensitive keys.
 */
public class CIKeyMap<Value> implements Map<String, Value> {
    private final Client client;
    private CaseMapping lastCaseMapping;
    private final Map<String, Pair<String, Value>> map = new ConcurrentHashMap<>();

    public CIKeyMap(Client client) {
        this.client = client;
    }

    protected final synchronized String toLowerCase(String input) {
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
    public boolean containsKey(Object key) {
        return key instanceof String && this.map.containsKey(this.toLowerCase((String) key));
    }

    @Override
    public boolean containsValue(Object value) {
        for (Pair<String, Value> pair : this.map.values()) {
            if (value == null ? pair.getRight() == null : pair.getRight().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Value get(Object key) {
        if (key instanceof String) {
            Pair<String, Value> pair = this.map.get(this.toLowerCase((String) key));
            return pair == null ? null : pair.getRight();
        }
        return null;
    }

    @Override
    public Value put(String key, Value value) {
        Pair<String, Value> pair = this.map.put(this.toLowerCase(key), new Pair<>(key, value));
        return pair == null ? null : pair.getRight();
    }

    @Override
    public Value remove(Object key) {
        if (key instanceof String) {
            Pair<String, Value> pair = this.map.remove(this.toLowerCase((String) key));
            return pair == null ? null : pair.getRight();
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Value> m) {
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
    @Override
    public Set<String> keySet() {
        return this.map.values().stream().map(Pair::getLeft).collect(Collectors.toSet());
    }

    /**
     * Gets an UNCHANGING representation of the values.
     *
     * @return list of values
     */
    @Override
    public Collection<Value> values() {
        return this.map.values().stream().map(Pair::getRight).collect(Collectors.toList());
    }

    /**
     * Gets an UNCHANGING representation of the entries.
     *
     * @return set of entries
     */
    @Override
    public Set<Entry<String, Value>> entrySet() {
        return this.map.values().stream().map(pair -> new AbstractMap.SimpleImmutableEntry<>(pair.getLeft(), pair.getRight())).collect(Collectors.toSet());
    }
}