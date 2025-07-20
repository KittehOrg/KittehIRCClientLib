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
import org.jspecify.annotations.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.FailEvent;
import org.kitteh.irc.client.library.event.client.NoteEvent;
import org.kitteh.irc.client.library.event.client.StandardReplyEvent;
import org.kitteh.irc.client.library.event.client.WarnEvent;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Default FAIL/NOTE/WARN listener, producing events using default classes.
 */
public class DefaultStandardReplyListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultStandardReplyListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @CommandFilter("FAIL")
    @CommandFilter("NOTE")
    @CommandFilter("WARN")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void invite(ClientReceiveCommandEvent event) {
        StandardReplyEvent.Type type = StandardReplyEvent.Type.valueOf(event.getCommand().toUpperCase());
        if (event.getParameters().size() < 3) {
            this.trackException(event, type + " message too short");
            return;
        }
        String command = event.getParameters().get(0);
        String code = event.getParameters().get(1);
        List<String> context = new ArrayList<>();
        int i = 2;
        while ((i + 1) < event.getParameters().size()) {
            context.add(event.getParameters().get(i++));
        }
        String description = event.getParameters().get(i);
        switch (type) {
            case FAIL:
                this.fire(new FailEvent(this.getClient(), event.getSource(), command, code, context, description));
                break;
            case NOTE:
                this.fire(new NoteEvent(this.getClient(), event.getSource(), command, code, context, description));
                break;
            case WARN:
                this.fire(new WarnEvent(this.getClient(), event.getSource(), command, code, context, description));
                break;
        }
    }
}
