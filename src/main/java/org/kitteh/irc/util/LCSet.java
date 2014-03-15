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
package org.kitteh.irc.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A threadsafe, automagically lowercased Set.
 */
public final class LCSet extends CopyOnWriteArraySet<String> {
    @Override
    public boolean contains(Object o) {
        return o instanceof String && super.contains(((String) o).toLowerCase());
    }

    @Override
    public boolean add(String s) {
        return super.add(s.toLowerCase());
    }

    @Override
    public boolean remove(Object o) {
        return o instanceof String && super.remove(((String) o).toLowerCase());
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!this.contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        boolean modified = false;
        for (String s : c) {
            if (this.add(s)) {
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
        Set<String> set = new HashSet<>();
        for (Object o : c) {
            if (o instanceof String) {
                set.add(((String) o).toLowerCase());
            }
        }
        return set;
    }
}