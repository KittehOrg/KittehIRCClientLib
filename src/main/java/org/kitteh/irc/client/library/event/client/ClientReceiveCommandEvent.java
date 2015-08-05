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
import org.kitteh.irc.client.library.event.abstractbase.ActorEventBase;
import org.kitteh.irc.client.library.util.CommandFilter;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Fires when the client receives a command message. Note that the client
 * itself listens to this event internally to fire events at an mBassador
 * priority of Integer.MAX_VALUE - 1. If you wish to beat the client to
 * listening to a command, listen at priority INTEGER.MAX_VALUE.
 *
 * @see CommandFilter for filtering by commands
 */
public class ClientReceiveCommandEvent extends ActorEventBase<Actor> {
    private final String[] args;
    private final String command;
    private final String originalMessage;

    /**
     * Constructs the event.
     *
     * @param client client
     * @param originalMessage original message
     * @param actor actor
     * @param command command
     * @param args args
     */
    public ClientReceiveCommandEvent(@Nonnull Client client, @Nonnull String originalMessage, @Nonnull Actor actor, @Nonnull String command, @Nonnull String[] args) {
        super(client, actor);
        this.args = args;
        this.command = command;
        this.originalMessage = originalMessage;
    }

    /**
     * Gets the subsequent arguments after the command.
     *
     * @return arguments
     */
    @Nonnull
    public String[] getArgs() {
        return Arrays.copyOf(this.args, this.args.length);
    }

    /**
     * Gets the command sent.
     *
     * @return command, upper-case
     */
    @Nonnull
    public String getCommand() {
        return this.command;
    }

    /**
     * Gets the original message received by the server.
     *
     * @return unprocessed, original message
     */
    @Nonnull
    public String getOriginalMessage() {
        return this.originalMessage;
    }
}