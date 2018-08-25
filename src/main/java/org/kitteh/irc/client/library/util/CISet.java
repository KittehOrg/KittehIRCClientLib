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
package org.kitteh.irc.client.library.util;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.CaseMapping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A thread-safe set with case insensitivity tied to {@link Client}'s {@link
 * CaseMapping}.
 */
public class CISet implements Set<String> {
    private final Client client;
    private CaseMapping lastCaseMapping;
    private final Map<String, String> map = new ConcurrentHashMap<>();

    /**
     * Constructs a set tied to a client.
     *
     * @param client the client to which this set is tied
     */
    public CISet(@NonNull Client client) {
        this.client = Sanity.nullCheck(client, "Client cannot be null");
    }

    /**
     * Converts a given input to lower case based on the current {@link
     * CaseMapping}.
     *
     * @param input input to convert
     * @return lower cased input
     */
    protected final synchronized String toLowerCase(@NonNull String input) {
        CaseMapping caseMapping = this.client.getServerInfo().getCaseMapping();
        if (caseMapping != this.lastCaseMapping) {
            Set<String> set = new HashSet<>(this.map.values());
            this.lastCaseMapping = caseMapping;
            this.map.clear();
            set.forEach(this::add);
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
    public boolean contains(@Nullable Object o) {
        return (o instanceof String) && this.map.containsKey(this.toLowerCase((String) o));
    }

    @Override
    public @NonNull Iterator<String> iterator() {
        return this.map.values().iterator();
    }

    @Override
    public @NonNull Object[] toArray() {
        return this.map.values().toArray();
    }

    @Override
    public @NonNull <T> T[] toArray(@NonNull T[] a) {
        return this.map.values().toArray(a);
    }

    @Override
    public boolean add(@NonNull String s) {
        Sanity.nullCheck(s, "String cannot be null");
        this.map.put(this.toLowerCase(s), s);
        return true;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        return (o instanceof String) && (this.map.remove(this.toLowerCase((String) o)) != null);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        Sanity.nullCheck(c, "Collection cannot be null");
        for (Object o : c) {
            if (!this.contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends String> c) {
        Sanity.nullCheck(c, "Collection cannot be null");
        c.forEach(this::add);
        return true;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        Sanity.nullCheck(c, "Collection cannot be null");
        return this.map.keySet().retainAll(c.stream().filter(i -> i instanceof String).map(i -> (String) i).map(this::toLowerCase).collect(Collectors.toSet()));
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        Sanity.nullCheck(c, "Collection cannot be null");
        return this.map.keySet().removeAll(c.stream().filter(i -> i instanceof String).map(i -> (String) i).map(this::toLowerCase).collect(Collectors.toSet()));
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).add("client", this.client).add("set", this.map.values()).toString();
    }
}
