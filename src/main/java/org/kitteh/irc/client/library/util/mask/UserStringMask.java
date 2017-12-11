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

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A mask that only cares about matching the user string.
 */
public final class UserStringMask implements Mask {
    /**
     * Creates a user string mask from the given user.
     *
     * @param user the user
     * @return the nick mask from the user's user string
     */
    @Nonnull
    public static UserStringMask fromUser(@Nonnull final User user) {
        Sanity.nullCheck(user, "user");
        return new UserStringMask(user.getUserString());
    }

    /**
     * Creates a user string mask from the given user string.
     *
     * @param userString the userString
     * @return the userString mask
     */
    @Nonnull
    public static UserStringMask fromUserString(@Nonnull final String userString) {
        Sanity.nullCheck(userString, "userString");
        return new UserStringMask(userString);
    }

    private final String userString;

    private UserStringMask(@Nonnull final String userString) {
        this.userString = Sanity.nullCheck(userString, "userString");
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

    @Override
    public boolean test(@Nonnull final User user) {
        Sanity.nullCheck(user, "user");
        return user.getUserString().equals(this.userString);
    }

    @Override
    public boolean test(@Nonnull final String userString) {
        Sanity.nullCheck(userString, "userString");
        return userString.equals(this.userString);
    }

    @Nonnull
    @Override
    public String asString() {
        return "*!" + this.userString + "@*"; // TODO(kashike)
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        final UserStringMask that = (UserStringMask) other;
        return Objects.equals(this.userString, that.userString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.userString);
    }
}
