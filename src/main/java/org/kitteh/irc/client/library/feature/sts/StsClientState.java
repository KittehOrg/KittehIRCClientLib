package org.kitteh.irc.client.library.feature.sts;

/**
 * Enum to keep track of the STS implementation's state machine.
 */
public enum StsClientState {
    UNKNOWN,
    NO_STS_PRESENT,
    STS_PRESENT_RECONNECTING,
    STS_PRESENT_ALREADY_SECURE,
    INVALID_STS_MISSING_ON_RECONNECT
}
