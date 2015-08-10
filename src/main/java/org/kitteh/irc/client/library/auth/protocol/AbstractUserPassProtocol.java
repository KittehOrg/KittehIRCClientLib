/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.auth.protocol;

import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.auth.protocol.element.EventListening;
import org.kitteh.irc.client.library.auth.protocol.element.Password;
import org.kitteh.irc.client.library.auth.protocol.element.Username;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.util.NumericFilter;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;

/**
 * Abstract general username/password protocol.
 */
public abstract class AbstractUserPassProtocol implements Password, Username {
    public static abstract class WithListener extends AbstractUserPassProtocol implements EventListening {
        private class Listener {
            @NumericFilter(4)
            @Handler(filters = @Filter(NumericFilter.Filter.class))
            public void listenVersion(ClientReceiveNumericEvent event) {
                WithListener.this.startAuthentication();
            }
        }

        private final Listener listener = new Listener();

        /**
         * Creates an instance.
         *
         * @param client client
         * @param username username
         * @param password password
         */
        protected WithListener(Client client, String username, String password) {
            super(client, username, password);
        }

        @Override
        public Object getEventListener() {
            return this.listener;
        }
    }

    private final Client client;
    private String password;
    private String username;

    /**
     * Creates an instance.
     *
     * @param client client
     * @param username username
     * @param password password
     */
    protected AbstractUserPassProtocol(Client client, String username, String password) {
        Sanity.nullCheck(client, "Client cannot be null");
        Sanity.safeMessageCheck(username, "Username");
        Sanity.safeMessageCheck(password, "Password");
        this.client = client;
        this.username = username;
        this.password = password;
    }

    @Nonnull
    @Override
    public Client getClient() {
        return this.client;
    }

    @Nonnull
    @Override
    public String getPassword() {
        return this.password;
    }

    @Nonnull
    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void setPassword(@Nonnull String password) {
        Sanity.safeMessageCheck(password, "Password");
        this.password = password;
    }

    @Override
    public void setUsername(@Nonnull String username) {
        Sanity.safeMessageCheck(username, "Username");
        this.username = username;
    }

    @Override
    public final void startAuthentication() {
        this.client.sendRawLineImmediately(this.getAuthentication());
    }

    /**
     * Gets a String for {@link #startAuthentication()}.
     *
     * @return auth string
     */
    protected abstract String getAuthentication();
}