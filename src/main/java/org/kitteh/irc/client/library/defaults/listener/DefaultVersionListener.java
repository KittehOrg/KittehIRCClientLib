/*
 * * Copyright (C) 2013-2026 Matt Baxter https://kitteh.org
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.mode.DefaultUserMode;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.event.client.ClientNegotiationCompleteEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.helper.ClientReceiveServerMessageEvent;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Default version listener, producing events using default classes.
 */
public class DefaultVersionListener extends AbstractDefaultListenerBase {
    private @Nullable Actor negotiationServer;
    private @Nullable ServerInfo negotiationServerInfo;

    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultVersionListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @NumericFilter(4)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void version(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() > 1) {
            this.getClient().getServerInfo().setAddress(event.getParameters().get(1));
            if (event.getParameters().size() > 2) {
                this.getClient().getServerInfo().setVersion(event.getParameters().get(2));
                if (event.getParameters().size() > 3) {
                    List<UserMode> modes = new ArrayList<>(event.getParameters().get(3).length());
                    for (char mode : event.getParameters().get(3).toCharArray()) {
                        modes.add(new DefaultUserMode(this.getClient(), mode));
                    }
                    this.getClient().getServerInfo().setUserModes(modes);
                } else {
                    this.trackException(event, "Server user modes missing");
                }
            } else {
                this.trackException(event, "Server version and user modes missing");
            }
        } else {
            this.trackException(event, "Server address, version, and user modes missing");
        }
        this.getClient().sendRawLineImmediately("WHOIS " + this.getClient().getNick());
        this.negotiationServer = event.getActor();
        this.negotiationServerInfo = this.getClient().getServerInfo();
    }

    // ISUPPORT (005) follows 004 with no end marker, so negotiation completes on the first line after 004 that isn't 005
    @Handler(priority = Integer.MAX_VALUE)
    public void negotiationComplete(ClientReceiveServerMessageEvent event) {
        Actor server = this.negotiationServer;
        if ((server == null) || ((event instanceof ClientReceiveNumericEvent numeric) && (numeric.getNumeric() == 5))) {
            return;
        }
        this.negotiationServer = null;
        // A reconnect since 004 replaces the ServerInfo, in order with incoming lines
        if (this.negotiationServerInfo != this.getClient().getServerInfo()) {
            return;
        }
        this.fire(new ClientNegotiationCompleteEvent(this.getClient(), server, this.getClient().getServerInfo()));
        this.getClient().startSending();
    }
}
