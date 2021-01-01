/*
 * * Copyright (C) 2013-2021 Matt Baxter https://kitteh.org
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.value.qual.IntRange;

import java.util.Objects;

/**
 * A host and a port. A love story.
 */
public class HostWithPort {
    /**
     * Minimum acceptable port value: 0.
     */
    public static final int PORT_MIN = 0;
    /**
     * Maximum acceptable port value: 65535.
     */
    public static final int PORT_MAX = 65535;

    /**
     * Constructs a HostWithPort with the given host and port.
     *
     * @param host host
     * @param port port
     * @return a HostWithPort with the provided information
     */
    public static @NonNull HostWithPort of(@NonNull String host, @IntRange(from = HostWithPort.PORT_MIN, to = HostWithPort.PORT_MAX) int port) {
        Sanity.nullCheck(host, "Host");
        Sanity.truthiness((port >= HostWithPort.PORT_MIN) && (port <= HostWithPort.PORT_MAX), port + " is not acceptable port number");
        return new HostWithPort(host, port);
    }

    private final String host;
    private final int port;

    private HostWithPort(@NonNull String host, @IntRange(from = HostWithPort.PORT_MIN, to = HostWithPort.PORT_MAX) int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Gets the host.
     *
     * @return host
     */
    public @NonNull String getHost() {
        return this.host;
    }

    /**
     * Gets the port.
     *
     * @return port
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Returns a new instance with the given host and this object's port.
     *
     * @param host new host
     * @return new instance
     */
    public @NonNull HostWithPort withHost(@NonNull String host) {
        return HostWithPort.of(host, this.port);
    }

    /**
     * Returns a new instance with the given port and this object's host.
     *
     * @param port new port
     * @return new instance
     */
    public @NonNull HostWithPort withPort(@IntRange(from = HostWithPort.PORT_MIN, to = HostWithPort.PORT_MAX) int port) {
        return HostWithPort.of(this.host, port);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if ((other == null) || (this.getClass() != other.getClass())) {
            return false;
        }
        final HostWithPort that = (HostWithPort) other;
        return (this.port == that.port) && Objects.equals(this.host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.host, this.port);
    }

    @Override
    public String toString() {
        return new ToStringer(this)
                .add("host", this.host)
                .add("port", this.port)
                .toString();
    }
}
