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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A threadsafe hash map with case insensitive keys, and a keySet and
 * entrySet that is not yet actually production ready.
 */
public class CIKeyMap<Value> implements Map<String, Value> {
    private final Client client;
    private CaseMapping lastCaseMapping;
    private final Map<String, String> keys = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<String, Value> values = new ConcurrentHashMap<>();

    public CIKeyMap(Client client) {
        this.client = client;
    }

    @Override
    public int size() {
        return this.values.size();
    }

    @Override
    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String && this.values.containsKey(this.toLowerCase((String) key));
    }

    @Override
    public boolean containsValue(Object value) {
        return this.values.containsValue(value);
    }

    @Override
    public Value get(Object key) {
        return key instanceof String ? this.values.get(this.toLowerCase((String) key)) : null;
    }

    @Override
    public Value put(String key, Value value) {
        this.keys.put(this.toLowerCase(key), key);
        return this.values.put(this.toLowerCase(key), value);
    }

    @Override
    public Value remove(Object key) {
        if (key instanceof String) {
            String lc = this.toLowerCase((String) key);
            this.keys.remove(lc);
            return this.values.remove(lc);
        } else {
            return null;
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends Value> m) {
        m.entrySet().forEach(entry -> this.put(entry.getKey(), entry.getValue())); // Lowercased via put
    }

    @Override
    public void clear() {
        this.keys.clear();
        this.values.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.values.keySet().stream().map(this.keys::get).collect(Collectors.toSet());
    }

    @Override
    public Collection<Value> values() {
        return this.values.values();
    }

    @Override
    public Set<Entry<String, Value>> entrySet() {
        return this.values.entrySet().stream().map(entry -> new AbstractMap.SimpleImmutableEntry<>(this.keys.get(entry.getKey()), entry.getValue())).collect(Collectors.toSet());
    }

    protected final synchronized String toLowerCase(String input) {
        CaseMapping caseMapping = this.client.getServerInfo().getCaseMapping();
        if (caseMapping != this.lastCaseMapping) {
            this.lastCaseMapping = caseMapping;
            Iterator<Entry<String, Value>> i = this.entrySet().iterator();
            Entry<String, Value> entry;
            while (i.hasNext()) {
                entry = i.next();
                final String lowerKey = caseMapping.toLowerCase(entry.getKey());
                if (!lowerKey.equals(entry.getKey())) {
                    if (!this.values.containsKey(lowerKey)) {
                        this.put(lowerKey, entry.getValue());
                    }
                    i.remove();
                }
            }
        }
        return caseMapping.toLowerCase(input);
    }
}