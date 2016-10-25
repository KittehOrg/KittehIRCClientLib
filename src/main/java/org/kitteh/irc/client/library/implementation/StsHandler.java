package org.kitteh.irc.client.library.implementation;

import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
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
        if (!sts.getValue().isPresent()) {
            String msg = event.getOriginalMessages().stream().map(ServerMessage::getMessage).reduce(
                    (a, b) -> (a+b).replace('\n', ' ')
            ).orElse("Missing!");
            throw new KittehServerMessageException(msg, "No value provided for sts capability.");
        }

        final String capabilityValue = sts.getValue().get();


    }
}
