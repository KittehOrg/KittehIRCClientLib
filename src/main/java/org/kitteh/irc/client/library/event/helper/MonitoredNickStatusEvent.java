package org.kitteh.irc.client.library.event.helper;

import javax.annotation.Nonnull;

/**
 * A tracked nick has come online or offline.
 */
public interface MonitoredNickStatusEvent extends ServerMessageEvent {
    /**
     * Gets the tracked nick.
     *
     * @return nick
     */
    @Nonnull
    String getNick();
}
