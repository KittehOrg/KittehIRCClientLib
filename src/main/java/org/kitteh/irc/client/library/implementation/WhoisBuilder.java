/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.WhoisData;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

class WhoisBuilder {
    private class Whois implements WhoisData {
        private final Client client;
        private final Optional<String> account;
        private final Set<String> channels;
        private final String name;
        private final String nick;
        private final String userString;
        private final String host;
        private final long creationTime;
        private final Optional<String> realName;
        private final Optional<String> server;
        private final Optional<Long> idleTime;
        private final Optional<String> serverDescription;
        private final boolean secureConnection;
        private final Optional<String> operatorInformation;
        private final Optional<Long> signOnTime;
        private final boolean away;
        private final Optional<String> awayMessage;

        private Whois(@Nonnull Client client, @Nullable String account, @Nonnull Set<String> channels, @Nonnull String nick, @Nonnull String userString,
                      @Nonnull String host, @Nullable String realName, @Nullable String server, @Nullable String serverDescription, boolean secureConnection,
                      @Nullable String operatorInformation, @Nullable Long idleTime, @Nullable Long signOnTime, @Nullable String awayMessage) {
            this.client = client;
            this.account = Optional.ofNullable(account);
            this.channels = Collections.unmodifiableSet(new HashSet<>(channels));
            this.name = nick + '!' + userString + '@' + host;
            this.nick = nick;
            this.userString = userString;
            this.host = host;
            this.realName = Optional.ofNullable(realName);
            this.server = Optional.ofNullable(server);
            this.serverDescription = Optional.ofNullable(serverDescription);
            this.operatorInformation = Optional.ofNullable(operatorInformation);
            this.secureConnection = secureConnection;
            this.idleTime = Optional.ofNullable(idleTime);
            this.signOnTime = Optional.ofNullable(signOnTime);
            this.away = awayMessage != null;
            this.awayMessage = Optional.ofNullable(awayMessage);
            this.creationTime = System.currentTimeMillis();
        }

        @Nonnull
        @Override
        public Optional<String> getAccount() {
            return this.account;
        }

        @Nonnull
        @Override
        public Set<String> getChannels() {
            return this.channels;
        }

        @Nonnull
        @Override
        public String getHost() {
            return this.host;
        }

        @Nonnull
        @Override
        public String getNick() {
            return this.nick;
        }

        @Nonnull
        @Override
        public Optional<String> getRealName() {
            return this.realName;
        }

        @Nonnull
        @Override
        public Optional<String> getServer() {
            return this.server;
        }

        @Nonnull
        @Override
        public String getUserString() {
            return this.userString;
        }

        @Override
        public boolean isAway() {
            return this.away;
        }

        @Nonnull
        @Override
        public String getMessagingName() {
            return this.nick;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Nonnull
        @Override
        public Client getClient() {
            return this.client;
        }

        @Override
        public long getCreationTime() {
            return this.creationTime;
        }

        @Nonnull
        @Override
        public Optional<Long> getIdleTime() {
            return this.idleTime;
        }

        @Nonnull
        @Override
        public Optional<String> getOperatorInformation() {
            return this.operatorInformation;
        }

        @Nonnull
        @Override
        public Optional<String> getServerDescription() {
            return this.serverDescription;
        }

        @Nonnull
        @Override
        public Optional<Long> getSignOnTime() {
            return this.signOnTime;
        }

        @Override
        public boolean isSecureConnection() {
            return this.secureConnection;
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this)
                    .add("client", this.client)
                    .add("account", this.account)
                    .add("channels", this.channels)
                    .add("name", this.name)
                    .add("creationTime", this.creationTime)
                    .add("realName", this.realName)
                    .add("server", this.server)
                    .add("serverDescription", this.serverDescription)
                    .add("secureConnection", this.secureConnection)
                    .add("operatorInformation", this.operatorInformation)
                    .add("idleTime", this.idleTime)
                    .add("signOnTime", this.signOnTime)
                    .add("awayMessage", this.awayMessage)
                    .toString();
        }
    }

    private final Client client;
    private String account;
    private final Set<String> channels = new HashSet<>();
    private final String nick;
    private String userString;
    private String host;
    private String realName;
    private String server;
    private String serverDescription;
    private boolean secure;
    private String operatorInformation;
    private Long idleTime;
    private Long signOnTime;
    private String awayMessage;

    WhoisBuilder(@Nonnull Client client, @Nonnull String nick) {
        this.client = client;
        this.nick = nick;
    }

    @Nonnull
    public String getNick() {
        return this.nick;
    }

    void setAccount(@Nonnull String account) {
        this.account = account;
    }

    void addChannels(@Nonnull String channels) {
        for (String channel : channels.split(" ")) {
            this.channels.add(channel);
        }
    }

    void setAway(@Nonnull String awayMessage) {
        this.awayMessage = awayMessage;
    }

    void setUserString(@Nonnull String userString) {
        this.userString = userString;
    }

    void setHost(@Nonnull String host) {
        this.host = host;
    }

    void setRealName(@Nonnull String realName) {
        this.realName = realName;
    }

    void setServer(@Nonnull String server) {
        this.server = server;
    }

    void setServerDescription(@Nonnull String serverDescription) {
        this.serverDescription = serverDescription;
    }

    void setSecure() {
        this.secure = true;
    }

    void setOperatorInformation(@Nonnull String operatorInformation) {
        this.operatorInformation = operatorInformation;
    }

    void setIdleTime(long idleTime) {
        this.idleTime = idleTime;
    }

    void setSignOnTime(long signOnTime) {
        this.signOnTime = signOnTime;
    }

    WhoisData build() {
        return new Whois(this.client, this.account, this.channels, this.nick, this.userString, this.host, this.realName, this.server, this.serverDescription, this.secure, this.operatorInformation, this.idleTime, this.signOnTime, this.awayMessage);
    }
}
