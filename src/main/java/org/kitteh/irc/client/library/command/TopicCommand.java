/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TOPICal command support.
 */
public class TopicCommand extends ChannelCommand {
    @Nullable
    private String topic;

    /**
     * Constructs a TOPIC command for a given channel.
     *
     * @param client the client on which this command is executing
     * @param channel channel targeted
     * @throws IllegalArgumentException if null parameters
     */
    public TopicCommand(@Nonnull Client client, @Nonnull String channel) {
        super(client, channel);
    }

    /**
     * Sets the topic.
     *
     * @param topic new topic or null to query the current topic
     * @return this TopicCommand
     * @throws IllegalArgumentException if topic invalid
     */
    @Nonnull
    public TopicCommand topic(@Nullable String topic) {
        this.topic = (topic == null) ? null : Sanity.safeMessageCheck(topic, "Topic");
        return this;
    }

    /**
     * Sets this command to query the channel's current topic.
     *
     * @return this TopicCommand
     */
    @Nonnull
    public TopicCommand query() {
        this.topic = null;
        return this;
    }

    @Override
    public synchronized void execute() {
        this.getClient().sendRawLine("TOPIC " + this.getChannel() + (this.topic == null ? "" : (" :" + this.topic)));
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("client", this.getClient()).add("channel", this.getChannel()).add("topic", this.topic).toString();
    }
}
