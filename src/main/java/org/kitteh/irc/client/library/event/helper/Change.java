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
package org.kitteh.irc.client.library.event.helper;

import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;

/**
 * Generic class used to track a piece of information changing.
 *
 * @param <Type> type of data changing
 */
public class Change<Type> {
    private final Type oldData;
    private final Type newData;

    /**
     * Constructs the change.
     *
     * @param oldData the old data prior to the change taking place
     * @param newData the new data following the change taking place
     */
    public Change(@Nonnull Type oldData, @Nonnull Type newData) {
        this.oldData = Sanity.nullCheck(oldData, "old data cannot be null");
        this.newData = Sanity.nullCheck(newData, "new data cannot be null");
    }

    /**
     * Gets the old data prior to the change taking place.
     *
     * @return the old data
     */
    @Nonnull
    public Type getOld() {
        return this.oldData;
    }

    /**
     * Gets the new data following the change taking place.
     *
     * @return the new data
     */
    @Nonnull
    public Type getNew() {
        return this.newData;
    }
}
