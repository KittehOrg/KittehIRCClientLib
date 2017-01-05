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
package org.kitteh.irc.client.library.element;

import java.io.Closeable;
import java.net.SocketAddress;
import java.util.Optional;

/**
 * Represents an exchange using DCC.
 */
public interface DCCExchange extends Actor, Closeable {
    /**
     * @return the socket address of the local end
     */
    Optional<SocketAddress> getLocalSocketAddress();

    /**
     * @return the socket address of the remote end
     */
    Optional<SocketAddress> getRemoteSocketAddress();

    /**
     * @return {@code true} if the exchange is connected, otherwise
     *      {@code false}
     */
    boolean isConnected();

    /**
     * Closes this DCC exchange. It will no longer be
     * {@link #isConnected() connected}.
     *
     * <p>Note: Because this is a snapshot, the connected status
     * <em>will not</em> be updated on this object.</p>
     */
    @Override
    void close();
}
