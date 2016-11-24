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
package org.kitteh.irc.client.library.feature;

/**
 * An enum that maps out all possible outbound messages between KICL and the
 * server that we wish to provide a custom default message for.
 */
public enum DefaultingOutboundMessage {
    /**
     * When a KickCommand has no reason supplied.
     */
    KICK,
    /**
     * When a channel is parted with no reason supplied.
     */
    PART,
    /**
     * When KICL triggers a reconnect neither due to connection loss with the
     * server, nor due to an exception.
     */
    RECONNECT,
    /**
     * An user specified shutdown.
     */
    QUIT,
    /**
     * When KICL thinks it has lost connection with the server, it will
     * attempt to reconnect with this value. Does not work when it is
     * the server that is timing KICL out (e.g. because you lose
     * internet connection)
     */
    QUIT_PING_TIMEOUT,
    /**
     * When KICL experiences an IO exception it will attempt to reconnect
     * to the server with this value.
     */
    QUIT_INTERNAL_EXCEPTION,
}
