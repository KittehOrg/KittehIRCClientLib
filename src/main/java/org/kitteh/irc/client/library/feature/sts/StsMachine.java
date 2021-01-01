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

/**
 * Interface representing the STS FSM.
 * <p>
 * This is a finite state machine which represents
 * the various states related to strict-transport
 * security that the client can be in at any one time.
 * <p>
 * State changes are caused by the IRCd sending the
 * appropriate capability data, or if a user manually
 * initiates a change. Actions are automatically taken
 * as a result of state transitions,
 */
public interface StsMachine {
    /**
     * Returns the current state in the enum that the
     * state machine is operating in.
     *
     * @return one of STSClientState's members
     */
    @NonNull StsClientState getCurrentState();

    /**
     * Changes the state of the FSM, triggering any state-specific work.
     *
     * @param newState a valid (non-UNKNOWN, non-null) state
     */
    void setCurrentState(@NonNull StsClientState newState);

    /**
     * Gets the persistence/storage manager.
     *
     * @return the storage manager instance
     */
    @NonNull StsStorageManager getStorageManager();

    /**
     * Provides a key-&gt;value map of options and list of flags, making up the STS policy.
     * <p>
     * It is expected the policy is valid at this stage.
     *
     * @param policy the valid STS policy
     */
    void setStsPolicy(@NonNull StsPolicy policy);
}
