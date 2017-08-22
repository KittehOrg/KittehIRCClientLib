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
package org.kitteh.irc.client.library.util;

import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents a mask that can match a {@link User}.
 */
public class Mask implements Predicate<User> {
    /**
     * Creates a Mask from a given String.
     *
     * @param string string
     * @return mask from string
     */
    @Nonnull
    public static Mask fromString(@Nonnull String string) {
        Sanity.nullCheck(string, "String cannot be null");
        return new Mask(string);
    }

    /**
     * Creates a Mask from a given User.
     *
     * @param user user
     * @return mask from user
     */
    @Nonnull
    public static Mask fromUser(@Nonnull User user) {
        Sanity.nullCheck(user, "User cannot be null");
        return new Mask(user.getHost(), user.getNick(), user.getUserString());
    }

    /**
     * Creates a Mask from the given nick.
     *
     * @param nick nick
     * @return mask from nick
     */
    @Nonnull
    public static Mask fromNick(@Nonnull String nick) {
        Sanity.nullCheck(nick, "Nick cannot be null");
        return new Mask(nick, null, null);
    }

    /**
     * Creates a Mask from the given user string.
     *
     * @param user user
     * @return mask from user
     */
    @Nonnull
    public static Mask fromUserString(@Nonnull String user) {
        Sanity.nullCheck(user, "User cannot be null");
        return new Mask(null, user, null);
    }

    /**
     * Creates a Mask from the given host.
     *
     * @param host host
     * @return mask from host
     */
    @Nonnull
    public static Mask fromHost(@Nonnull String host) {
        Sanity.nullCheck(host, "Host cannot be null");
        return new Mask(null, null, host);
    }

    // Valid nick chars: \w\[]^`{}|-_
    // Pattern unescaped: ([\w\\\[\]\^`\{\}\|\-_]+)!([~\w]+)@([\w\.\-:]+)
    // You know what? Screw it.
    // Let's just do it assuming no IRCD can handle following the rules.
    // New pattern: ([^!@]+)!([^!@]+)@([^!@]+)
    public static final Pattern NICK_PATTERN = Pattern.compile("([^!@]+)!([^!@]+)@([^!@]+)");
    public static final char WILDCARD_CHARACTER = '*';
    public static final String WILDCARD = String.valueOf(WILDCARD_CHARACTER);
    protected final Optional<String> nick;
    protected final Optional<String> user;
    protected final Optional<String> host;
    protected final Pattern pattern;

    protected Mask(@Nonnull String string) {
        Sanity.nullCheck(string, "String cannot be null");

        @Nullable String nick = null;
        @Nullable String user = null;
        @Nullable String host = null;

        final Matcher matcher = NICK_PATTERN.matcher(string);
        if (matcher.matches()) {
            nick = matcher.group(1);
            user = matcher.group(2);
            host = matcher.group(3);
        }

        this.nick = Optional.ofNullable(nick);
        this.user = Optional.ofNullable(user);
        this.host = Optional.ofNullable(host);
        this.pattern = StringUtil.wildcardToPattern(string);
    }

    protected Mask(@Nullable String nick, @Nullable String user, @Nullable String host) {
        this.nick = Optional.ofNullable(nick);
        this.user = Optional.ofNullable(user);
        this.host = Optional.ofNullable(host);
        this.pattern = this.resolvePattern();
    }

    @Nonnull
    protected Pattern resolvePattern() {
        String pattern = this.asString();
        return StringUtil.wildcardToPattern(pattern);
    }

    /**
     * Gets the nick component of this mask.
     *
     * @return nick component if known
     */
    @Nonnull
    public Optional<String> getNick() {
        return this.nick;
    }

    /**
     * Gets the user component of this mask.
     *
     * @return user component if known
     */
    @Nonnull
    public Optional<String> getUser() {
        return this.user;
    }

    /**
     * Gets the host component of this mask.
     *
     * @return host component if known
     */
    @Nonnull
    public Optional<String> getHost() {
        return this.host;
    }

    /**
     * Gets the String representation of this mask.
     *
     * @return string
     */
    @Nonnull
    public String asString() {
        return this.getNick().orElse(WILDCARD) + '!' + this.getUser().orElse(WILDCARD) + '@' + this.getHost().orElse(WILDCARD);
    }

    /**
     * Gets a set of users that match this mask in the provided channel.
     *
     * @param channel channel
     * @return set of users that match this mask
     */
    @Nonnull
    public Set<User> getMatches(@Nonnull Channel channel) {
        Sanity.nullCheck(channel, "Channel cannot be null");
        return channel.getUsers().stream().filter(this).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Gets if the user matches this mask.
     *
     * @param user user
     * @return true if user matches this mask
     */
    @Override
    public boolean test(@Nonnull User user) {
        Sanity.nullCheck(user, "User cannot be null");
        return this.test(user.getName());
    }

    /**
     * Gets if the string matches this mask.
     *
     * @param string string
     * @return true if string matches this mask
     */
    public boolean test(@Nonnull String string) {
        Sanity.nullCheck(string, "String cannot be null");
        return this.pattern.matcher(string).matches();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getNick(), this.getUser(), this.getHost());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Mask)) {
            return false;
        }

        final Mask that = (Mask) o;
        return Objects.equals(this.getNick(), that.getNick()) && Objects.equals(this.getUser(), that.getUser()) && Objects.equals(this.getHost(), that.getHost());
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("nick", this.getNick()).add("user", this.getUser()).add("host", this.getHost()).add("pattern", this.pattern).toString();
    }
}
