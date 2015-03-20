package org.kitteh.irc.client.library.event.helper;

import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;

/**
 * A {@link Channel} is gaining or losing a {@link User}
 */
public interface ChannelUserListChange {
    public enum Change {
        JOIN,
        NICK_CHANGE,
        LEAVE;
    }

    /**
     * Gets the type of change occurring.
     *
     * @return type of change
     */
    Change getChange();

    /**
     * Gets the channel affected or null if affecting all channels the user
     * is present in.
     *
     * @return channel or null for all channels
     */
    Channel getChannel();

    /**
     * Gets the current user affected.
     *
     * @return user
     */
    User getUser();
}