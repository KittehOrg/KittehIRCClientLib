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
package org.kitteh.irc.client.library.defaults.element;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.WhoisData;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Default implementation of {@link WhoisData}.
 */
public class DefaultWhoisData implements WhoisData {
    /**
     * A builder to assist in building {@link DefaultWhoisData}.
     */
    public static class Builder {
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

        /**
         * Constructs the builder.
         *
         * @param client client
         * @param nick nick of the user
         */
        public Builder(@NonNull Client client, @NonNull String nick) {
            this.client = client;
            this.nick = nick;
        }

        /**
         * Gets the user's nick.
         *
         * @return nick
         */
        @NonNull
        public String getNick() {
            return this.nick;
        }

        /**
         * Sets the account.
         *
         * @param account account
         */
        public void setAccount(@NonNull String account) {
            this.account = account;
        }

        /**
         * Adds channels, not erasing any previously added channels.
         *
         * @param channels channels to add
         */
        public void addChannels(@NonNull String channels) {
            Collections.addAll(this.channels, Sanity.nullCheck(channels, "Channels cannot be null").split(" "));
        }

        /**
         * Sets the away message.
         *
         * @param awayMessage away message
         */
        public void setAway(@NonNull String awayMessage) {
            this.awayMessage = awayMessage;
        }

        /**
         * Sets the user string.
         *
         * @param userString user string
         */
        public void setUserString(@NonNull String userString) {
            this.userString = userString;
        }

        /**
         * Sets the host.
         *
         * @param host host
         */
        public void setHost(@NonNull String host) {
            this.host = host;
        }

        /**
         * Sets the real name.
         *
         * @param realName real name
         */
        public void setRealName(@NonNull String realName) {
            this.realName = realName;
        }

        /**
         * Sets the server.
         *
         * @param server server
         */
        public void setServer(@NonNull String server) {
            this.server = server;
        }

        /**
         * Sets the server description.
         *
         * @param serverDescription server description
         */
        public void setServerDescription(@NonNull String serverDescription) {
            this.serverDescription = serverDescription;
        }

        /**
         * Sets that the connection is known to be secure
         */
        public void setSecure() {
            this.secure = true;
        }

        /**
         * Sets the operator information.
         *
         * @param operatorInformation operator information
         */
        public void setOperatorInformation(@NonNull String operatorInformation) {
            this.operatorInformation = operatorInformation;
        }

        /**
         * Sets the idle time.
         *
         * @param idleTime idle time
         */
        public void setIdleTime(long idleTime) {
            this.idleTime = idleTime;
        }

        /**
         * Sets the sign on time.
         *
         * @param signOnTime sign on time
         */
        public void setSignOnTime(long signOnTime) {
            this.signOnTime = signOnTime;
        }

        /**
         * Builds a new {@link WhoisData} from the provided information.
         *
         * @return new WhoisData
         */
        @NonNull
        public WhoisData build() {
            return new DefaultWhoisData(this.client, this.account, this.channels, this.nick, this.userString, this.host, this.realName, this.server, this.serverDescription, this.secure, this.operatorInformation, this.idleTime, this.signOnTime, this.awayMessage);
        }
    }

    private final Client client;
    private final String account;
    private final Set<String> channels;
    private final String name;
    private final String nick;
    private final String userString;
    private final String host;
    private final long creationTime;
    private final String realName;
    private final String server;
    private final Long idleTime;
    private final String serverDescription;
    private final boolean secureConnection;
    private final String operatorInformation;
    private final Long signOnTime;
    private final boolean away;
    private final String awayMessage;

    /**
     * Creates the default WHOIS data object.
     *
     * @param client client
     * @param account account, if known
     * @param channels channels the user known to be in
     * @param nick nickname
     * @param userString user string
     * @param host host
     * @param realName real name, if known
     * @param server server, if known
     * @param serverDescription server description, if known
     * @param secureConnection if the connection is known to be secure
     * @param operatorInformation any operator information, if known
     * @param idleTime how long the user has been idle, if known
     * @param signOnTime when the user signed on, if known
     * @param awayMessage user away message, if known
     */
    public DefaultWhoisData(@NonNull Client client, @Nullable String account, @NonNull Set<String> channels, @NonNull String nick, @NonNull String userString,
                            @NonNull String host, @Nullable String realName, @Nullable String server, @Nullable String serverDescription, boolean secureConnection,
                            @Nullable String operatorInformation, @Nullable Long idleTime, @Nullable Long signOnTime, @Nullable String awayMessage) {
        this.client = client;
        this.account = account;
        this.channels = Collections.unmodifiableSet(new HashSet<>(channels));
        this.name = nick + '!' + userString + '@' + host;
        this.nick = nick;
        this.userString = userString;
        this.host = host;
        this.realName = realName;
        this.server = server;
        this.serverDescription = serverDescription;
        this.operatorInformation = operatorInformation;
        this.secureConnection = secureConnection;
        this.idleTime = idleTime;
        this.signOnTime = signOnTime;
        this.away = awayMessage != null;
        this.awayMessage = awayMessage;
        this.creationTime = System.currentTimeMillis();
    }

    @NonNull
    @Override
    public Optional<String> getAccount() {
        return Optional.ofNullable(this.account);
    }

    @NonNull
    @Override
    public Optional<String> getAwayMessage() {
        return Optional.ofNullable(this.awayMessage);
    }

    @NonNull
    @Override
    public Set<String> getChannels() {
        return this.channels;
    }

    @NonNull
    @Override
    public String getHost() {
        return this.host;
    }

    @NonNull
    @Override
    public String getNick() {
        return this.nick;
    }

    @NonNull
    @Override
    public Optional<String> getRealName() {
        return Optional.ofNullable(this.realName);
    }

    @NonNull
    @Override
    public Optional<String> getServer() {
        return Optional.ofNullable(this.server);
    }

    @NonNull
    @Override
    public String getUserString() {
        return this.userString;
    }

    @Override
    public boolean isAway() {
        return this.away;
    }

    @NonNull
    @Override
    public String getMessagingName() {
        return this.nick;
    }

    @NonNull
    @Override
    public String getName() {
        return this.name;
    }

    @NonNull
    @Override
    public Client getClient() {
        return this.client;
    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @NonNull
    @Override
    public Optional<Long> getIdleTime() {
        return Optional.ofNullable(this.idleTime);
    }

    @NonNull
    @Override
    public Optional<String> getOperatorInformation() {
        return Optional.ofNullable(this.operatorInformation);
    }

    @NonNull
    @Override
    public Optional<String> getServerDescription() {
        return Optional.ofNullable(this.serverDescription);
    }

    @NonNull
    @Override
    public Optional<Long> getSignOnTime() {
        return Optional.ofNullable(this.signOnTime);
    }

    @Override
    public boolean isSecureConnection() {
        return this.secureConnection;
    }

    @NonNull
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
