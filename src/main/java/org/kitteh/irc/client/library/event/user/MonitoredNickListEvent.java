/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.event.user;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ServerMessageEventBase;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fires when the server sends the full list of tracked nicknames for the
 * MONITOR feature.
 */
public class MonitoredNickListEvent extends ServerMessageEventBase {
    private final List<String> nicks;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param nicks nicknames tracked
     */
    public MonitoredNickListEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull List<String> nicks) {
        super(client, originalMessages);
        this.nicks = Collections.unmodifiableList(new ArrayList<>(Sanity.nullCheck(nicks, "Nicks cannot be null")));
    }

    /**
     * Gets the tracked nicknames.
     *
     * @return tracked nicknames
     */
    public List<String> getNicks() {
        return this.nicks;
    }

    @Override
    @Nonnull
    protected ToStringer toStringer() {
        return super.toStringer().add("nicks", this.nicks);
    }
}
