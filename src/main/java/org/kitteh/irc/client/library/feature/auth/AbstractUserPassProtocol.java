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
import org.kitteh.irc.client.library.feature.auth.element.Password;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;

/**
 * Abstract general username/password protocol.
 */
public abstract class AbstractUserPassProtocol extends AbstractUserProtocol implements Password {
    private String password;

    /**
     * Creates an instance.
     *
     * @param client client
     * @param username username
     * @param password password
     */
    protected AbstractUserPassProtocol(@Nonnull Client client, @Nonnull String username, @Nonnull String password) {
        super(client, username);
        Sanity.safeMessageCheck(password, "Password");
        this.password = password;
    }

    @Nonnull
    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public void setPassword(@Nonnull String password) {
        Sanity.safeMessageCheck(password, "Password");
        this.password = password;
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("user", this.getUsername()).add("pass", this.getPassword()).toString();
    }
}
