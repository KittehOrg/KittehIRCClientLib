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

import javax.annotation.Nonnull;

/**
 * Manages authentication.
 */
public interface AuthManager {
    /**
     * Attempts to authenticate with the server's authentication system as
     * defined in this manager.
     *
     * @throws IllegalStateException if auth type or details are not set
     * @throws IllegalStateException if authentication cannot occur at this
     * time
     * @see ClientBuilder#auth(AuthType, String, String) to set in builder
     * @see #setAuthType(AuthType) to set authentication type
     * @see #setPassword(String) to set password
     * @see #setUsername(String) to set username
     */
    void authenticate();

    /**
     * Attempts to reclaim a nickname.
     *
     * @param nick nickname to reclaim
     * @throws UnsupportedOperationException if not supported by the selected
     * {@link AuthType}
     */
    void reclaimNick(@Nonnull String nick);

    /**
     * Sets the authentication type to utilize.
     *
     * @param authType authentication type
     * @throws IllegalArgumentException if authType is null
     */
    void setAuthType(@Nonnull AuthType authType);

    /**
     * Sets the authentication password.
     *
     * @param password password
     * @throws IllegalArgumentException if password is null or contains
     * spaces/invalid characters
     */
    void setPassword(@Nonnull String password);

    /**
     * Sets the authentication password.
     *
     * @param username username
     * @throws IllegalArgumentException if username is null or contains
     * spaces/invalid characters
     */
    void setUsername(@Nonnull String username);
}