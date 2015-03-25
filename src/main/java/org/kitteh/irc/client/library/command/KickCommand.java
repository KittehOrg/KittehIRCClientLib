/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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