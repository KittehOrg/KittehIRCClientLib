/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.event.capabilities;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ServerMessageEventBase;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fired when a CAP LIST is received, listing the current capabilities
 *
 * @see CapabilityManager
 * @see CapabilityRequestCommand
 */
public class CapabilitiesListEvent extends ServerMessageEventBase {
    private final List<CapabilityState> capabilities;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param capabilities capabilities listed
     */
    public CapabilitiesListEvent(@NonNull Client client, @NonNull List<ServerMessage> originalMessages, @NonNull List<CapabilityState> capabilities) {
        super(client, originalMessages);
        Sanity.nullCheck(capabilities, "Capabilities list cannot be null");
        this.capabilities = Collections.unmodifiableList(new ArrayList<>(capabilities));
    }

    /**
     * Gets the currently enabled capabilities.
     *
     * @return list of capabilities
     */
    @NonNull
    public List<CapabilityState> getCapabilities() {
        return this.capabilities;
    }

    @Override
    @NonNull
    protected ToStringer toStringer() {
        return super.toStringer().add("capabilities", this.capabilities);
    }
}
