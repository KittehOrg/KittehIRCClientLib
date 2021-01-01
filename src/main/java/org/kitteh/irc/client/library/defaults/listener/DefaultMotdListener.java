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
package org.kitteh.irc.client.library.defaults.listener;

import net.engio.mbassy.listener.Handler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.client.ClientReceiveMotdEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Default MOTD listener, producing events using default classes.
 */
public class DefaultMotdListener extends AbstractDefaultListenerBase {
    private final List<String> motd = new ArrayList<>();
    private final List<ServerMessage> motdMessages = new ArrayList<>();

    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultMotdListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @NumericFilter(375)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void motdStart(ClientReceiveNumericEvent event) {
        this.motd.clear();
        this.motdMessages.clear();
    }

    @NumericFilter(372)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void motdContent(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "MOTD message too short");
            return;
        }
        this.motd.add(event.getParameters().get(1));
        this.motdMessages.add(event.getServerMessage());
    }

    @NumericFilter(376)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void motdEnd(ClientReceiveNumericEvent event) {
        this.motdMessages.add(event.getServerMessage());
        this.getClient().getServerInfo().setMotd(new ArrayList<>(this.motd));
        this.fire(new ClientReceiveMotdEvent(this.getClient(), this.motdMessages));
    }
}
