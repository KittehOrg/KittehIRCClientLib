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
package org.kitteh.irc.client.library.event.abstractbase;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesListEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesNewSupportedEvent;
import org.kitteh.irc.client.library.event.helper.CapabilityNegotiationRequestEvent;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for specific capability events
 *
 * @see CapabilitiesNewSupportedEvent
 * @see CapabilitiesListEvent
 */
public abstract class CapabilityNegotiationResponseEventWithRequestBase extends CapabilityNegotiationResponseEventBase implements CapabilityNegotiationRequestEvent {
    private final List<String> requests = new ArrayList<>();

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param negotiating if we are negotiating right now
     */
    protected CapabilityNegotiationResponseEventWithRequestBase(@NonNull Client client, @NonNull List<ServerMessage> originalMessages, boolean negotiating) {
        super(client, originalMessages, negotiating);
    }

    @Override
    public void addRequest(@NonNull String capability) {
        this.requests.add(Sanity.safeMessageCheck(capability, "capability"));
    }

    @Override
    public @NonNull List<String> getRequests() {
        return Collections.unmodifiableList(new ArrayList<>(this.requests));
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("requests", this.requests);
    }
}
