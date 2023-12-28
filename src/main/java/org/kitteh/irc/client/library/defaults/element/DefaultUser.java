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
package org.kitteh.irc.client.library.defaults.element;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Default implementation of {@link User}.
 */
public class DefaultUser extends DefaultStaleable implements User {
    private final String account;
    private final String awayMessage;
    private final Set<String> channels;
    private final String host;
    private final boolean isAway;
    private final String nick;
    private final String operString;
    private final String realName;
    private final String server;
    private final String user;

    /**
     * Constructs the object.
     *
     * @param client client from which this came
     * @param name full mask name
     * @param account account, if known
     * @param awayMessage away message, if known
     * @param nick nick
     * @param user user string (ident)
     * @param host host
     * @param isAway true if away
     * @param operString oper info, if known
     * @param realName real name, if known
     * @param server server info, if known
     * @param channels channels they are in
     */
    public DefaultUser(Client.@NonNull WithManagement client, @NonNull String name, @Nullable String account, @Nullable String awayMessage,
                       @NonNull String nick, @NonNull String user, @NonNull String host, boolean isAway,
                       @Nullable String operString, @Nullable String realName, @Nullable String server, @NonNull Set<String> channels) {
        super(client, name);
        this.account = account;
        this.awayMessage = awayMessage;
        this.nick = nick;
        this.user = user;
        this.host = host;
        this.isAway = isAway;
        this.operString = operString;
        this.realName = realName;
        this.server = server;
        this.channels = Collections.unmodifiableSet(channels);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DefaultUser) && (((DefaultUser) o).getClient() == this.getClient()) && ((DefaultUser) o).getLowerCaseName().equals(this.getLowerCaseName());
    }

    @Override
    public @NonNull Optional<String> getAccount() {
        return Optional.ofNullable(this.account);
    }

    @Override
    public @NonNull Optional<String> getAwayMessage() {
        return Optional.ofNullable(this.awayMessage);
    }

    @Override
    public @NonNull Set<String> getChannels() {
        return this.channels;
    }

    @Override
    public @NonNull String getHost() {
        return this.host;
    }

    @Override
    public @NonNull String getMessagingName() {
        return this.getNick();
    }

    @Override
    public @NonNull String getNick() {
        return this.nick;
    }

    @Override
    public @NonNull Optional<String> getOperatorInformation() {
        return Optional.ofNullable(this.operString);
    }

    @Override
    public @NonNull Optional<String> getRealName() {
        return Optional.ofNullable(this.realName);
    }

    @Override
    public @NonNull Optional<String> getServer() {
        return Optional.ofNullable(this.server);
    }

    @Override
    public @NonNull String getUserString() {
        return this.user;
    }

    @Override
    public int hashCode() {
        return (this.getLowerCaseName().hashCode() * 2) + this.getClient().hashCode();
    }

    @Override
    public boolean isAway() {
        return this.isAway;
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).add("client", this.getClient()).add("nick", this.nick).add("user", this.user).add("host", this.host).add("channels", this.channels.size()).toString();
    }
}
