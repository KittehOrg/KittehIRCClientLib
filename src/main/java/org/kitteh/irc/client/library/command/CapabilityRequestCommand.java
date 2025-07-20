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
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends a capability request to the server.
 */
public class CapabilityRequestCommand extends Command<CapabilityRequestCommand> {
    private final List<String> requests = new ArrayList<>();

    /**
     * Constructs the CAP REQ command.
     *
     * @param client the client
     * @throws IllegalArgumentException if client is null
     */
    public CapabilityRequestCommand(@NonNull Client client) {
        super(client);
    }

    /**
     * Adds a capability identifier to request enabling.
     *
     * @param capability capability requested
     * @return this command
     * @throws IllegalArgumentException if capability is null
     * @see CapabilityManager#getCapabilities()
     * @see CapabilityManager#getSupportedCapabilities()
     */
    public synchronized @NonNull CapabilityRequestCommand enable(@NonNull String capability) {
        this.requests.add(Sanity.safeMessageCheck(capability, "Capability"));
        return this;
    }

    /**
     * Adds a capability identifier to request disabling.
     *
     * @param capability capability requested
     * @return this command
     * @throws IllegalArgumentException if capability is null
     * @see CapabilityManager#getCapabilities()
     */
    public synchronized @NonNull CapabilityRequestCommand disable(@NonNull String capability) {
        this.requests.add('-' + Sanity.safeMessageCheck(capability, "Capability"));
        return this;
    }

    @Override
    public synchronized void execute() {
        StringBuilder builder = new StringBuilder(200);
        for (String request : this.requests) {
            if ((builder.length() > 0) && ((request.length() + builder.length()) > 200)) {
                this.send(builder.toString());
                builder.setLength(0);
            }
            builder.append(request).append(' ');
        }
        this.send(builder.toString());
    }

    private void send(@NonNull String requests) {
        this.sendCommandLine("CAP REQ :" + requests, true);
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("requests", this.requests);
    }
}
