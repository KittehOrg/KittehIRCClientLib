/*
 * * Copyright (C) 2013-2025 Matt Baxter https://kitteh.org
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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

/**
 * Sends a SETNAME request to the server.
 */
public class SetNameCommand extends Command<SetNameCommand> {
    private String newName;

    /**
     * Constructs the command.
     *
     * @param client the client
     * @throws IllegalArgumentException if client is null
     */
    public SetNameCommand(@NonNull Client client) {
        super(client);
    }

    /**
     * Sets the new name.
     *
     * @param newName new name
     * @return this command
     * @throws IllegalArgumentException for invalid message
     */
    public @NonNull SetNameCommand newName(@Nullable String newName) {
        this.newName = (newName == null) ? null : Sanity.safeMessageCheck(newName);
        return this;
    }

    @Override
    public void execute() {
        if (this.newName == null) {
            throw new IllegalStateException("New name not specified");
        }
        if (this.getClient().getCapabilityManager().getCapability(CapabilityManager.Defaults.SETNAME).isEmpty()) {
            throw new IllegalStateException("Cannot send SETNAME when the capability is not negotiated");
        }
        this.sendCommandLine("SETNAME :" + this.newName);
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("newName", this.newName);
    }
}
