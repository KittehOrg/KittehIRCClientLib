/*
 * * Copyright (C) 2013-2025 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.defaults.listener;

import net.engio.mbassy.listener.Handler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.defaults.element.DefaultCapabilityState;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesAcknowledgedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesDeletedSupportedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesListEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesNewSupportedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesRejectedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.helper.CapabilityNegotiationRequestEvent;
import org.kitteh.irc.client.library.event.helper.CapabilityNegotiationResponseEvent;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default CAP listener, producing events using default classes.
 */
public class DefaultCapListener extends AbstractDefaultListenerBase {
    private static final int CAPABILITY_LIST_INDEX_DEFAULT = 2;

    private final List<CapabilityState> capList = new ArrayList<>();
    private final List<ServerMessage> capListMessages = new ArrayList<>();
    private final List<CapabilityState> capLs = new ArrayList<>();
    private final List<ServerMessage> capLsMessages = new ArrayList<>();

    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultCapListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @CommandFilter("CAP")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void cap(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "CAP message too short");
            return;
        }
        CapabilityNegotiationResponseEvent responseEvent = null;
        int capabilityListIndex;
        if ("*".equals(event.getParameters().get(DefaultCapListener.CAPABILITY_LIST_INDEX_DEFAULT))) {
            if (event.getParameters().size() < 4) {
                this.trackException(event, "CAP message too short");
                return;
            }
            capabilityListIndex = DefaultCapListener.CAPABILITY_LIST_INDEX_DEFAULT + 1;
        } else {
            capabilityListIndex = DefaultCapListener.CAPABILITY_LIST_INDEX_DEFAULT;
        }
        List<CapabilityState> capabilityStateList = Arrays.stream(event.getParameters().get(capabilityListIndex).split(" ")).filter(string -> !string.isEmpty()).map(capability -> new DefaultCapabilityState(this.getClient(), capability)).collect(Collectors.toCollection(ArrayList::new));
        switch (event.getParameters().get(1).toLowerCase()) {
            case "ack":
                this.getClient().getCapabilityManager().updateCapabilities(capabilityStateList);
                responseEvent = new CapabilitiesAcknowledgedEvent(this.getClient(), event.getSource(), this.getClient().getCapabilityManager().isNegotiating(), capabilityStateList);
                this.fire(responseEvent);
                break;
            case "list":
                this.capListMessages.add(event.getServerMessage());
                if (capabilityListIndex != DefaultCapListener.CAPABILITY_LIST_INDEX_DEFAULT) {
                    this.capList.addAll(capabilityStateList);
                } else {
                    List<CapabilityState> states;
                    if (this.capList.isEmpty()) {
                        states = capabilityStateList;
                    } else {
                        states = this.capList;
                        states.addAll(capabilityStateList);
                    }
                    this.getClient().getCapabilityManager().setCapabilities(states);
                    this.fire(new CapabilitiesListEvent(this.getClient(), this.capListMessages, states));
                    states.clear();
                }
                break;
            case "ls":
                this.capLsMessages.add(event.getServerMessage());
                if (capabilityListIndex != DefaultCapListener.CAPABILITY_LIST_INDEX_DEFAULT) {
                    this.capLs.addAll(capabilityStateList);
                } else {
                    List<CapabilityState> states;
                    if (this.capLs.isEmpty()) {
                        states = capabilityStateList;
                    } else {
                        states = this.capLs;
                        states.addAll(capabilityStateList);
                    }
                    this.getClient().getCapabilityManager().setSupportedCapabilities(states);
                    responseEvent = new CapabilitiesSupportedListEvent(this.getClient(), this.capLsMessages, this.getClient().getCapabilityManager().isNegotiating(), states);
                    this.fireAndCapReq((CapabilitiesSupportedListEvent) responseEvent);
                    this.capLs.clear();
                }
                break;
            case "nak":
                this.getClient().getCapabilityManager().updateCapabilities(capabilityStateList);
                responseEvent = new CapabilitiesRejectedEvent(this.getClient(), event.getSource(), this.getClient().getCapabilityManager().isNegotiating(), capabilityStateList);
                this.fire(responseEvent);
                break;
            case "new":
                List<CapabilityState> statesAdded = new ArrayList<>(this.getClient().getCapabilityManager().getSupportedCapabilities());
                statesAdded.addAll(capabilityStateList);
                this.getClient().getCapabilityManager().setSupportedCapabilities(statesAdded);
                responseEvent = new CapabilitiesNewSupportedEvent(this.getClient(), event.getSource(), this.getClient().getCapabilityManager().isNegotiating(), capabilityStateList);
                this.fireAndCapReq((CapabilitiesNewSupportedEvent) responseEvent);
                break;
            case "del":
                List<CapabilityState> statesRemaining = new ArrayList<>(this.getClient().getCapabilityManager().getSupportedCapabilities());
                statesRemaining.removeAll(capabilityStateList);
                this.getClient().getCapabilityManager().setSupportedCapabilities(statesRemaining);
                responseEvent = new CapabilitiesDeletedSupportedEvent(this.getClient(), event.getSource(), this.getClient().getCapabilityManager().isNegotiating(), capabilityStateList);
                this.fire(responseEvent);
                break;
        }
        if (responseEvent != null) {
            if (responseEvent.isNegotiating() && responseEvent.isEndingNegotiation()) {
                this.getClient().sendRawLineImmediately("CAP END");
                this.getClient().getCapabilityManager().endNegotiation();
            }
        }
    }

    private void fireAndCapReq(@NonNull CapabilityNegotiationRequestEvent responseEvent) {
        Set<String> capabilities = this.getClient().getCapabilityManager().getSupportedCapabilities().stream().map(CapabilityState::getName).collect(Collectors.toCollection(HashSet::new));
        capabilities.retainAll(CapabilityManager.Defaults.getDefaults());
        List<String> currentCapabilities = this.getClient().getCapabilityManager().getCapabilities().stream().map(CapabilityState::getName).collect(Collectors.toList());
        capabilities.removeAll(currentCapabilities);
        if (!capabilities.isEmpty()) {
            responseEvent.setEndingNegotiation(false);
            capabilities.forEach(responseEvent::addRequest);
        }
        this.fire(responseEvent);
        List<String> requests = responseEvent.getRequests();
        if (!requests.isEmpty()) {
            CapabilityRequestCommand capabilityRequestCommand = new CapabilityRequestCommand(this.getClient());
            requests.stream().distinct().filter(c -> !currentCapabilities.contains(c)).forEach(capabilityRequestCommand::enable);
            capabilityRequestCommand.execute();
        }
    }
}
