/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
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

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Represents a manager that can persist STS policies in some form.
 */
public interface StsStorageManager {
    /**
     * Adds an STS policy to the store.
     * <p>
     * Implementers MUST accept requests to add entries that already exist and overwrite the old entry.
     *
     * @param hostname the hostname (as sent in the SNI by the client)
     * @param duration the length (in seconds) until the expiry of this stored policy
     * @param policy the STS policy instance, including all data sent from the server
     */
    void addEntry(@Nonnull String hostname, long duration, @Nonnull StsPolicy policy);

    /**
     * Gets an STS policy from the store, looking it up via hostname.
     *
     * @param hostname the hostname (as sent in the SNI by the client)
     * @return all data sent by the server in the CAP LS "sts" value when we connected securely
     */
    @Nonnull
    Optional<StsPolicy> getEntry(@Nonnull String hostname);

    /**
     * Checks if a policy has been stored for the hostname.
     *
     * @param hostname the hostname to check
     * @return whether the entry exists in the store
     */
    boolean hasEntry(@Nonnull String hostname);

    /**
     * Deletes an entry from the store (used for 0 duration policies).
     * <p>
     * Implementers MUST ignore requests to remove entries that do not exist.
     *
     * @param hostname the hostname to remove the policy for
     */
    void removeEntry(@Nonnull String hostname);
}
