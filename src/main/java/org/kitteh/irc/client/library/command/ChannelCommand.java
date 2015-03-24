package org.kitteh.irc.client.library.command;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.util.Sanity;

/**
 * A command only executed on a chanenl.
 */
public abstract class ChannelCommand extends Command {
    private final String channel;

    /**
     * Constructs a command for a given channel.
     *
     * @param client the client on which this command is executing
     * @param channel channel targeted
     */
    public ChannelCommand(Client client, Channel channel) {
        super(client);
        Sanity.nullCheck(channel, "Channel cannot be null");
        Sanity.truthiness(channel.getClient() == client, "Channel comes from a different Client");
        this.channel = channel.getName();
    }

    /**
     * Constructs a command for a given channel.
     *
     * @param client the client on which this command is executing
     * @param channel channel targeted
     */
    public ChannelCommand(Client client, String channel) {
        super(client);
        Sanity.nullCheck(channel, "Channel cannot be null");
        Sanity.nullCheck(client.getChannel(channel), "Invalid channel name '" + channel + "'");
        this.channel = channel;
    }

    public String getChannel() {
        return this.channel;
    }
}