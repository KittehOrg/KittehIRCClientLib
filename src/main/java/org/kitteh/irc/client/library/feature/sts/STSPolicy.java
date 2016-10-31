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
import java.util.Set;

/**
 * Simple POJO to represent an STS policy.
 */
public class STSPolicy {

    private final Map<String, String> options;
    private final Set<String> flags;


    /**
     * Constucts a policy.
     *
     * @param options the key-value options from the server-sent policy
     * @param flags the valueless flags from the server-sent policy
     */
    public STSPolicy(Map<String, String> options, Set<String> flags) {
        this.options = options;
        this.flags = flags;
    }

    /**
     * @return the valueless flags from the server-sent policy
     */
    public Set<String> getFlags() {
        return flags;
    }

    /**
     * @return he key-value options from the server-sent policy
     */
    public Map<String, String> getOptions() {
        return options;
    }
}
