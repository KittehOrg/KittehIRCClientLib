/*
 * * Copyright (C) 2013-2019 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

/**
 * A command only executed on a channel.
 */
public abstract class ChannelCommand extends Command {
    private final String channel;

    /**
     * Constructs a command for a given channel.
     *
     * @param client the client on which this command is executing
     * @param channel channel targeted
     * @throws IllegalArgumentException if null parameters
     */
    protected ChannelCommand(@NonNull Client client, @NonNull String channel) {
        super(client);
        Sanity.safeMessageCheck(channel, "Channel");
        Sanity.truthiness(client.getServerInfo().isValidChannel(channel), "Invalid channel name '" + channel + '\'');
        this.channel = channel;
    }

    /**
     * Gets the channel this command affects.
     *
     * @return channel relevant to this command
     */
    public @NonNull String getChannel() {
        return this.channel;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("channel", this.getChannel());
    }
}
