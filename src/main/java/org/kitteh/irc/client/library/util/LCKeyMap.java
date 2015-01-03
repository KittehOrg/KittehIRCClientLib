/*
 * * Copyright (C) 2013-2014 Matt Baxter http://kitteh.org
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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A threadsafe hash map with lowercased keys.
 */
public class LCKeyMap<Value> extends ConcurrentHashMap<String, Value> {
    private final Client client;
    private CaseMapping lastCaseMapping;

    public LCKeyMap(Client client) {
        this.client = client;
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String && super.containsKey(this.toLowerCase((String) key));
    }

    @Override
    public Value get(Object key) {
        return key instanceof String ? super.get(this.toLowerCase((String) key)) : null;
    }

    @Override
    public Value put(String key, Value value) {
        return super.put(this.toLowerCase(key), value);
    }

    @Override
    public Value remove(Object key) {
        return key instanceof String ? super.remove(this.toLowerCase((String) key)) : null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Value> m) {
        for (Entry<? extends String, ? extends Value> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue()); // Lowercased
        }
    }

    protected final synchronized String toLowerCase(String input) {
        CaseMapping caseMapping = this.client.getServerInfo().getCaseMapping();
        if (caseMapping != this.lastCaseMapping) {
            this.lastCaseMapping = caseMapping;
            Iterator<Entry<String, Value>> i = super.entrySet().iterator();
            Entry<String, Value> entry;
            while (i.hasNext()) {
                entry = i.next();
                final String lowerKey = caseMapping.toLowerCase(entry.getKey());
                if (!lowerKey.equals(entry.getKey())) {
                    if (!super.containsKey(lowerKey)) {
                        super.put(lowerKey, entry.getValue());
                    }
                    i.remove();
                }
            }
        }
        return caseMapping.toLowerCase(input);
    }
}
