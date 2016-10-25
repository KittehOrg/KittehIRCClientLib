package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.feature.sts.STSClientState;
import org.kitteh.irc.client.library.feature.sts.STSMachine;
import org.kitteh.irc.client.library.feature.sts.STSStorageManager;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;

/**
 * "Memory" prefix to distinguish implementation class
 * from interface, because STSMachine isn't IStsMachine.
 *
 * This class implements our FSM in an in-memory fashion,
 * using Java's data structures.
 */
public class MemorySTSMachine implements STSMachine {

    private final STSStorageManager manager;
    private STSClientState state = STSClientState.UNKNOWN;

    public MemorySTSMachine(@Nonnull STSStorageManager manager) {
        this.manager = Sanity.nullCheck(manager, "Cannot have a null STS persistence manager.");
    }

    @Nonnull
    @Override
    public STSClientState getCurrentState() {
        return this.state;
    }

    @Override
    public void setCurrentState(@Nonnull STSClientState newState) {
        this.state = Sanity.nullCheck(newState, "Need a valid state for the state machine.");
    }

    @Override
    public STSStorageManager getStorageManager() {
        return this.manager;
    }
}
