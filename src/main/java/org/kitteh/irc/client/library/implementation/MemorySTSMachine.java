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
