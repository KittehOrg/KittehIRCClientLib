package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class TestUser implements User {
    private final String nick;
    private final String user;
    private final String host;
    private final Optional<String> account;

    public TestUser(@Nonnull String nick, @Nonnull String user, @Nonnull String host, @Nullable String account) {
        this.nick = nick;
        this.user = user;
        this.host = host;
        this.account = Optional.ofNullable(account);
    }

    @Nonnull
    @Override
    public Optional<String> getAccount() {
        return this.account;
    }

    @Nonnull
    @Override
    public Optional<String> getAwayMessage() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Set<String> getChannels() {
        return Collections.emptySet();
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
    public Optional<String> getOperatorInformation() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<String> getRealName() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<String> getServer() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public String getUserString() {
        return this.user;
    }

    @Override
    public boolean isAway() {
        return false;
    }

    @Nonnull
    @Override
    public String getMessagingName() {
        return null;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.nick + '!' + this.user + '@' + this.host;
    }

    @Override
    public boolean isStale() {
        return false;
    }

    @Override
    public long getCreationTime() {
        return 0;
    }

    @Nonnull
    @Override
    public Client getClient() {
        return null;
    }

    @Nonnull
    @Override
    public String toString() {
        return this.getName() + (this.account != null ? " (account: " + this.account + '}' : "");
    }
}
