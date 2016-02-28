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
package org.kitteh.irc.client.library.event.capabilities;

import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.CapabilityNegotiationResponseEventBase;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Fired when a CAP ACK command is received.
 *
 * @see CapabilityManager
 * @see CapabilityRequestCommand
 */
public class CapabilitiesAcknowledgedEvent extends CapabilityNegotiationResponseEventBase {
    private final List<CapabilityState> acknowledgedCapabilities;

    /**
     * Constructs this event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param negotiating if we are negotiating right now
     * @param acknowledgedCapabilities capabilities acknowledged
     */
    public CapabilitiesAcknowledgedEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, boolean negotiating, @Nonnull List<CapabilityState> acknowledgedCapabilities) {
        super(client, originalMessages, negotiating);
        Sanity.nullCheck(acknowledgedCapabilities, "Capabilities list cannot be null");
        this.acknowledgedCapabilities = Collections.unmodifiableList(acknowledgedCapabilities);
    }

    /**
     * Gets capabilities acknowledged.
     *
     * @return ACK'd capabilities
     */
    @Nonnull
    public List<CapabilityState> getAcknowledgedCapabilities() {
        return this.acknowledgedCapabilities;
    }
}
