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

import net.engio.mbassy.listener.Handler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.event.channel.ChannelNamesUpdatedEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Default NAMES listener, producing events using default classes.
 */
public class DefaultNamesListener extends AbstractDefaultListenerBase {
    private final List<ServerMessage> namesMessages = new ArrayList<>();

    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultNamesListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @NumericFilter(353) // NAMES
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void names(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 4) {
            this.trackException(event, "NAMES response too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getChannel(event.getParameters().get(2));
        if (!channel.isPresent()) {
            this.trackException(event, "NAMES response sent for invalid channel name");
            return;
        }
        List<ChannelUserMode> channelUserModes = this.getClient().getServerInfo().getChannelUserModes();
        for (String combo : event.getParameters().get(3).split(" ")) {
            Set<ChannelUserMode> modes = new HashSet<>();
            for (int i = 0; i < combo.length(); i++) {
                char c = combo.charAt(i);
                Optional<ChannelUserMode> mode = channelUserModes.stream().filter(userMode -> userMode.getNickPrefix() == c).findFirst();
                if (mode.isPresent()) {
                    modes.add(mode.get());
                } else {
                    this.getTracker().trackChannelNick(channel.get().getName(), combo.substring(i), modes);
                    break;
                }
            }
        }
        this.namesMessages.add(event.getServerMessage());
    }

    @NumericFilter(366) // End of NAMES
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void namesComplete(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "NAMES response too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getChannel(event.getParameters().get(1));
        if (!channel.isPresent()) {
            this.trackException(event, "NAMES response sent for invalid channel name");
            return;
        }
        this.namesMessages.add(event.getServerMessage());
        this.fire(new ChannelNamesUpdatedEvent(this.getClient(), this.namesMessages, channel.get()));
        this.namesMessages.clear();
    }
}
