/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link CapabilityManager}.
 */
public class ManagerCapability implements CapabilityManager.WithManagement {
    private final Client client;
    private final Map<String, CapabilityState> capabilities = new ConcurrentHashMap<>();
    private List<CapabilityState> supportedCapabilities = new ArrayList<>();
    private boolean negotiating = true;

    /**
     * Constructs the capability manager.
     *
     * @param client client for which this manager will operate
     */
    public ManagerCapability(Client client) {
        this.client = client;
    }

    @Override
    public void reset() {
        this.capabilities.clear();
        this.negotiating = true;
    }

    @Nonnull
    @Override
    public List<CapabilityState> getCapabilities() {
        return new ArrayList<>(this.capabilities.values());
    }

    @Nonnull
    @Override
    public List<CapabilityState> getSupportedCapabilities() {
        return new ArrayList<>(this.supportedCapabilities);
    }

    @Override
    public boolean isNegotiating() {
        return this.negotiating;
    }

    @Override
    public void endNegotiation() {
        this.negotiating = false;
    }

    @Override
    public void updateCapabilities(@Nonnull List<CapabilityState> capabilityStates) {
        for (CapabilityState capabilityState : capabilityStates) {
            if (capabilityState.isDisabled()) {
                this.capabilities.remove(capabilityState.getName());
            } else {
                this.capabilities.put(capabilityState.getName(), capabilityState);
            }
        }
    }

    @Override
    public void setCapabilities(@Nonnull List<CapabilityState> capabilityStates) {
        this.capabilities.clear();
        this.updateCapabilities(capabilityStates);
    }

    @Override
    public void setSupportedCapabilities(@Nonnull List<CapabilityState> capabilityStates) {
        this.supportedCapabilities = new ArrayList<>(capabilityStates);
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this)
                .add("client", this.client)
                .add("capabilities", this.capabilities)
                .add("supportedCapabilities", this.supportedCapabilities)
                .toString();
    }
}
