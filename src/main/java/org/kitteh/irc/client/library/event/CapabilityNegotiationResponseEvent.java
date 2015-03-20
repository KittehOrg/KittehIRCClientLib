/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.event;

import org.kitteh.irc.client.library.Client;

/**
 * CAP command handling.
 */
public abstract class CapabilityNegotiationResponseEvent extends ClientEvent {
    private boolean endNegotiation = true;
    private final boolean negotiating;

    protected CapabilityNegotiationResponseEvent(Client client, boolean negotiating) {
        super(client);
        this.negotiating = negotiating;
    }

    /**
     * Gets if negotiation should end after this event fires, which can be
     * changed via this event.
     *
     * @return true if negotiation will end after this event
     * @see #setEndingNegotiation(boolean)
     */
    public boolean isEndingNegotiation() {
        return this.endNegotiation;
    }

    /**
     * Gets if this event is fired during capability negotiation.
     *
     * @return true if negotiating capabilities
     */
    public boolean isNegotiating() {
        return this.negotiating;
    }

    /**
     * Sets if negotiation should end after this event fires. Note that if
     * negotiation is not set to end, and {@link #isNegotiating()} is true,
     * the connection will not complete.
     *
     * @param endNegotiation true if negotiation should end, false if not
     */
    public void setEndingNegotiation(boolean endNegotiation) {
        this.endNegotiation = endNegotiation;
    }
}