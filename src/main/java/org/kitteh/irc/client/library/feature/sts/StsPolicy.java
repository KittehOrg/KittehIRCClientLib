/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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

import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple POJO to represent an STS policy.
 */
public class StsPolicy {
    /**
     * Name of the secure port key as defined in the spec.
     */
    public static final String POLICY_OPTION_KEY_PORT = "port";
    /**
     * Name of the policy's duration (in seconds) key as defined in the spec.
     */
    public static final String POLICY_OPTION_KEY_DURATION = "duration";
    private final ConcurrentMap<String, String> options;
    private final Set<String> flags;

    /**
     * Constructs a policy.
     *
     * @param options the key-value options from the server-sent policy
     * @param flags the valueless flags from the server-sent policy
     */
    public StsPolicy(@Nonnull Map<String, String> options, @Nonnull Set<String> flags) {
        this.options = new ConcurrentHashMap<>(Sanity.nullCheck(options, "Must provide a valid options map"));
        this.flags = Collections.synchronizedSet(new HashSet<>(Sanity.nullCheck(flags, "Must provide a valid flags set")));
    }

    /**
     * Get the set of flags in the policy.
     *
     * @return the valueless flags from the server-sent policy
     */
    @Nonnull
    public Set<String> getFlags() {
        return this.flags;
    }

    /**
     * Get the map of options to their values (excludes flags which have no value).
     *
     * @return the key-value options from the server-sent policy
     */
    @Nonnull
    public Map<String, String> getOptions() {
        return this.options;
    }
}
