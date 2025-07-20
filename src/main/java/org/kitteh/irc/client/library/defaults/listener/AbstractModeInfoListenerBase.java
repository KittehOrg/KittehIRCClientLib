/*
 * * Copyright (C) 2013-2025 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.defaults.listener;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.mode.DefaultModeInfo;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ModeInfo;
import org.kitteh.irc.client.library.event.channel.ChannelModeInfoListEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A base for listening to mode info events.
 */
public class AbstractModeInfoListenerBase extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public AbstractModeInfoListenerBase(Client.@NonNull WithManagement client) {
        super(client);
    }

    /**
     * Adds an item to a mode info list.
     *
     * @param event event
     * @param name name in list
     * @param mode mode
     * @param messageList list of server messages
     * @param infoList info list to append
     */
    protected void modeInfoList(@NonNull ClientReceiveNumericEvent event, @NonNull String name, char mode, @NonNull List<ServerMessage> messageList, @NonNull List<ModeInfo> infoList) {
        this.modeInfoList(event, name, mode, messageList, infoList, 0);
    }

    /**
     * Adds an item to a mode info list.
     *
     * @param event event
     * @param name name in list
     * @param mode mode
     * @param messageList list of server messages
     * @param infoList info list to append
     * @param offset offset
     */
    protected void modeInfoList(@NonNull ClientReceiveNumericEvent event, @NonNull String name, char mode, @NonNull List<ServerMessage> messageList, @NonNull List<ModeInfo> infoList, int offset) {
        if (event.getParameters().size() < (3 + offset)) {
            this.trackException(event, name + " response too short");
            return;
        }
        Channel channel;
        if ((channel = this.getChannel(event, name)) == null) {
            return;
        }
        ChannelMode channelMode;
        if ((channelMode = this.getChannelMode(event, name, mode)) == null) {
            return;
        }

        messageList.add(event.getServerMessage());
        String creator = (event.getParameters().size() > (3 + offset)) ? event.getParameters().get((3 + offset)) : null;
        Instant creationTime = null;
        if (event.getParameters().size() > (4 + offset)) {
            try {
                creationTime = Instant.ofEpochSecond(Integer.parseInt(event.getParameters().get((4 + offset))));
            } catch (NumberFormatException | DateTimeException ignored) {
            }
        }
        infoList.add(new DefaultModeInfo(this.getClient(), channel, channelMode, event.getParameters().get((2 + offset)), creator, creationTime));
    }

    /**
     * Handles the end of the mode info list.
     *
     * @param event event
     * @param name name
     * @param mode mode
     * @param messageList list of messages
     * @param infoList mode info list
     */
    protected void endModeInfoList(@NonNull ClientReceiveNumericEvent event, @NonNull String name, char mode, @NonNull List<ServerMessage> messageList, @NonNull List<ModeInfo> infoList) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, name + " response too short");
            return;
        }
        Channel channel;
        if ((channel = this.getChannel(event, name)) == null) {
            return;
        }
        ChannelMode channelMode;
        if ((channelMode = this.getChannelMode(event, name, mode)) == null) {
            return;
        }

        messageList.add(event.getServerMessage());
        List<ModeInfo> modeInfos = new ArrayList<>(infoList);
        this.fire(new ChannelModeInfoListEvent(this.getClient(), messageList, channel, channelMode, modeInfos));
        this.getTracker().setChannelModeInfoList(channel.getName(), mode, modeInfos);
        infoList.clear();
        messageList.clear();
    }

    protected @Nullable Channel getChannel(@NonNull ClientReceiveNumericEvent event, @NonNull String name) {
        Channel channel = this.getTracker().getChannel(event.getParameters().get(1)).orElse(null);
        if (channel == null) {
            this.trackException(event, name + " response sent for invalid channel name");
        }
        return channel;
    }

    protected @Nullable ChannelMode getChannelMode(@NonNull ClientReceiveNumericEvent event, @NonNull String name, char mode) {
        ChannelMode channelMode = this.getClient().getServerInfo().getChannelMode(mode).orElse(null);
        if (channelMode == null) {
            this.trackException(event, name + " can't list if there's no '" + mode + "' mode");
        }
        return channelMode;
    }
}
