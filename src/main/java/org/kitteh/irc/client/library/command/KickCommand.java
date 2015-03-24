package org.kitteh.irc.client.library.command;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.util.Sanity;

/**
 * Get your KICKs on Route 66.
 */
public class KickCommand extends ChannelCommand {
    private String target;
    private String reason;

    public KickCommand(Client client, Channel channel) {
        super(client, channel);
    }

    public KickCommand(Client client, String channel) {
        super(client, channel);
    }

    /**
     * Sets the target of this kick.
     *
     * @param target target
     * @return this command
     */
    public KickCommand target(String target) {
        Sanity.nullCheck(target, "Target cannot be null");
        Sanity.safeMessageCheck(target, "target");
        this.target = target;
        return this;
    }

    /**
     * Sets the target of this kick.
     *
     * @param target target
     * @return this command
     */
    public KickCommand target(User target) {
        Sanity.nullCheck(target, "Target cannot be null");
        Sanity.truthiness(target.getClient() == this.getClient(), "User comes from a different client");
        this.target(target.getNick());
        return this;
    }

    /**
     * Sets the reason for this kick.
     *
     * @param reason or null for no reason
     * @return this command
     */
    public KickCommand reason(String reason) {
        Sanity.safeMessageCheck(reason);
        this.reason = reason;
        return this;
    }

    @Override
    public void execute() {
        Sanity.nullCheck(this.target, "Target not defined");
        StringBuilder builder = new StringBuilder();
        builder.append("KICK ").append(this.getChannel()).append(' ').append(this.target);
        if (this.reason != null) {
            builder.append(" :").append(this.reason);
        }
        this.getClient().sendRawLine(builder.toString());
    }
}