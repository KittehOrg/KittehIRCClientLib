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
package org.kitteh.irc.client.library.event.capabilities;

import org.kitteh.irc.client.library.CapabilityState;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.abstractbase.ClientEventBase;

import java.util.Collections;
import java.util.List;

/**
 * Fired when a CAP LIST is received, listing the current capabilities
 */
public class CapabilitiesListEvent extends ClientEventBase {
    private final List<CapabilityState> capabilities;

    public CapabilitiesListEvent(Client client, List<CapabilityState> capabilities) {
        super(client);
        this.capabilities = Collections.unmodifiableList(capabilities);
    }

    /**
     * Gets the currently enabled capabilities.
     *
     * @return list of capabilities
     */
    public List<CapabilityState> getCapabilities() {
        return this.capabilities;
    }
}