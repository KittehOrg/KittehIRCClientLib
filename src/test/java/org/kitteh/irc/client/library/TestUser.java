package org.kitteh.irc.client.library;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.element.User;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class TestUser implements User {
    private final String nick;
    private final String user;
    private final String host;
    private final Optional<String> account;

    public TestUser(@NonNull String nick, @NonNull String user, @NonNull String host, @Nullable String account) {
        this.nick = nick;
        this.user = user;
        this.host = host;
        this.account = Optional.ofNullable(account);
    }

    @Override
    public @NonNull Optional<String> getAccount() {
        return this.account;
    }

    @Override
    public @NonNull Optional<String> getAwayMessage() {
        return Optional.empty();
    }

    @Override
    public @NonNull Set<String> getChannels() {
        return Collections.emptySet();
    }

    @Override
    public @NonNull String getHost() {
        return this.host;
    }

    @Override
    public @NonNull String getNick() {
        return this.nick;
    }

    @Override
    public @NonNull Optional<String> getOperatorInformation() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<String> getRealName() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<String> getServer() {
        return Optional.empty();
    }

    @Override
    public @NonNull String getUserString() {
        return this.user;
    }

    @Override
    public boolean isAway() {
        return false;
    }

    @Override
    public @NonNull String getMessagingName() {
        return null;
    }

    @Override
    public @NonNull String getName() {
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

    @Override
    public @NonNull Client getClient() {
        return null;
    }

    @Override
    public @NonNull String toString() {
        return this.getName() + (this.account.map(s -> " (account: " + s + '}').orElse(""));
    }
}
