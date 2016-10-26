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
package org.kitteh.irc.client.library.feature.sts;

import java.util.Map;
import java.util.Optional;

/**
 * Represents a manager that can persist STS policies in some form.
 */
public interface STSStorageManager {
    /**
     * Adds an STS policy to the store.
     *
     * @param hostname the hostname (as sent in the SNI by the client)
     * @param duration the length (in seconds) until the expiry of this stored policy
     * @param data all data sent by the server in the CAP LS "sts" value when connecting securely
     */
    void addEntry(String hostname, long duration, Map<String, Optional<String>> data);

    /**
     * Gets an STS policy from the store, looking it up via hostname.
     *
     * @param hostname the hostname (as sent in the SNI by the client)
     * @return all data sent by the server in the CAP LS "sts" value when we connected securely
     */
    Map<String, Optional<String>> getEntry(String hostname);

    /**
     * Checks if a policy has been stored for the hostname.
     *
     * @param hostname the hostname to check
     * @return whether the entry exists in the store
     */
    boolean hasEntry(String hostname);

    /**
     * Deletes an entry from the store (used for 0 duration policies).
     * <p>
     * Implementers must ignore requests to remove entries that do not exist.
     *
     * @param hostname the hostname to remove the policy for
     */
    void removeEntry(String hostname);
}
