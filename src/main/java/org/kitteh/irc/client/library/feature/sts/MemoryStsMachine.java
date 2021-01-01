/*
 * * Copyright (C) 2013-2021 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.feature.sts;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.HostWithPort;
import org.kitteh.irc.client.library.util.Sanity;

/**
 * "Memory" prefix to distinguish implementation class
 * from interface, because StsMachine isn't IStsMachine.
 *
 * This class implements our FSM in an in-memory fashion,
 * using Java's data structures.
 */
public class MemoryStsMachine implements StsMachine {
    private final StsStorageManager manager;
    private final Client.WithManagement client;
    private StsClientState state = StsClientState.UNKNOWN;
    private StsPolicy policy;

    /**
     * Constructs the STS machine.
     *
     * @param manager STS manager
     * @param client client
     */
    public MemoryStsMachine(@NonNull StsStorageManager manager, Client.WithManagement client) {
        this.client = Sanity.nullCheck(client, "Cannot have a null client.");
        this.manager = Sanity.nullCheck(manager, "Cannot have a null STS persistence manager.");
    }

    @Override
    public @NonNull StsClientState getCurrentState() {
        return this.state;
    }

    @Override
    public void setCurrentState(@NonNull StsClientState newState) {
        this.state = Sanity.nullCheck(newState, "Need a valid state for the state machine.");
        this.step();
    }

    private void step() {
        switch (this.state) {
            case UNKNOWN:
                throw new IllegalStateException("Unknown state can only be used as an initial state!");
            case STS_POLICY_CACHED:
            case STS_PRESENT_RECONNECTING:
                this.client.isSecureConnection();
                HostWithPort oldAddress = this.client.getServerAddress();
                HostWithPort newAddress = HostWithPort.of(oldAddress.getHost(), Integer.parseInt(this.policy.getOptions().getOrDefault(StsPolicy.POLICY_OPTION_KEY_PORT, "6697")));

                this.client.setServerAddress(newAddress);
                break;
            case NO_STS_PRESENT:
            case STS_PRESENT_CANNOT_CONNECT:
                // stay in this state. An exception will have been thrown in Netty with useful info.
            default:
                // nothing to do
                break;
        }

        if (this.state == StsClientState.STS_PRESENT_RECONNECTING) {
            this.client.reconnect();
        }
    }

    @Override
    public @NonNull StsStorageManager getStorageManager() {
        return this.manager;
    }

    @Override
    public void setStsPolicy(@NonNull StsPolicy policy) {
        this.policy = Sanity.nullCheck(policy, "Policy");
    }
}
