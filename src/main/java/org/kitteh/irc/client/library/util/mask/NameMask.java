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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.util.Sanity;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A mask that cares about the nick, user string, and host.
 */
public final class NameMask implements Mask.AsString {
    // Valid nick chars: \w\[]^`{}|-_
    // Pattern unescaped: ([\w\\\[\]\^`\{\}\|\-_]+)!([~\w]+)@([\w\.\-:]+)
    // You know what? Screw it.
    // Let's just do it assuming no IRCD can handle following the rules.
    // New pattern: ([^!@]+)!([^!@]+)@([^!@]+)
    /**
     * TODO
     */
    public static final Pattern PATTERN = Pattern.compile("([^!@]+)!([^!@]+)@([^!@]+)");

    /**
     * Creates a name mask from the given user.
     *
     * @param user the user
     * @return the name mask from the user's host
     */
    public static @NonNull NameMask fromUser(final @NonNull User user) {
        Sanity.nullCheck(user, "user");
        return new NameMask(user.getNick(), user.getUserString(), user.getHost());
    }

    /**
     * Creates a name mask from the given string.
     *
     * @param string the string
     * @return the name mask from the string
     */
    public static @NonNull NameMask fromString(final @NonNull String string) {
        Sanity.nullCheck(string, "string");
        final @Nullable String nick;
        final @Nullable String userString;
        final @Nullable String host;
        final Matcher matcher = PATTERN.matcher(string);
        if (matcher.matches()) {
            nick = matcher.group(1);
            userString = matcher.group(1);
            host = matcher.group(1);
        } else {
            nick = null;
            userString = null;
            host = null;
        }
        return new NameMask(nick, userString, host);
    }

    /**
     * Tests if two strings are equal, defaulting to {@code true} if {@code b} is {@code null}.
     *
     * @param a the first string
     * @param b the second string
     * @return {@code true} if equal
     */
    private static boolean equals(final @NonNull String a, final @Nullable String b) {
        return (b == null) || a.equals(b);
    }

    private final @Nullable String nick;
    private final @Nullable String userString;
    private final @Nullable String host;

    private NameMask(final @Nullable String nick, final @Nullable String userString, final @Nullable String host) {
        this.nick = nick;
        this.userString = userString;
        this.host = host;
    }

    /**
     * Gets the nick.
     *
     * @return the nick
     */
    public @NonNull Optional<String> getNick() {
        return Optional.ofNullable(this.nick);
    }

    /**
     * Gets the user string.
     *
     * @return the user string
     */
    public @NonNull Optional<String> getUserString() {
        return Optional.ofNullable(this.userString);
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public @NonNull Optional<String> getHost() {
        return Optional.ofNullable(this.host);
    }

    @Override
    public boolean test(final @NonNull User user) {
        Sanity.nullCheck(user, "user");
        return equals(user.getNick(), this.nick)
            && equals(user.getUserString(), this.userString)
            && equals(user.getHost(), this.host);
    }

    @Override
    public boolean test(final @NonNull String string) {
        Sanity.nullCheck(string, "string");
        return this.asString().equals(string);
    }

    @Override
    public @NonNull String asString() {
        return this.getNick().orElse(WILDCARD_STRING) + '!' + this.getUserString().orElse(WILDCARD_STRING) + '@' + this.getHost().orElse(WILDCARD_STRING);
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
