/*
 * * Copyright (C) 2013-2021 Matt Baxter https://kitteh.org
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

/**
 * TOPICal command support.
 */
public class TopicCommand extends ChannelCommand<TopicCommand> {
    private @Nullable String topic;

    /**
     * Constructs a TOPIC command for a given channel.
     *
     * @param client the client on which this command is executing
     * @param channel channel targeted
     * @throws IllegalArgumentException if null parameters
     */
    public TopicCommand(@NonNull Client client, @NonNull String channel) {
        super(client, channel);
    }

    /**
     * Sets the topic.
     *
     * @param topic new topic or null to query the current topic
     * @return this TopicCommand
     * @throws IllegalArgumentException if topic invalid
     */
    public @NonNull TopicCommand topic(@Nullable String topic) {
        this.topic = (topic == null) ? null : Sanity.safeMessageCheck(topic, "Topic");
        return this;
    }

    /**
     * Sets this command to query the channel's current topic.
     *
     * @return this TopicCommand
     */
    public @NonNull TopicCommand query() {
        this.topic = null;
        return this;
    }

    @Override
    public synchronized void execute() {
        this.sendCommandLine("TOPIC " + this.getChannel() + (this.topic == null ? "" : (" :" + this.topic)));
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("topic", this.topic);
    }
}
