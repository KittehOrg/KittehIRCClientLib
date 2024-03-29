/*
 * * Copyright (C) 2013-2023 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.feature.auth;

import net.engio.mbassy.listener.Handler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.feature.auth.element.EventListening;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

/**
 * GameSurge's AuthServ protocol. Automatically attempts to identify upon connection.
 */
public class GameSurge extends AbstractAccountPassProtocol implements EventListening {
    private class Listener {
        @NumericFilter(4)
        @Handler
        public void listenVersion(ClientReceiveNumericEvent event) {
            GameSurge.this.startAuthentication();
        }

        @Override
        public @NonNull String toString() {
            return new ToStringer(this).toString();
        }
    }

    private final Listener listener = new Listener();

    /**
     * Creates a GameSurge authentication protocol instance.
     *
     * @param client client for which this will be used
     * @param accountName account name
     * @param password password
     */
    public GameSurge(@NonNull Client client, @NonNull String accountName, @NonNull String password) {
        super(client, Sanity.safeMessageCheck(accountName, "Account name"), password);
    }

    @Override
    protected @NonNull String getAuthentication() {
        return "PRIVMSG AuthServ@services.gamesurge.net :auth " + this.getAccountName() + ' ' + this.getPassword();
    }

    @Override
    public @NonNull Object getEventListener() {
        return this.listener;
    }
}
