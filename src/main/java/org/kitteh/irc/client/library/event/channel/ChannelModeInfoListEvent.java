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
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ModeInfo;
import org.kitteh.irc.client.library.event.abstractbase.ChannelEventBase;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * A list of mode info is available!
 */
public class ChannelModeInfoListEvent extends ChannelEventBase {
    private final ChannelMode mode;
    private final List<ModeInfo> info;

    /**
     * Constructs the event.
     *
     * @param client client for which this is occurring
     * @param originalMessages original messages
     * @param channel channel with this info
     * @param mode mode for which the info exists
     * @param info list of info
     */
    public ChannelModeInfoListEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull Channel channel, @Nonnull ChannelMode mode, @Nonnull List<ModeInfo> info) {
        super(client, originalMessages, channel);
        this.mode = mode;
        this.info = Collections.unmodifiableList(info);
    }

    /**
     * Gets the mode info's mode.
     *
     * @return mode
     */
    @Nonnull
    public ChannelMode getMode() {
        return this.mode;
    }

    /**
     * Gets the channel's mode info.
     *
     * @return info
     */
    @Nonnull
    public List<ModeInfo> getModeInfo() {
        return this.info;
    }

    @Override
    @Nonnull
    protected ToStringer toStringer() {
        return super.toStringer().add("mode", this.mode).add("info", this.info);
    }
}
