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
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.user.UserRealnameChangeEvent;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;

import java.util.Optional;

/**
 * Default SETNAME listener, producing events using default classes.
 */
public class DefaultSetNameListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultSetNameListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @CommandFilter("SETNAME")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void setname(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() != 1) {
            this.trackException(event, "Invalid number of parameters for SETNAME message");
            return;
        }

        if (!(event.getActor() instanceof User)) {
            this.trackException(event, "Invalid actor for SETNAME message");
            return;
        }

        User user = (User) event.getActor();
        Optional<User> optUser = this.getTracker().getTrackedUser(user.getNick());

        if (!optUser.isPresent()) {
            this.trackException(event, "Null old user for nick");
            return;
        }

        User oldUser = optUser.get();

        String newRealName = event.getParameters().get(0);
        this.getTracker().trackUserRealnameChange(user.getNick(), newRealName);
        this.fire(new UserRealnameChangeEvent(this.getClient(), event.getSource(), oldUser, this.getTracker().getTrackedUser(user.getNick()).get()));
    }
}
