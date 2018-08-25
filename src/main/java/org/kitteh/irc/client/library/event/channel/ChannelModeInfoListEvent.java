/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ModeInfo;
import org.kitteh.irc.client.library.event.abstractbase.ChannelEventBase;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

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
    public ChannelModeInfoListEvent(@NonNull Client client, @NonNull List<ServerMessage> originalMessages, @NonNull Channel channel, @NonNull ChannelMode mode, @NonNull List<ModeInfo> info) {
        super(client, originalMessages, channel);
        this.mode = Sanity.nullCheck(mode, "Mode cannot be null");
        this.info = Collections.unmodifiableList(Sanity.nullCheck(info, "Info cannot be null"));
    }

    /**
     * Gets the mode info's mode.
     *
     * @return mode
     */
    public @NonNull ChannelMode getMode() {
        return this.mode;
    }

    /**
     * Gets the channel's mode info.
     *
     * @return info
     */
    public @NonNull List<ModeInfo> getModeInfo() {
        return this.info;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("mode", this.mode).add("info", this.info);
    }
}
