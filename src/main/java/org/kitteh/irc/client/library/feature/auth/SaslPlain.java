/*
 * * Copyright (C) 2013-2018 Matt Baxter http://kitteh.org
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

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.auth.element.Password;

import javax.annotation.Nonnull;

/**
 * SASL PLAIN authentication. Automatically attempts auth during connection.
 */
public class SaslPlain extends AbstractSaslProtocol<String> implements Password {
    /**
     * Creates an instance.
     *
     * @param client client
     * @param accountName account name
     * @param password password
     */
    public SaslPlain(@Nonnull Client client, @Nonnull String accountName, @Nonnull String password) {
        super(client, accountName, password, "PLAIN");
    }

    @Nonnull
    @Override
    protected String getAuthLine() {
        return this.getAccountName() + '\u0000' + this.getAccountName() + '\u0000' + this.getPassword();
    }

    @Nonnull
    @Override
    public String getPassword() {
        return this.getAuthValue();
    }

    @Override
    public void setPassword(@Nonnull String password) {
        this.setAuthValue(password);
    }
}
