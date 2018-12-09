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
package org.kitteh.irc.client.library.defaults.listener;

import net.engio.mbassy.listener.Handler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.helper.MonitoredNickStatusEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickListEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickListFullEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickOfflineEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickOnlineEvent;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default MONITOR listener, producing events using default classes.
 */
public class DefaultMonitorListener extends AbstractDefaultListenerBase {
    private final List<String> monitorList = new ArrayList<>();
    private final List<ServerMessage> monitorListMessages = new ArrayList<>();

    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultMonitorListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @NumericFilter(730) // Monitor online
    @NumericFilter(731) // Monitor offline
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void monitorOnline(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "MONITOR status message too short");
            return;
        }
        List<ServerMessage> originalMessages = event.getOriginalMessages();
        for (String nick : event.getParameters().get(1).split(",")) {
            MonitoredNickStatusEvent monitorEvent;
            if (event.getNumeric() == 730) {
                monitorEvent = new MonitoredNickOnlineEvent(this.getClient(), originalMessages, nick);
            } else {
                monitorEvent = new MonitoredNickOfflineEvent(this.getClient(), originalMessages, nick);
            }
            this.fire(monitorEvent);
        }
    }

    @NumericFilter(732) // Monitor list
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void monitorList(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "MONITOR list message too short");
            return;
        }
        Collections.addAll(this.monitorList, event.getParameters().get(1).split(","));
        this.monitorListMessages.add(event.getServerMessage());
    }

    @NumericFilter(733) // Monitor list end
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void monitorListEnd(ClientReceiveNumericEvent event) {
        this.fire(new MonitoredNickListEvent(this.getClient(), this.monitorListMessages, this.monitorList));
        this.monitorList.clear();
        this.monitorListMessages.clear();
    }

    @NumericFilter(734) // Monitor list full
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void monitorListFull(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "MONITOR list full message too short");
            return;
        }
        int limit;
        try {
            limit = Integer.parseInt(event.getParameters().get(1));
        } catch (NumberFormatException e) {
            this.trackException(event, "MONITOR list full message using non-int limit");
            return;
        }
        this.fire(new MonitoredNickListFullEvent(this.getClient(), event.getOriginalMessages(), limit, Arrays.stream(event.getParameters().get(2).split(",")).collect(Collectors.toList())));
    }
}
