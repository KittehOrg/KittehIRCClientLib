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
package org.kitteh.irc.client.library.defaults.element.isupport;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.exception.KittehServerISupportException;

import java.util.Optional;

/**
 * Default implementation of {@link ISupportParameter.CaseMapping}.
 */
public class DefaultISupportCaseMapping extends DefaultISupportParameterValueRequired implements ISupportParameter.CaseMapping {
    private final org.kitteh.irc.client.library.feature.CaseMapping caseMapping;

    /**
     * Constructs the object.
     *
     * @param client client
     * @param name parameter name
     * @param value parameter value, if present
     */
    public DefaultISupportCaseMapping(@NonNull Client client, @NonNull String name, @Nullable String value) {
        super(client, name, value);
        Optional<org.kitteh.irc.client.library.feature.CaseMapping> caseMapping = org.kitteh.irc.client.library.feature.CaseMapping.getByName(value);
        if (caseMapping.isPresent()) {
            this.caseMapping = caseMapping.get();
        } else {
            throw new KittehServerISupportException(name, "Undefined casemapping");
        }
    }

    @Override
    public org.kitteh.irc.client.library.feature.@NonNull CaseMapping getCaseMapping() {
        return this.caseMapping;
    }
}
