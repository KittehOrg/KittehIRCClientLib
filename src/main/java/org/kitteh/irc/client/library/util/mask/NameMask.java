/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.util.mask;

import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.util.Sanity;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * A mask that cares about the nick, user string, and host.
 */
public final class NameMask implements Mask.AsString {
    /**
     * Creates a name mask from the given user.
     *
     * @param user the user
     * @return the name mask from the user's host
     */
    @Nonnull
    public static NameMask fromUser(@Nonnull final User user) {
        Sanity.nullCheck(user, "user");
        return new NameMask(user.getNick(), user.getUserString(), user.getHost());
    }

    private final String nick;
    private final String userString;
    private final String host;

    private NameMask(final String nick, final String userString, final String host) {
        this.nick = nick;
        this.userString = userString;
        this.host = host;
    }

    /**
     * Gets the nick.
     *
     * @return the nick
     */
    @Nonnull
    public String getNick() {
        return this.nick;
    }

    /**
     * Gets the user string.
     *
     * @return the user string
     */
    @Nonnull
    public String getUserString() {
        return this.userString;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    @Nonnull
    public String getHost() {
        return this.host;
    }

    @Override
    public boolean test(@Nonnull final User user) {
        Sanity.nullCheck(user, "user");
        return user.getNick().equals(this.nick)
            && user.getUserString().equals(this.userString)
            && user.getHost().equals(this.host);
    }

    @Override
    public boolean test(@Nonnull final String string) {
        Sanity.nullCheck(string, "string");
        return this.asString().equals(string);
    }

    @Nonnull
    @Override
    public String asString() {
        return this.nick + '!' + this.userString + '@' + this.host;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        final NameMask that = (NameMask) other;
        return Objects.equals(this.nick, that.nick)
            && Objects.equals(this.userString, that.userString)
            && Objects.equals(this.host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.nick, this.userString, this.host);
    }
}
