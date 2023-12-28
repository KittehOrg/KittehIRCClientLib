/*
 * * Copyright (C) 2013-2023 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.event.abstractbase.CapabilityNegotiationResponseEventBase;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.Collections;
import java.util.List;

/**
 * Fired when a CAP NAK is received.
 *
 * @see CapabilityManager
 * @see CapabilityRequestCommand
 */
public class CapabilitiesRejectedEvent extends CapabilityNegotiationResponseEventBase {
    private final List<CapabilityState> rejectedCapabilitiesRequest;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param sourceMessage source message
     * @param negotiating if we are negotiating right now
     * @param rejectedCapabilitiesRequest capabilities rejected
     */
    public CapabilitiesRejectedEvent(@NonNull Client client, @NonNull ServerMessage sourceMessage, boolean negotiating, @NonNull List<CapabilityState> rejectedCapabilitiesRequest) {
        super(client, sourceMessage, negotiating);
        Sanity.nullCheck(rejectedCapabilitiesRequest, "Capabilities list");
        this.rejectedCapabilitiesRequest = Collections.unmodifiableList(rejectedCapabilitiesRequest);
    }

    /**
     * Gets the rejected change, or at least the first 100 characters worth.
     *
     * @return rejected request list
     */
    public @NonNull List<CapabilityState> getRejectedCapabilitiesRequest() {
        return this.rejectedCapabilitiesRequest;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("rejectedCapabilitiesRequest", this.rejectedCapabilitiesRequest);
    }
}
