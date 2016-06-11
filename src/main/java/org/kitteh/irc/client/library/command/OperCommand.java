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
 * Sends an OPER request to the server - MAKE SURE IT'S YOUR SERVER!
 */
public class OperCommand extends Command {
    private String user;
    private String password;

    /**
     * Constructs the command.
     *
     * @param client the client
     * @throws IllegalArgumentException if client is null
     */
    public OperCommand(@Nonnull Client client) {
        super(client);
    }

    /**
     * Sets the password for the command.
     *
     * @param password password for the user
     * @return this command
     */
    @Nonnull
    public OperCommand password(@Nonnull String password) {
        this.password = Sanity.safeMessageCheck(password, "password");
        return this;
    }

    /**
     * Sets user for the command.
     *
     * @param user target nick
     * @return this command
     * @throws IllegalArgumentException for invalid target
     */
    @Nonnull
    public OperCommand user(@Nonnull String user) {
        this.user = Sanity.safeMessageCheck(user, "user");
        return this;
    }

    @Override
    public void execute() {
        if (this.password == null) {
            throw new IllegalStateException("Password not defined");
        }
        if (this.user == null) {
            throw new IllegalStateException("User not defined");
        }
        this.getClient().sendRawLine("OPER " + this.user + ' ' + this.password);
    }

    @Override
    public String toString() {
        return new ToStringer(this).add("user", this.user).add("password", this.password).toString();
    }
}
