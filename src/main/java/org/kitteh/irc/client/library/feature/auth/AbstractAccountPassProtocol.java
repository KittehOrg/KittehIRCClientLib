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
import org.kitteh.irc.client.library.feature.auth.element.Password;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

/**
 * Abstract general account/password protocol.
 */
public abstract class AbstractAccountPassProtocol extends AbstractAccountProtocol implements Password {
    private String password;

    /**
     * Creates an instance.
     *
     * @param client client
     * @param accountName account name
     * @param password password
     */
    protected AbstractAccountPassProtocol(@NonNull Client client, @NonNull String accountName, @NonNull String password) {
        super(client, accountName);
        this.password = Sanity.safeMessageCheck(password, "Password");
    }

    @Override
    public @NonNull String getPassword() {
        return this.password;
    }

    @Override
    public void setPassword(@NonNull String password) {
        this.password = Sanity.safeMessageCheck(password, "Password");
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).add("account", this.getAccountName()).add("pass", this.getPassword()).toString();
    }
}
