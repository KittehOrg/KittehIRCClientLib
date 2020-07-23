/*
 * * Copyright (C) 2013-2020 Matt Baxter https://kitteh.org
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ServerMessageEventBase;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fires when a MONITOR target addition command is rejected.
 */
public class MonitoredNickListFullEvent extends ServerMessageEventBase {
    private final int limit;
    private final List<String> rejectedNicks;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param sourceMessage source message
     * @param limit limit
     * @param rejectedNicks rejected nicks
     */
    public MonitoredNickListFullEvent(@NonNull Client client, @NonNull ServerMessage sourceMessage, int limit, @NonNull List<String> rejectedNicks) {
        super(client, sourceMessage);
        this.limit = limit;
        this.rejectedNicks = Collections.unmodifiableList(new ArrayList<>(Sanity.nullCheck(rejectedNicks, "Rejected nicks cannot be null")));
    }

    /**
     * Gets the maximum number of targets a client can have.
     *
     * @return target limit
     */
    public int getLimit() {
        return this.limit;
    }

    /**
     * Gets the nicknames not added due to the limit.
     *
     * @return rejected nicknames
     */
    public @NonNull List<String> getRejectedNicks() {
        return this.rejectedNicks;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("limit", this.limit).add("rejectedNicks", this.rejectedNicks);
    }
}
