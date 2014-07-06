package org.kitteh.irc.event.channel;

import org.kitteh.irc.elements.Channel;
import org.kitteh.irc.elements.User;
import org.kitteh.irc.event.ActorChannelMessageEvent;

/**
 * A {@link User} has left a {@link Channel}!
 */
public class ChannelPartEvent extends ActorChannelMessageEvent<User> {
    /**
     * Creates the event.
     *
     * @param channel channel being left
     * @param user user leaving
     * @param message message the user left
     */
    public ChannelPartEvent(Channel channel, User user, String message) {
        super(user, channel, message);
    }
}