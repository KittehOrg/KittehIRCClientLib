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
package org.kitteh.irc.client.library.feature.auth;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.auth.element.AccountName;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;

/**
 * Abstract general account name protocol.
 */
public abstract class AbstractAccountProtocol implements AccountName {
    private final Client client;
    private String accountName;

    /**
     * Creates an instance.
     *
     * @param client client
     * @param accountName account name
     */
    protected AbstractAccountProtocol(@Nonnull Client client, @Nonnull String accountName) {
        Sanity.nullCheck(client, "Client cannot be null");
        Sanity.safeMessageCheck(accountName, "Account name");
        this.client = client;
        this.accountName = accountName;
    }

    @Nonnull
    @Override
    public Client getClient() {
        return this.client;
    }

    @Nonnull
    @Override
    public String getAccountName() {
        return this.accountName;
    }

    @Override
    public void setAccountName(@Nonnull String accountName) {
        Sanity.safeMessageCheck(accountName, "Account name");
        this.accountName = accountName;
    }

    @Override
    public final void startAuthentication() {
        this.client.sendRawLineImmediately(this.getAuthentication());
    }

    /**
     * Gets a String for {@link #startAuthentication()}.
     *
     * @return auth string
     */
    @Nonnull
    protected abstract String getAuthentication();

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("account", this.getAccountName()).toString();
    }
}
