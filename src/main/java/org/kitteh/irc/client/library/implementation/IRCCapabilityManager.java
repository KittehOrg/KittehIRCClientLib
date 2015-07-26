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
package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.CapabilityManager;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class IRCCapabilityManager implements CapabilityManager {
    static class IRCCapabilityState implements CapabilityState {
        private final long creationTime;
        private final boolean disable;
        private final String name;

        IRCCapabilityState(@Nonnull String capabilityListItem) {
            this.creationTime = System.currentTimeMillis();
            this.disable = capabilityListItem.charAt(0) == '-';
            this.name = this.disable ? capabilityListItem.substring(1) : capabilityListItem;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof IRCCapabilityState) {
                IRCCapabilityState state = (IRCCapabilityState) o;
                return state.name.equals(this.name) && (state.disable == this.disable);
            }
            return false;
        }

        @Override
        public boolean isDisabled() {
            return this.disable;
        }

        @Override
        @Nonnull
        public String getCapabilityName() {
            return this.name;
        }

        @Override
        public long getCreationTime() {
            return this.creationTime;
        }

        @Override
        public int hashCode() {
            return (2 * this.name.hashCode()) + (this.disable ? 1 : 0);
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).toString();
        }
    }

    private final InternalClient client;
    private final List<String> capabilities = new ArrayList<>();
    private List<String> supportedCapabilities = new ArrayList<>();
    private boolean negotiating = true;

    IRCCapabilityManager(InternalClient client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public List<String> getCapabilities() {
        return new ArrayList<>(this.capabilities);
    }

    @Nonnull
    @Override
    public List<String> getSupportedCapabilities() {
        return new ArrayList<>(this.supportedCapabilities);
    }

    boolean isNegotiating() {
        return this.negotiating;
    }

    void endNegotiation() {
        this.negotiating = false;
    }

    void updateCapabilities(@Nonnull List<CapabilityState> capabilityStates) {
        for (CapabilityState capabilityState : capabilityStates) {
            if (capabilityState.isDisabled()) {
                this.capabilities.remove(capabilityState.getCapabilityName());
            } else {
                this.capabilities.add(capabilityState.getCapabilityName());
            }
        }
    }

    void setCapabilities(@Nonnull List<CapabilityState> capabilityStates) {
        this.capabilities.clear();
        this.updateCapabilities(capabilityStates);
    }

    void setSupportedCapabilities(@Nonnull List<CapabilityState> capabilityStates) {
        this.supportedCapabilities = capabilityStates.stream().map(CapabilityState::getCapabilityName).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("client", this.client).toString();
    }
}