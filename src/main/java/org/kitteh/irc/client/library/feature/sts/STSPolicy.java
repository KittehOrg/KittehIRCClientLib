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

import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

/**
 * Simple POJO to represent an STS policy.
 */
public class STSPolicy {
    public static final String POLICY_OPTION_KEY_PORT = "port";
    public static final String POLICY_OPTION_KEY_DURATION = "duration";
    private final Map<String, String> options;
    private final Set<String> flags;


    /**
     * Constucts a policy.
     *
     * @param options the key-value options from the server-sent policy
     * @param flags the valueless flags from the server-sent policy
     */
    public STSPolicy(@Nonnull Map<String, String> options, @Nonnull Set<String> flags) {
        this.options = Sanity.nullCheck(options, "Must provide a valid options map");
        this.flags = Sanity.nullCheck(flags, "Must provide a valid flags set");
    }

    /**
     * @return the valueless flags from the server-sent policy
     */
    @Nonnull
    public Set<String> getFlags() {
        return this.flags;
    }

    /**
     * @return the key-value options from the server-sent policy
     */
    @Nonnull
    public Map<String, String> getOptions() {
        return this.options;
    }
}
