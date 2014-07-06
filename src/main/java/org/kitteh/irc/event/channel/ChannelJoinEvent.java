package org.kitteh.irc.event.channel;

import org.kitteh.irc.elements.Channel;
import org.kitteh.irc.elements.User;
import org.kitteh.irc.event.ActorChannelEvent;

/**
 * A {@link User} has joined a {@link Channel}!
 */
public class ChannelJoinEvent extends ActorChannelEvent<User> {
    /**
     * Creates the event.
     *
     * @param channel the channel joined
     * @param user the user joining
     */
    public ChannelJoinEvent(Channel channel, User user) {
        super(user, channel);
    }
}
