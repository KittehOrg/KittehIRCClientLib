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
package org.kitteh.irc.client.library.feature;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.util.TriFunction;

import java.util.Optional;

/**
 * Manages processing of ISUPPORT parameters.
 */
public interface ISupportManager {
    /**
     * Creates a {@link ISupportParameter} from given input.
     *
     * @param tag input
     * @return a parameter based on the input
     */
    @NonNull ISupportParameter createParameter(@NonNull String tag);

    /**
     * Gets the registered ISUPPORT creator for a given parameter name.
     *
     * @param parameter parameter
     * @return registered creator if present
     */
    @NonNull Optional<TriFunction<Client, String, String, ? extends ISupportParameter>> getCreator(@NonNull String parameter);

    /**
     * Registers a function that creates an {@link ISupportParameter} from a
     * given parameter and value.
     *
     * @param parameter name of the parameter to register
     * @param creator function that creates tags
     * @return displaced creator if one existed for the given parameter
     */
    @NonNull Optional<TriFunction<Client, String, String, ? extends ISupportParameter>> registerParameter(@NonNull String parameter, @NonNull TriFunction<Client, String, String, ? extends ISupportParameter> creator);

    /**
     * Removes the registered creator for a given parameter.
     *
     * @param parameter parameter
     * @return registered creator if present
     */
    @NonNull Optional<TriFunction<Client, String, String, ? extends ISupportParameter>> unregisterParameter(@NonNull String parameter);
}
