package org.kitteh.irc.client.library.implementation;

import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.feature.sts.StsMachine;

import java.util.Optional;

/**
 * Class for handling the STS capability,
 * returned in the CAP LS 302 response.
 */
public class StsHandler {

    private final StsMachine machine;

    /**
     * Creates the event handler for STS.
     *
     * @param machine The STS FSM.
     */
    public StsHandler(StsMachine machine) {
        this.machine = machine;
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
        // TODO: CAP API work I suspect..
    }
}
