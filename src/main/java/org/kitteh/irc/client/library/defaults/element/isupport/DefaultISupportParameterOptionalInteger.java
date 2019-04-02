/*
 * * Copyright (C) 2013-2019 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.defaults.element.DefaultISupportParameter;
import org.kitteh.irc.client.library.element.ISupportParameter;

import java.util.OptionalInt;

/**
 * Default implementation of {@link OptionalIntegerParameter}.
 */
public abstract class DefaultISupportParameterOptionalInteger extends DefaultISupportParameter implements ISupportParameter.OptionalIntegerParameter {
    private final Integer integer;

    /**
     * Constructs the object.
     *
     * @param client client
     * @param name parameter name
     * @param value value
     */
    protected DefaultISupportParameterOptionalInteger(@NonNull Client client, @NonNull String name, @Nullable String value) {
        super(client, name, value);
        Integer integer;
        try {
            integer = Integer.valueOf(value);
        } catch (Exception e) {
            integer = null;
        }
        this.integer = integer;
    }

    @Override
    public OptionalInt getInteger() {
        return this.integer == null ? OptionalInt.empty() : OptionalInt.of(this.integer);
    }
}