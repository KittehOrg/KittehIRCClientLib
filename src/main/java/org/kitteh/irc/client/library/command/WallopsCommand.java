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
package org.kitteh.irc.client.library.command;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;

/**
 * Sends a WALLOPS message to the server.
 */
public class WallopsCommand extends Command {
    private String message;

    /**
     * Constructs the command.
     *
     * @param client the client
     * @throws IllegalArgumentException if client is null
     */
    public WallopsCommand(@Nonnull Client client) {
        super(client);
    }

    /**
     * Sets the message to send.
     *
     * @param message message
     * @return this command
     * @throws IllegalArgumentException for invalid target
     */
    @Nonnull
    public WallopsCommand message(@Nonnull String message) {
        this.message = Sanity.safeMessageCheck(message, "message");
        return this;
    }

    @Override
    public void execute() {
        if (this.message == null) {
            throw new IllegalStateException("Message not defined");
        }
        this.getClient().sendRawLine("WALLOPS :" + this.message);
    }

    @Override
    public String toString() {
        return new ToStringer(this).add("client", this.getClient()).add("message", this.message).toString();
    }
}
