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
package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.ToStringer;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

abstract class AbstractNameValueProcessor<NameValue> {
    protected static class Creator<NameValue> {
        private final TriFunction<Client, String, Optional<String>, ? extends NameValue> function;

        protected Creator(@Nonnull TriFunction<Client, String, Optional<String>, ? extends NameValue> function) {
            this.function = function;
        }

        @Nonnull
        protected TriFunction<Client, String, Optional<String>, ? extends NameValue> getFunction() {
            return this.function;
        }
    }

    private final InternalClient client;
    private final Map<String, Creator<NameValue>> registeredNames = new ConcurrentHashMap<>();

    AbstractNameValueProcessor(InternalClient client) {
        this.client = client;
    }

    protected InternalClient getClient() {
        return this.client;
    }

    protected final Map<String, Creator<NameValue>> getRegistrations() {
        return this.registeredNames;
    }

    @Nonnull
    protected final Optional<TriFunction<Client, String, Optional<String>, ? extends NameValue>> getCreatorByName(@Nonnull String name) {
        return this.optional(this.registeredNames.get(name));
    }

    @Nonnull
    protected final Optional<TriFunction<Client, String, Optional<String>, ? extends NameValue>> registerCreator(@Nonnull String name, @Nonnull Creator<NameValue> creator) {
        return this.optional(this.registeredNames.put(name, creator));
    }

    @Nonnull
    protected final Optional<TriFunction<Client, String, Optional<String>, ? extends NameValue>> unregisterCreator(@Nonnull String name) {
        return this.optional(this.registeredNames.remove(name));
    }

    @Nonnull
    private Optional<TriFunction<Client, String, Optional<String>, ? extends NameValue>> optional(@Nullable Creator<NameValue> creator) {
        return (creator == null) ? Optional.empty() : Optional.of(creator.getFunction());
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).toString();
    }
}
