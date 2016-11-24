package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.feature.sts.STSClientState;
import org.kitteh.irc.client.library.feature.sts.STSMachine;
import org.kitteh.irc.client.library.feature.sts.STSPolicy;
import org.kitteh.irc.client.library.feature.sts.STSStorageManager;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;

/**
 * "Memory" prefix to distinguish implementation class
 * from interface, because STSMachine isn't IStsMachine.
 *
 * This class implements our FSM in an in-memory fashion,
 * using Java's data structures.
 */
class MemorySTSMachine implements STSMachine {
    private final STSStorageManager manager;
    private final InternalClient client;
    private STSClientState state = STSClientState.UNKNOWN;
    private STSPolicy policy;

    MemorySTSMachine(@Nonnull STSStorageManager manager, InternalClient client) {
        this.client = Sanity.nullCheck(client, "Cannot have a null client.");
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
        this.step();
    }

    private void step() {
        switch (this.state) {
            case UNKNOWN:
                throw new IllegalStateException("Unknown state can only be used as an initial state!");
            case STS_POLICY_CACHED:
            case STS_PRESENT_RECONNECTING:
                this.client.getConfig().set(Config.SSL, true);
                InetSocketAddress oldAddress = this.client.getConfig().get(Config.SERVER_ADDRESS);
                InetSocketAddress newAddress = new InetSocketAddress(oldAddress.getHostName(), Integer.parseInt(this.policy.getOptions().getOrDefault(STSPolicy.POLICY_OPTION_KEY_PORT, "6697")));

                this.client.getConfig().set(Config.SERVER_ADDRESS, newAddress);
                break;
            case NO_STS_PRESENT:
            case STS_PRESENT_CANNOT_CONNECT:
                // stay in this state. An exception will have been thrown in Netty with useful info.
            default:
                // nothing to do
                break;
        }

        if (this.state == STSClientState.STS_PRESENT_RECONNECTING) {
            this.client.reconnect();
        }
    }

    @Nonnull
    @Override
    public STSStorageManager getStorageManager() {
        return this.manager;
    }

    @Override
    public void setSTSPolicy(@Nonnull STSPolicy policy) {
        this.policy = Sanity.nullCheck(policy, "Policy cannot be null");
    }
}
