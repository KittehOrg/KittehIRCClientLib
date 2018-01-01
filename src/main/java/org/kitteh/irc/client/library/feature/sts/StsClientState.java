/*
 * * Copyright (C) 2013-2018 Matt Baxter http://kitteh.org
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

/**
 * Enum to keep track of the STS implementation's state machine.
 */
public enum StsClientState {
    /**
     * A state is not yet available. Perhaps the CAP LS command has not yet been issued or
     * the IRCd does not support IRCv3.
     */
    UNKNOWN,

    /**
     * The CAP LS command gave us back a response, but `sts` wasn't present in the list.
     */
    NO_STS_PRESENT,

    /**
     * A cached policy was used to force a secure connection.
     */
    STS_POLICY_CACHED,

    /**
     * We connected over an insecure link and found a valid `sts` capability. Now we're
     * going to reconnect on the specified secure port.
     */
    STS_PRESENT_RECONNECTING,

    /**
     * We connected and found a valid STS policy, but we were already connected on a secure
     * port so we don't need to reconnect. We will update the stored policy, though.
     */
    STS_PRESENT_ALREADY_SECURE,

    /**
     * We connected over plaintext and found a valid STS policy. We reconnected successfully
     * using TLS and verified the policy still exists.
     */
    STS_PRESENT_NOW_SECURE,

    /**
     * We connected via an insecure communication means and found an STS policy. We then
     * tried to establish a secure connection on the advertised port, but couldn't. This
     * could be due to a network issue or a problem validating the provided certificate.
     *
     * The client will stay in this state and it is not possible to bypass.
     */
    STS_PRESENT_CANNOT_CONNECT,

    /**
     * An STS policy was present when we connected with the insecure port, but it wasn't there
     * when we connected on the advertised secure port. This is indicative of an active MitM or
     * a server misconfiguration and should be investigated.
     */
    INVALID_STS_MISSING_ON_RECONNECT
}
