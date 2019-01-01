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
package org.kitteh.irc.client.library.feature;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.feature.auth.AuthProtocol;
import org.kitteh.irc.client.library.feature.auth.element.EventListening;

import java.util.Optional;
import java.util.Set;

/**
 * Manages Authentication
 */
public interface AuthManager {
    /**
     * Adds a protocol to be handled by this manager. Event handlers will be
     * registered if it implements {@link EventListening} and if a previously
     * set protocol of same class exists it will be removed.
     *
     * @param protocol protocol to add
     * @return displaced protocol of same class
     * @throws IllegalArgumentException if the protocol is for another Client
     */
    @NonNull Optional<AuthProtocol> addProtocol(@NonNull AuthProtocol protocol);

    /**
     * Gets all protocols currently registered to the manager.
     *
     * @return all protocols
     */
    @NonNull Set<AuthProtocol> getProtocols();

    /**
     * Removes a protocol if it was registered, unregistering event handlers
     * on it if registered.
     *
     * @param protocol protocol to remove
     */
    void removeProtocol(@NonNull AuthProtocol protocol);
}
