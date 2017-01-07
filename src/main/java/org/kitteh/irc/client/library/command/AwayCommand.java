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
package org.kitteh.irc.client.library.command;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Sends an AWAY request to the server.
 */
public class AwayCommand extends Command {
    private String message;

    /**
     * Constructs the command.
     *
     * @param client the client
     * @throws IllegalArgumentException if client is null
     */
    public AwayCommand(@Nonnull Client client) {
        super(client);
    }

    /**
     * Sets the away message for the command.
     *
     * @param message away message or null to set as not away
     * @return this command
     * @throws IllegalArgumentException for invalid message
     */
    @Nonnull
    public AwayCommand message(@Nullable String message) {
        this.message = (message == null) ? null : Sanity.safeMessageCheck(message);
        return this;
    }

    /**
     * Removes the away message, meaning the command will set the Client as
     * not away.
     *
     * @return this command
     */
    @Nonnull
    public AwayCommand messageRemove() {
        this.message = null;
        return this;
    }

    @Override
    public void execute() {
        String exec = "AWAY";
        if (this.message != null) {
            exec += " :" + this.message;
        }
        this.getClient().sendRawLine(exec);
    }

    @Nonnull
    @Override
    protected ToStringer toStringer() {
        return super.toStringer().add("message", this.message);
    }
}
