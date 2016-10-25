package org.kitteh.irc.client.library.feature.sts;

/**
 * Interface representing the STS FSM.
 */
public interface StsMachine {
    StsClientState getCurrentState();
    void setState(StsClientState newState);
    StsStorageManager getStorageManager();
}
