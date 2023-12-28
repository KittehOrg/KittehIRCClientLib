/*
 * * Copyright (C) 2013-2023 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.exception;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Thrown in reaction to exceptions related to the connection.
 */
public class KittehConnectionException extends Exception {
    private final boolean fatal;

    /**
     * Constructs this event.
     *
     * @param cause the cause
     * @param fatal true if the exception indicates death of the connection
     */
    public KittehConnectionException(@NonNull Throwable cause, boolean fatal) {
        super(cause);
        this.fatal = fatal;
    }

    /**
     * Gets if the connection has died.
     *
     * @return true if the exception indicates death of the connection
     */
    public boolean isFatal() {
        return this.fatal;
    }
}
