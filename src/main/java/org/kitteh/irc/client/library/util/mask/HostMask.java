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
 * A mask that only cares about matching the host.
 */
public final class HostMask implements Mask {
    /**
     * Creates a host mask from the given user.
     *
     * @param user the user
     * @return the host mask from the user's host
     */
    @Nonnull
    public static HostMask fromUser(@Nonnull final User user) {
        Sanity.nullCheck(user, "user");
        return new HostMask(user.getHost());
    }

    /**
     * Creates a host mask from the given host.
     *
     * @param host the host
     * @return the host mask
     */
    @Nonnull
    public static HostMask fromHost(@Nonnull final String host) {
        Sanity.nullCheck(host, "host");
        return new HostMask(host);
    }

    private final String host;

    private HostMask(@Nonnull final String host) {
        this.host = Sanity.nullCheck(host, "host");
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
        return user.getHost().equals(this.host);
    }

    @Override
    public boolean test(@Nonnull final String host) {
        Sanity.nullCheck(host, "host");
        return host.equals(this.host);
    }

    @Nonnull
    @Override
    public String asString() {
        return "*!*@" + this.host; // TODO(kashike)
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        final HostMask that = (HostMask) other;
        return Objects.equals(this.host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.host);
    }
}
