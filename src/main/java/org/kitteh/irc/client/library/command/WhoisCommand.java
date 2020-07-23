/*
 * * Copyright (C) 2013-2020 Matt Baxter https://kitteh.org
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

/**
 * Sends a WHOIS request to the server.
 */
public class WhoisCommand extends Command<WhoisCommand> {
    private @Nullable String server;
    private @Nullable String target;

    /**
     * Constructs the command.
     *
     * @param client the client
     * @throws IllegalArgumentException if client is null
     */
    public WhoisCommand(@NonNull Client client) {
        super(client);
    }

    /**
     * Sets the server to query.
     * <p>
     * It's commonly possible to specify the user's nick here instead of a server,
     * which will cause the IRC daemon to automatically send it to the user's
     * current server.
     *
     * @param server server to query or null to not specify one
     * @return this command
     */
    public @NonNull WhoisCommand server(@Nullable String server) {
        this.server = (server == null) ? null : Sanity.safeMessageCheck(server, "server");
        return this;
    }

    /**
     * Sets the target nickname for the command.
     *
     * @param target target nick
     * @return this command
     * @throws IllegalArgumentException for invalid target
     */
    public @NonNull WhoisCommand target(@NonNull String target) {
        this.target = Sanity.safeMessageCheck(target, "target");
        return this;
    }

    @Override
    public void execute() {
        if (this.target == null) {
            throw new IllegalStateException("Target not defined");
        }
        StringBuilder builder = new StringBuilder(5 + this.target.length() + ((this.server == null) ? 1 : (2 + this.server.length())));
        builder.append("WHOIS ");
        if (this.server != null) {
            builder.append(this.server).append(' ');
        }
        builder.append(this.target);
        this.sendCommandLine(builder.toString());
    }

    @Override
    public String toString() {
        return new ToStringer(this).add("client", this.getClient()).add("server", this.server).add("target", this.target).toString();
    }
}
