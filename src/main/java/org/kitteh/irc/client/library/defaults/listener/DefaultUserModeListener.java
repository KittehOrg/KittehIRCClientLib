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
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;
import org.kitteh.irc.client.library.util.StringUtil;

/**
 * Default UMODE listener, producing events using default classes.
 */
public class DefaultUserModeListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultUserModeListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @NumericFilter(221) // UMODEIS
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void umode(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "UMODE response too short");
            return;
        }

        if (!this.getClient().getServerInfo().getCaseMapping().areEqualIgnoringCase(event.getParameters().get(0), this.getClient().getNick())) {
            this.trackException(event, "UMODE response for another user");
            return;
        }
        ModeStatusList<UserMode> modes;
        try {
            modes = ModeStatusList.fromUser(this.getClient(), StringUtil.combineSplit(event.getParameters().toArray(new String[event.getParameters().size()]), 1));
        } catch (IllegalArgumentException e) {
            this.trackException(event, e.getMessage());
            return;
        }
        this.getClient().setUserModes(modes);
    }
}
