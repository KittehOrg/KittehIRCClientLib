package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.feature.sts.StsClientState;
import org.kitteh.irc.client.library.feature.sts.StsMachine;
import org.kitteh.irc.client.library.feature.sts.StsStorageManager;

/**
 * "Memory" prefix to distinguish implementation class
 * from interface, because StsMachine isn't IStsMachine.
 *
 * This class implements our FSM in an in-memory fashion,
 * using Java's data structures.
 */
public class MemoryStsMachine implements StsMachine {

    private final StsStorageManager storeMan;
    private StsClientState state = StsClientState.UNKNOWN;

    public MemoryStsMachine(StsStorageManager storeMan) {
        this.storeMan = storeMan;
    }

    @Override
    public StsClientState getCurrentState() {
        return this.state;
    }

    @Override
    public void setState(StsClientState newState) {
        this.state = newState;
    }

    @Override
    public StsStorageManager getStorageManager() {
        return this.storeMan;
    }
}
