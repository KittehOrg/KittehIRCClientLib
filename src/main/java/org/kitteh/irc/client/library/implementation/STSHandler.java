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
package org.kitteh.irc.client.library.implementation;

import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesNewSupportedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.event.client.ClientConnectionClosedEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.sts.STSClientState;
import org.kitteh.irc.client.library.feature.sts.STSMachine;
import org.kitteh.irc.client.library.feature.sts.STSPolicy;
import org.kitteh.irc.client.library.feature.sts.STSStorageManager;
import org.kitteh.irc.client.library.util.STSUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Class for handling the STS capability,
 * returned in the CAP LS 302 response.
 */
public class STSHandler {

    private final STSMachine machine;
    private final InternalClient client;
    private boolean isSecure;
    private final static Predicate<CapabilityState> STS_CAPABILITY_PREDICATE = c -> c.getName().equals("sts");

    /**
     * Creates the event handler for STS.
     *
     * @param machine The STS FSM.
     */
    public STSHandler(STSMachine machine, InternalClient client) {
        this.machine = machine;
        this.client = client;
        this.isSecure = client.getConfig().getNotNull(Config.SSL);
    }

    /**
     * Called when the server responds with its supported capabilities.
     * @param event The event instance.
     */
    @Handler
    public void onCapLs(CapabilitiesSupportedListEvent event) {
        // stability not a concern, only one or zero result(s)
        final Optional<CapabilityState> potentialStsCapability = event.getSupportedCapabilities().stream()
            .filter(STSHandler.STS_CAPABILITY_PREDICATE).findAny();

        if (!potentialStsCapability.isPresent()) {
            if (this.machine.getCurrentState() == STSClientState.STS_PRESENT_RECONNECTING) {
                this.machine.setCurrentState(STSClientState.INVALID_STS_MISSING_ON_RECONNECT);
            }
            return;
        }

        // okay, we have an STS capability!
        final CapabilityState sts = potentialStsCapability.get();
        final List<ServerMessage> originalMessages = event.getOriginalMessages();

        handleSTSCapability(sts, originalMessages);
    }

    @Handler
    public void onCapNew(CapabilitiesNewSupportedEvent event) {
        // stability not a concern, only one or zero result(s)
        final Optional<CapabilityState> potentialStsCapability = event.getNewCapabilities().stream()
                .filter(STS_CAPABILITY_PREDICATE).findAny();

        if (!potentialStsCapability.isPresent()) {
            // get out if we can't do anything useful here
            return;
        }

        // okay, we have an STS capability!
        final CapabilityState sts = potentialStsCapability.get();
        final List<ServerMessage> originalMessages = event.getOriginalMessages();
        if (this.machine.getCurrentState() == STSClientState.UNKNOWN) {
            this.handleSTSCapability(sts, originalMessages);
        }
    }

    @Handler
    public void onDisconnect(ClientConnectionClosedEvent event) {
        // The spec says we have to update the expiry of the policy if it still exists
        // at disconnection time...
        // Do this by removing and re-adding.
        String hostname = this.client.getConfig().getNotNull(Config.SERVER_ADDRESS).getHostName();
        final STSStorageManager storageManager = this.machine.getStorageManager();
        if (storageManager.hasEntry(hostname)) {
            final STSPolicy policy = storageManager.getEntry(hostname).get();
            long duration = Long.parseLong(policy.getOptions().get(STSPolicy.POLICY_OPTION_KEY_DURATION));
            storageManager.removeEntry(hostname);
            storageManager.addEntry(hostname, duration, policy);
        }
    }

    private void handleSTSCapability(CapabilityState sts, List<ServerMessage> originalMessages) {
        this.isSecure = client.getConfig().getNotNull(Config.SSL);
        InetSocketAddress address = client.getConfig().getNotNull(Config.SERVER_ADDRESS);
        final String msg = originalMessages.stream().map(ServerMessage::getMessage)
                .reduce((a, b) -> (a+b).replace('\n', ' ')).orElse("Missing!");
        if (!sts.getValue().isPresent()) {
            throw new KittehServerMessageException(msg, "No value provided for sts capability.");
        }

        final String capabilityValue = sts.getValue().get();
        final STSPolicy policy = STSUtil.getSTSPolicyFromString(",", capabilityValue);
        if (policy.getFlags().contains(STSPolicy.POLICY_OPTION_KEY_PORT) || policy.getFlags().contains(STSPolicy.POLICY_OPTION_KEY_DURATION)) {
            throw new KittehServerMessageException(msg, "Improper use of flag in required option context!");
        }

        if (!policy.getOptions().containsKey(STSPolicy.POLICY_OPTION_KEY_PORT)) {
            policy.getOptions().put(STSPolicy.POLICY_OPTION_KEY_PORT, Integer.toString(address.getPort())); // spec says port is optional
        }

        for (String key : policy.getOptions().keySet()) {
            // Unknown keys are ignored by the switches below
            this.machine.setSTSPolicy(policy);
            if (this.isSecure) {
                this.handleSecureKey(key, policy);
            } else {
                this.handleInsecureKey(key, policy);
            }
        }
    }

    private void handleInsecureKey(String key, STSPolicy policy) {
        final String value = policy.getOptions().get(key);

        switch (key) {
            case STSPolicy.POLICY_OPTION_KEY_DURATION:
                // Do NOT persist, because this policy could've been inserted by an active MitM
                break;
            case STSPolicy.POLICY_OPTION_KEY_PORT:
                try {
                    Integer.parseInt(value); // can't easily use a short because signed..
                } catch (NumberFormatException nfe) {
                    throw new KittehServerMessageException(value, "Specified port could not be parsed: "  + nfe.getMessage());
                }

                this.machine.setCurrentState(STSClientState.STS_PRESENT_RECONNECTING);
                break;
        }
    }

    private void handleSecureKey(String key, STSPolicy policy) {
        final String value = policy.getOptions().get(key);

        switch (key) {
            case STSPolicy.POLICY_OPTION_KEY_DURATION:
                // We can safely persist this.

                long duration;
                try {
                    // This MUST be specified in seconds as the value of this key
                    // and as a single integer without a prefix or suffix.
                    duration = Long.parseLong(value);
                } catch (NumberFormatException nfe) {
                    throw new KittehServerMessageException(value, "Invalid duration provided: " + nfe.getMessage());
                }

                final STSStorageManager storageMan = this.machine.getStorageManager();
                String hostname = this.client.getConfig().getNotNull(Config.SERVER_ADDRESS).getHostName();

                // A duration of 0 means the policy expires immediately.
                // This method can be used by servers to remove a previously set policy.
                if (duration == 0) {
                    storageMan.removeEntry(hostname);
                    return;
                }

                if (storageMan.hasEntry(hostname)) {
                    // Clients MUST reset the duration "time to live" every time they receive a valid policy
                    // We achieve this by just removing old policies and re-adding new ones.
                    storageMan.removeEntry(hostname);
                }

                storageMan.addEntry(hostname, duration, policy);
                this.machine.setCurrentState(STSClientState.STS_PRESENT_NOW_SECURE);
                break;
            case STSPolicy.POLICY_OPTION_KEY_PORT:
                // Ignored when already connected securely
        }
    }
}
