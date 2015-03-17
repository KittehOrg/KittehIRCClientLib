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
package org.kitteh.irc.client.library;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * I'm the CAP man!
 */
class CapabilityManager {
    private List<String> capabilities = new ArrayList<>();
    private List<String> supportedCapabilities = new ArrayList<>();
    private final IRCClient client;
    private boolean negotiating = true;

    CapabilityManager(IRCClient client) {
        this.client = client;
    }

    List<String> getCapabilities() {
        return new ArrayList<>(this.capabilities);
    }

    List<String> getSupportedCapabilities() {
        return new ArrayList<>(this.supportedCapabilities);
    }

    boolean isNegotiating() {
        return this.negotiating;
    }

    void endNegotiation() {
        this.negotiating = false;
    }

    void updateCapabilities(List<CapabilityState> capabilityStates) {
        for (CapabilityState capabilityState : capabilityStates) {
            if (capabilityState.isDisabled()) {
                this.capabilities.remove(capabilityState.getCapabilityName());
            } else {
                this.capabilities.add(capabilityState.getCapabilityName());
            }
        }
    }

    void setSupportedCapabilities(List<CapabilityState> capabilityStates) {
        this.supportedCapabilities = capabilityStates.stream().map(CapabilityState::getCapabilityName).collect(Collectors.toList());
    }
}