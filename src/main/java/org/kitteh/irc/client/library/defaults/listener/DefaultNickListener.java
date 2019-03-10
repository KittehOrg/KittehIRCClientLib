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
package org.kitteh.irc.client.library.defaults.listener;

import net.engio.mbassy.listener.Handler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.user.UserNickChangeEvent;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;

import java.util.Optional;

/**
 * Default NICK listener, producing events using default classes.
 */
public class DefaultNickListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultNickListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @CommandFilter("NICK")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void nick(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 1) {
            this.trackException(event, "NICK message too short");
            return;
        }
        if (event.getActor() instanceof User) {
            boolean isSelf = ((User) event.getActor()).getNick().equals(this.getClient().getNick());
            Optional<User> user = this.getTracker().getTrackedUser(((User) event.getActor()).getNick());
            if (!user.isPresent()) {
                if (isSelf) {
                    this.getClient().setCurrentNick(event.getParameters().get(0));
                    return; // Don't fail if NICK changes while not in a channel!
                }
                this.trackException(event, "NICK message sent for user not in tracked channels");
                return;
            }
            User oldUser = user.get();
            this.getTracker().trackUserNickChange(user.get().getNick(), event.getParameters().get(0));
            User newUser = user.get();
            this.fire(new UserNickChangeEvent(this.getClient(), event.getSource(), oldUser, newUser));
            if (isSelf) {
                this.getClient().setCurrentNick(event.getParameters().get(0));
            }
        } else {
            this.trackException(event, "NICK message sent for non-user");
        }
    }
}
