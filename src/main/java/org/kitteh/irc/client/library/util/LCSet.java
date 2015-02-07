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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * A threadsafe, automagically lowercased Set.
 */
public class LCSet extends CopyOnWriteArraySet<String> {
    private final Client client;
    private CaseMapping lastCaseMapping;

    public LCSet(Client client) {
        this.client = client;
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof String && super.contains(this.toLowerCase(((String) o)));
    }

    @Override
    public boolean add(String s) {
        return super.add(this.toLowerCase(s));
    }

    @Override
    public boolean remove(Object o) {
        return o instanceof String && super.remove(this.toLowerCase(((String) o)));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!this.contains(o)) { // Lowercased
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        boolean modified = false;
        for (String s : c) {
            if (this.add(s)) { // Lowercased
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return super.retainAll(this.toLC(c));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return super.removeAll(this.toLC(c));
    }

    private Set<String> toLC(Collection<?> c) {
        return c.stream().filter(o -> o instanceof String).map(o -> this.toLowerCase(((String) o))).collect(Collectors.toSet());
    }

    protected final synchronized String toLowerCase(String input) {
        CaseMapping caseMapping = this.client.getServerInfo().getCaseMapping();
        if (caseMapping != this.lastCaseMapping) {
            this.lastCaseMapping = caseMapping;
            Iterator<String> i = super.iterator();
            String key;
            while (i.hasNext()) {
                key = i.next();
                final String lowerKey = caseMapping.toLowerCase(key);
                if (!lowerKey.equals(key)) {
                    super.add(lowerKey);
                    i.remove();
                }
            }
        }
        return caseMapping.toLowerCase(input);
    }
}