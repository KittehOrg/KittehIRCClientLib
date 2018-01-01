/*
 * * Copyright (C) 2013-2018 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.event.abstractbase;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesListEvent;
import org.kitteh.irc.client.library.event.helper.CapabilityNegotiationResponseEvent;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Abstract base class for events involving capability negotiation (CAP) that
 * can have a response.
 *
 * @see CapabilityNegotiationResponseEvent
 * @see CapabilitiesListEvent
 */
public abstract class CapabilityNegotiationResponseEventBase extends ServerMessageEventBase implements CapabilityNegotiationResponseEvent {
    private boolean endNegotiation = true;
    private final boolean negotiating;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param negotiating if we are negotiating right now
     */
    protected CapabilityNegotiationResponseEventBase(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, boolean negotiating) {
        super(client, originalMessages);
        this.negotiating = negotiating;
    }

    @Override
    public final boolean isEndingNegotiation() {
        return this.endNegotiation;
    }

    @Override
    public final boolean isNegotiating() {
        return this.negotiating;
    }

    @Override
    public final void setEndingNegotiation(boolean endNegotiation) {
        this.endNegotiation = endNegotiation;
    }

    @Override
    @Nonnull
    protected ToStringer toStringer() {
        return super.toStringer().add("isEndingNegotiation", this.endNegotiation).add("isNegotiating", this.isNegotiating());
    }
}
