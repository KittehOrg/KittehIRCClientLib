/*
 * * Copyright (C) 2013-2018 Matt Baxter http://kitteh.org
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
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ClientReceiveServerMessageEventBase;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Fires when the client receives a numeric coded message. Note that the
 * client itself listens to this event internally to fire events at an
 * mBassador priority of Integer.MAX_VALUE - 1. If you wish to beat the
 * client to listening to a numeric, listen at priority INTEGER.MAX_VALUE.
 *
 * @see NumericFilter
 */
public class ClientReceiveNumericEvent extends ClientReceiveServerMessageEventBase {
    private final int numeric;

    /**
     * Constructs the event.
     *
     * @param client client
     * @param serverMessage server message
     * @param server server
     * @param command command
     * @param numeric numeric
     * @param args args
     */
    public ClientReceiveNumericEvent(@Nonnull Client client, @Nonnull ServerMessage serverMessage, @Nonnull Actor server, String command, int numeric, @Nonnull List<String> args) {
        super(client, serverMessage, server, command, args);
        this.numeric = numeric;
    }

    /**
     * Gets the numeric code sent.
     *
     * @return numeric
     */
    public int getNumeric() {
        return this.numeric;
    }
}
