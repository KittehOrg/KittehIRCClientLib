/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library;

import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;

final class IRCAuthManager implements AuthManager {
    private final Client client;
    private AuthType authType;
    private String username;
    private String password;

    IRCAuthManager(@Nonnull Client client) {
        this.client = client;
    }

    @Override
    public void authenticate() {
        if (this.username == null) {
            throw new IllegalStateException("No username has been defined.");
        }
        if (this.password == null) {
            throw new IllegalStateException("No password has been defined.");
        }
        this.authType.authenticate(this.client, this.username, this.password);
    }

    @Override
    public void reclaimNick(@Nonnull String nick) {
        Sanity.nullCheck(nick, "Nickname cannot be null");
        this.authType.reclaimNick(this.client, nick);
    }

    @Override
    public void setAuthType(@Nonnull AuthType authType) {
        Sanity.nullCheck(authType, "Authentication type cannot be null. See AuthType.DISABLED");
        this.authType = authType;
    }

    @Override
    public void setPassword(@Nonnull String password) {
        Sanity.nullCheck(password, "Password cannot be null");
        Sanity.safeMessageCheck(password, "authentication password");
        this.password = password;
    }

    @Override
    public void setUsername(@Nonnull String username) {
        Sanity.nullCheck(username, "Username cannot be null");
        Sanity.safeMessageCheck(username, "authentication username");
        this.username = username;
    }
}