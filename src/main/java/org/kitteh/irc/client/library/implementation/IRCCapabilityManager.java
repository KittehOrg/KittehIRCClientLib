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
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class IRCCapabilityManager implements CapabilityManager {
    static class IRCCapabilityState implements CapabilityState {
        private final Client client;
        private final long creationTime;
        private final boolean disable;
        private final String name;
        private final Optional<String> value;

        IRCCapabilityState(@Nonnull Client client, @Nonnull String capabilityListItem) {
            this.client = client;
            this.creationTime = System.currentTimeMillis();
            this.disable = capabilityListItem.charAt(0) == '-';
            String remaining = this.disable ? capabilityListItem.substring(1) : capabilityListItem;
            int index = remaining.indexOf('=');
            if (index > -1 && remaining.length() > (index + 1)) {
                this.name = remaining.substring(0, index);
                this.value = Optional.of(remaining.substring(index + 1));
            } else {
                this.name = index > -1 ? remaining.substring(0, index) : remaining;
                this.value = Optional.empty();
            }
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

        @Nonnull
        @Override
        public Client getClient() {
            return this.client;
        }

        @Override
        public long getCreationTime() {
            return this.creationTime;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Nonnull
        @Override
        public Optional<String> getValue() {
            return this.value;
        }

        @Override
        public int hashCode() {
            return (2 * this.name.hashCode()) + (this.disable ? 1 : 0);
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("name", this.name).add("disabled", this.disable).add("value", this.value).toString();
        }
    }

    private final InternalClient client;
    private final Map<String, CapabilityState> capabilities = new ConcurrentHashMap<>();
    private List<CapabilityState> supportedCapabilities = new ArrayList<>();
    private boolean negotiating = true;

    IRCCapabilityManager(InternalClient client) {
        this.client = client;
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

    boolean isNegotiating() {
        return this.negotiating;
    }

    void endNegotiation() {
        this.negotiating = false;
    }

    void updateCapabilities(@Nonnull List<CapabilityState> capabilityStates) {
        for (CapabilityState capabilityState : capabilityStates) {
            if (capabilityState.isDisabled()) {
                this.capabilities.remove(capabilityState.getName());
            } else {
                this.capabilities.put(capabilityState.getName(), capabilityState);
            }
        }
    }

    void setCapabilities(@Nonnull List<CapabilityState> capabilityStates) {
        this.capabilities.clear();
        this.updateCapabilities(capabilityStates);
    }

    void setSupportedCapabilities(@Nonnull List<CapabilityState> capabilityStates) {
        this.supportedCapabilities = new ArrayList<>(capabilityStates);
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("client", this.client).toString();
    }
}