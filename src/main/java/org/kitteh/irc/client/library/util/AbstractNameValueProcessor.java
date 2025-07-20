/*
 * * Copyright (C) 2013-2025 Matt Baxter https://kitteh.org
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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kitteh.irc.client.library.Client;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract class for registering and processing name/value pairs.
 *
 * @param <NameValue> type of pair
 */
public abstract class AbstractNameValueProcessor<NameValue> {
    /**
     * A creator of name/value pairs of a particular type.
     *
     * @param <NameValue> type of pair
     */
    public static class Creator<NameValue> {
        private final TriFunction<Client, String, String, ? extends NameValue> function;

        /**
         * Constructs the creator.
         *
         * @param function function to do the work
         */
        public Creator(@NonNull TriFunction<Client, String, String, ? extends NameValue> function) {
            this.function = Sanity.nullCheck(function, "Function");
        }

        /**
         * Gets the creator's function.
         *
         * @return function
         */
        public @NonNull TriFunction<Client, String, String, ? extends NameValue> getFunction() {
            return this.function;
        }
    }

    private final Client.WithManagement client;
    private final Map<String, Creator<NameValue>> registeredNames = new ConcurrentHashMap<>();

    /**
     * Constructs the processor.
     *
     * @param client client this will be used for
     */
    protected AbstractNameValueProcessor(Client.WithManagement client) {
        this.client = client;
    }

    /**
     * Gets the Client for which this processor functions.
     *
     * @return client
     */
    public Client.WithManagement getClient() {
        return this.client;
    }

    /**
     * Gets the actual registrations map, for manipulation.
     *
     * @return registration map
     */
    protected final Map<String, Creator<NameValue>> getRegistrations() {
        return this.registeredNames;
    }

    /**
     * Gets a registered creator function by registered name.
     *
     * @param name name to find
     * @return registered creator function
     */
    protected final @NonNull Optional<TriFunction<Client, String, String, ? extends NameValue>> getCreatorByName(@NonNull String name) {
        return this.optional(this.registeredNames.get(Sanity.nullCheck(name, "Name")));
    }

    /**
     * Registers a creator to a name.
     *
     * @param name name
     * @param creator creator
     * @return previous occupant of the name registration, if there was one
     */
    protected final @NonNull Optional<TriFunction<Client, String, String, ? extends NameValue>> registerCreator(@NonNull String name, @NonNull Creator<NameValue> creator) {
        return this.optional(this.registeredNames.put(Sanity.nullCheck(name, "Name"), creator));
    }

    /**
     * Removes registration of a creator to a name.
     *
     * @param name name
     * @return previous occupant of the name registration, if there was one
     */
    protected final @NonNull Optional<TriFunction<Client, String, String, ? extends NameValue>> unregisterCreator(@NonNull String name) {
        return this.optional(this.registeredNames.remove(Sanity.nullCheck(name, "Name")));
    }

    private @NonNull Optional<TriFunction<Client, String, String, ? extends NameValue>> optional(@Nullable Creator<NameValue> creator) {
        return (creator == null) ? Optional.empty() : Optional.of(creator.getFunction());
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).toString();
    }
}
