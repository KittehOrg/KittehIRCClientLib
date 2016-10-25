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
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.sts.STSMachine;
import org.kitteh.irc.client.library.feature.sts.STSClientState;
import org.kitteh.irc.client.library.feature.sts.STSStorageManager;
import org.kitteh.irc.client.library.util.StringUtil;

import java.util.Map;
import java.util.Optional;

/**
 * Class for handling the STS capability,
 * returned in the CAP LS 302 response.
 */
public class STSHandler {

    private final STSMachine machine;
    private final boolean isSecure;

    /**
     * Creates the event handler for STS.
     *
     * @param machine The STS FSM.
     */
    public STSHandler(STSMachine machine, boolean isSecure) {
        this.machine = machine;
        this.isSecure = isSecure;
    }

    /**
     * Called when the server responds with its supported capabilities.
     * @param event The event instance.
     */
    @Handler
    public void onCapLs(CapabilitiesSupportedListEvent event) {
        // stability not a concern, only one or zero result(s)
        final Optional<CapabilityState> potentialStsCapability = event.getSupportedCapabilities().stream()
            .filter(c -> c.getName().equals("sts")).findAny();

        if (!potentialStsCapability.isPresent()) {
            // get out if we can't do anything useful here
            return;
        }

        // okay, we have an STS capability!
        final CapabilityState sts = potentialStsCapability.get();
        if (!sts.getValue().isPresent()) {
            final String msg = event.getOriginalMessages().stream().map(ServerMessage::getMessage)
                .reduce((a, b) -> (a+b).replace('\n', ' ')).orElse("Missing!");
            throw new KittehServerMessageException(msg, "No value provided for sts capability.");
        }

        final String capabilityValue = sts.getValue().get();
        final Map<String, Optional<String>> options = StringUtil.parseSeparatedKeyValueString(",", capabilityValue);
        for (String key : options.keySet()) {
            // Unknown keys are ignored by the switches below
            if (this.isSecure) {
                this.handleSecureKey(key, options.get(key));
            } else {
                this.handleInsecureKey(key, options.get(key));
            }
        }

    }

    private void handleInsecureKey(String key, Optional<String> s) {
        switch (key) {
            case "duration":
                // Do NOT persist, because this policy could've been inserted by an active MitM
                break;
            case "port":
                // Brilliant, we have a secure port. We'll reconnect with SSL and verify the policy.
                this.machine.setCurrentState(STSClientState.STS_PRESENT_RECONNECTING);
                // TODO: Reconnect.
                break;
        }
    }

    private void handleSecureKey(String key, Optional<String> s) {
        switch (key) {
            case "duration":
                // We can safely persist this.

                // MUST be specified in seconds as the value of this key
                if (!s.isPresent()) {
                    throw new KittehServerMessageException(key, "Missing value for this STS option.");
                }

                long duration;
                try {
                    // This MUST be specified in seconds as the value of this key
                    // and as a single integer without a prefix or suffix.
                    duration = Long.parseLong(s.get());
                } catch (NumberFormatException nfe) {
                    throw new KittehServerMessageException(s.get(), "Invalid duration provided");
                }

                final STSStorageManager storageMan = this.machine.getStorageManager();
                if (storageMan.hasEntry("google.com")) {
                    // Clients MUST reset the duration "time to live" every time they receive a valid policy
                    storageMan.removeEntry("google.com");
                }

                // A duration of 0 means the policy expires immediately.
                // This method can be used by servers to remove a previously set policy.
                if (duration == 0) {
                    return;
                }

                // persist him, TODO: with the current port
                storageMan.addEntry("google.com", duration, null); // TODO: get access to it from here..
                break;
            case "port":
                // Ignored when already connected securely
        }
    }
}
