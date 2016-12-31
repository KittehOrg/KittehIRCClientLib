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
package org.kitteh.irc.client.library.event.channel;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.event.abstractbase.ActorChannelEventBase;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Channel a la mode.
 */
public class ChannelModeEvent extends ActorChannelEventBase<Actor> {
    private final ModeStatusList<ChannelMode> statusList;

    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param originalMessages original messages
     * @param actor the mode setter
     * @param channel the channel in which the change is occurring
     * @param statusList list of statuses
     */
    public ChannelModeEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull Actor actor, @Nonnull Channel channel, @Nonnull ModeStatusList<ChannelMode> statusList) {
        super(client, originalMessages, actor, channel);
        this.statusList = statusList;
    }

    /**
     * Gets the list of statuses.
     *
     * @return status list
     */
    @Nonnull
    public ModeStatusList<ChannelMode> getStatusList() {
        return this.statusList;
    }

    @Override
    @Nonnull
    protected ToStringer toStringer() {
        return super.toStringer().add("statusList", this.statusList);
    }
}
