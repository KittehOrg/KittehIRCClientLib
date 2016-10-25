package org.kitteh.irc.client.library.implementation;

import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.sts.StsClientState;
import org.kitteh.irc.client.library.feature.sts.StsMachine;
import org.kitteh.irc.client.library.feature.sts.StsStorageManager;
import org.kitteh.irc.client.library.util.StringUtil;

import java.util.Map;
import java.util.Optional;

/**
 * Class for handling the STS capability,
 * returned in the CAP LS 302 response.
 */
public class StsHandler {

    private final StsMachine machine;
    private final boolean isSecure;

    /**
     * Creates the event handler for STS.
     *
     * @param machine The STS FSM.
     */
    public StsHandler(StsMachine machine, boolean isSecure) {
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
        final Optional<CapabilityState> potentialStsCapability = event.getSupportedCapabilities().stream().filter(
            c -> c.getName().equals("sts") // inb4 complaints about this indentation
        ).findAny();

        if (!potentialStsCapability.isPresent()) {
            // get out if we can't do anything useful here
            return;
        }

        // okay, we have an STS capability!
        final CapabilityState sts = potentialStsCapability.get();
        if (!sts.getValue().isPresent()) {
            String msg = event.getOriginalMessages().stream().map(ServerMessage::getMessage).reduce(
                    (a, b) -> (a+b).replace('\n', ' ')
            ).orElse("Missing!");
            throw new KittehServerMessageException(msg, "No value provided for sts capability.");
        }

        final String capabilityValue = sts.getValue().get();
        Map<String, Optional<String>> options = StringUtil.parseSeparatedKeyValueString(",", capabilityValue);
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
                this.machine.setState(StsClientState.STS_PRESENT_RECONNECTING);
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

                final StsStorageManager storageMan = this.machine.getStorageManager();
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
                storageMan.addEntry("google.com", duration);
                break;
            case "port":
                // Ignored when already connected securely
        }
    }
}
