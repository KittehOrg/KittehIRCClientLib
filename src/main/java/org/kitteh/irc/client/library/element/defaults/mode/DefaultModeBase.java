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
package org.kitteh.irc.client.library.element.defaults.mode;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.mode.Mode;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;

/**
 * Abstract base class for modes.
 */
public abstract class DefaultModeBase implements Mode {
    private final Client client;
    private final char mode;

    /**
     * Constructs and such.
     *
     * @param client client
     * @param mode character
     */
    protected DefaultModeBase(@Nonnull Client client, char mode) {
        this.client = client;
        this.mode = mode;
    }

    @Override
    @Nonnull
    public Client getClient() {
        return this.client;
    }

    @Override
    public char getChar() {
        return this.mode;
    }

    @Nonnull
    @Override
    public String toString() {
        return this.toStringer().toString();
    }

    @Nonnull
    protected ToStringer toStringer() {
        return new ToStringer(this).add("client", this.getClient()).add("char", this.getChar());
    }
}
