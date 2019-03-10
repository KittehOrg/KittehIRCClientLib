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
package org.kitteh.irc.client.library.feature.auth;

import net.engio.mbassy.listener.Handler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.user.PrivateNoticeEvent;
import org.kitteh.irc.client.library.feature.auth.element.EventListening;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;
import org.kitteh.irc.client.library.util.Format;
import org.kitteh.irc.client.library.util.Sanity;

/**
 * NickServ protocol. Automatically attempts to identify upon connection.
 */
public class NickServ extends AbstractAccountPassProtocol implements EventListening {
    /**
     * NickServ builder.
     */
    public static class Builder {
        private final Client client;
        private String serviceName = "NickServ";
        private @Nullable String account;
        private @Nullable String password;

        /**
         * Constructs the builder with the given client.
         *
         * @param client client
         */
        protected Builder(final @NonNull Client client) {
            this.client = Sanity.nullCheck(client, "Client");
        }

        /**
         * Sets the service name.
         *
         * @param serviceName service name
         * @return this builder
         */
        public @NonNull Builder serviceName(@Nullable String serviceName) {
            this.serviceName = Sanity.safeMessageCheck(serviceName, "Service name");
            return this;
        }

        /**
         * Sets the account.
         *
         * @param account account
         * @return this builder
         */
        public @NonNull Builder account(@Nullable String account) {
            this.account = Sanity.safeMessageCheck(account, "Account");
            return this;
        }

        /**
         * Sets the password.
         *
         * @param password password
         * @return this builder
         */
        public @NonNull Builder password(@Nullable String password) {
            this.password = Sanity.safeMessageCheck(password, "Password");
            return this;
        }

        /**
         * Builds NickServ.
         *
         * @return nickserv
         * @throws IllegalArgumentException if password is not set
         */
        public @NonNull NickServ build() {
            Sanity.truthiness(this.password != null, "Password must be set");
            return new NickServ(this.client, this.serviceName, this.account, this.password);
        }
    }

    /**
     * Creates a new NickServ builder.
     *
     * @param client client for whom this is to be built
     * @return new builder
     */
    public static @NonNull Builder builder(@NonNull Client client) {
        return new Builder(client);
    }

    private final String serviceName;

    /**
     * Creates a NickServ authentication protocol instance.
     *
     * @param client client for which this will be used
     * @param serviceName service name
     * @param accountName account name
     * @param password password
     */
    protected NickServ(@NonNull Client client, @NonNull String serviceName, @Nullable String accountName, @NonNull String password) {
        super(client, accountName, password);
        this.serviceName = Sanity.safeMessageCheck(serviceName, "Service name");
    }

    @Override
    protected @NonNull String getAuthentication() {
        final String accountName = this.getAccountName();
        return "PRIVMSG " + this.serviceName + " :IDENTIFY " + (accountName == null ? "" : (accountName + ' ')) + this.getPassword();
    }

    @Override
    public @NonNull Object getEventListener() {
        return this;
    }

    @NumericFilter(4)
    @Handler
    public void listenVersion(ClientReceiveNumericEvent event) {
        this.startAuthentication();
    }

    @Handler
    public void listenSuccess(PrivateNoticeEvent event) {
        if (event.getActor().getNick().equals(this.serviceName)) {
            if (event.getMessage().startsWith("You are now identified")) {
                int first;
                String accountName = event.getMessage().substring((first = event.getMessage().indexOf(Format.BOLD.toString()) + 1), event.getMessage().indexOf(Format.BOLD.toString(), first));
                // TODO do something with this information
            }
        }
    }
}
