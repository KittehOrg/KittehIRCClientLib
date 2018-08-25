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
package org.kitteh.irc.client.library.feature.auth;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.StringUtil;
import org.kitteh.irc.client.library.util.ToStringer;

/**
 * SASL PLAIN authentication. Automatically attempts auth during connection.
 */
public class SaslPlain extends AbstractAccountSaslProtocol {
    private final String password;

    /**
     * Creates an instance.
     *
     * @param client client
     * @param accountName account name
     * @param password password
     */
    public SaslPlain(@NonNull Client client, @NonNull String accountName, @NonNull String password) {
        super(client, "PLAIN", accountName);
        this.password = Sanity.safeMessageCheck(password, "Password");
    }

    @Override
    protected @NonNull String getAuthLine() {
        return this.getAccountName() + '\u0000' + this.getAccountName() + '\u0000' + this.password;
    }

    @Override
    protected void toString(final ToStringer stringer) {
        super.toString(stringer);
        stringer.add("password", StringUtil.filterPassword(this.password));
    }
}
