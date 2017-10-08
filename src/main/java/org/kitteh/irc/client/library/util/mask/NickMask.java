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
package org.kitteh.irc.client.library.util.mask;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.util.Sanity;

import java.util.Objects;

/**
 * A mask that only cares about matching the nick.
 */
public final class NickMask implements Mask.AsString {
    /**
     * Creates a nick mask from the given user.
     *
     * @param user the user
     * @return the nick mask from the user's host
     */
    public static @NonNull NickMask fromUser(final @NonNull User user) {
        Sanity.nullCheck(user, "user");
        return new NickMask(user.getNick());
    }

    /**
     * Creates a nick mask from the given nick.
     *
     * @param nick the nick
     * @return the nick mask
     */
    public static @NonNull NickMask fromNick(final @NonNull String nick) {
        Sanity.nullCheck(nick, "nick");
        return new NickMask(nick);
    }

    private final String nick;

    private NickMask(final @NonNull String nick) {
        this.nick = Sanity.nullCheck(nick, "nick");
    }

    /**
     * Gets the nick.
     *
     * @return the nick
     */
    public @NonNull String getNick() {
        return this.nick;
    }

    @Override
    public boolean test(final @NonNull User user) {
        Sanity.nullCheck(user, "user");
        return user.getNick().equals(this.nick);
    }

    @Override
    public boolean test(final @NonNull String nick) {
        Sanity.nullCheck(nick, "nick");
        return nick.equals(this.nick);
    }

    @Override
    public @NonNull String asString() {
        return this.nick + '!' + WILDCARD_STRING + '@' + WILDCARD_STRING;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        final NickMask that = (NickMask) other;
        return Objects.equals(this.nick, that.nick);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.nick);
    }
}
