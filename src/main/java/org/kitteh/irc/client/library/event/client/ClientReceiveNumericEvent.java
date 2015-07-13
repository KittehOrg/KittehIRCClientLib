/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.event.client;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.event.abstractbase.ClientEventBase;
import org.kitteh.irc.client.library.util.NumericFilter;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Fires when the client receives a numeric coded message. Note that the
 * client itself listens to this event internally to fire events at an
 * mBassador priority of Integer.MAX_VALUE - 1. If you wish to beat the
 * client to listening to a numeric, listen at priority INTEGER.MAX_VALUE.
 *
 * @see NumericFilter for filtering by numerics
 */
public class ClientReceiveNumericEvent extends ClientEventBase {
    private final String[] args;
    private final int numeric;
    private final Actor server;

    /**
     * Constructs the event.
     *
     * @param client client
     * @param server server
     * @param numeric numeric
     * @param args args
     */
    public ClientReceiveNumericEvent(@Nonnull Client client, @Nonnull Actor server, int numeric, @Nonnull String[] args) {
        super(client);
        this.args = args;
        this.numeric = numeric;
        this.server = server;
    }

    /**
     * Gets the subsequent arguments after the numeric.
     *
     * @return arguments
     */
    @Nonnull
    public String[] getArgs() {
        return Arrays.copyOf(this.args, this.args.length);
    }

    /**
     * Gets the numeric code sent.
     *
     * @return numeric
     */
    public int getNumeric() {
        return this.numeric;
    }

    /**
     * Gets the server sending this message.
     *
     * @return server
     */
    @Nonnull
    public Actor getServer() {
        return this.server;
    }
}