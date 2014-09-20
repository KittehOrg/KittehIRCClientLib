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
package org.kitteh.irc;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores an IRCBot's configured data from the {@link BotBuilder}.
 */
final class Config {
    /**
     * Represents a configuration entry.
     *
     * @param <Type>
     */
    static final class Entry<Type> {
        private final Type defaultValue;
        private final Class<Type> type;

        private Entry(Type defaultValue, Class<Type> type) {
            this.defaultValue = defaultValue;
            this.type = type;
        }

        /**
         * Gets the entry's default value.
         *
         * @return the default value
         */
        private Type getDefault() {
            return this.defaultValue;
        }

        /**
         * Gets the type of the entry.
         *
         * @return the entry type
         */
        private Class<Type> getType() {
            return this.type;
        }
    }

    static final Entry<String> BOT_NAME = new Entry<>("Unnamed", String.class);
    static final Entry<InetSocketAddress> BIND_ADDRESS = new Entry<>(null, InetSocketAddress.class);
    static final Entry<Integer> MESSAGE_DELAY = new Entry<>(1200, Integer.class);
    static final Entry<String> NICK = new Entry<>("Kitteh", String.class);
    static final Entry<String> REAL_NAME = new Entry<>("Kitteh", String.class);
    static final Entry<InetSocketAddress> SERVER_ADDRESS = new Entry<>(new InetSocketAddress("localhost", 6667), InetSocketAddress.class);
    static final Entry<String> SERVER_PASSWORD = new Entry<>(null, String.class);
    static final Entry<String> USER = new Entry<>("Kitteh", String.class);

    private static final Object NULL = new Object();

    private final Map<Entry<?>, Object> map = new ConcurrentHashMap<>();

    @Override
    protected Config clone() {
        Config config = new Config();
        config.map.putAll(this.map);
        return config;
    }

    /**
     * Gets a stored configuration entry.
     *
     * @param entry entry to acquire
     * @param <Type> entry type
     * @return the stored entry, or the default value if not set
     */
    <Type> Type get(Entry<Type> entry) {
        if (this.map.containsKey(entry)) {
            Object value = this.map.get(entry);
            if (!value.equals(NULL) && entry.getType().isAssignableFrom(value.getClass())) {
                @SuppressWarnings("unchecked")
                Type tValue = (Type) this.map.get(entry);
                return tValue;
            }
            return null;
        }
        return entry.getDefault();
    }

    /**
     * Sets a configuration entry.
     *
     * @param entry entry to set
     * @param value value to set for the given entry
     * @param <Type> entry type
     */
    <Type> void set(Entry<Type> entry, Type value) {
        this.map.put(entry, value != null ? value : NULL);
    }
}