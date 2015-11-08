package org.kitteh.irc.client.library.event.abstractbase;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.helper.Change;
import org.kitteh.irc.client.library.event.helper.UserInfoChangeEvent;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

/**
 * Abstract base class for user info changing.
 *
 * @param <Type> type of change
 * @see UserInfoChangeEvent
 */
public class UserInfoChangeEventBase<Type> extends ActorEventBase<User> implements UserInfoChangeEvent<Type> {
    private final User newUser;
    private final Change<Type> change;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param originalMessages original messages
     * @param oldUser the actor
     * @param newUser the new actor
     * @param changedInfoGetter getter for the changed info
     */
    protected UserInfoChangeEventBase(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull User oldUser, @Nonnull User newUser, @Nonnull Function<User, Type> changedInfoGetter) {
        super(client, originalMessages, oldUser);
        this.newUser = Sanity.nullCheck(newUser, "New user cannot be null");
        this.change = new Change<>(changedInfoGetter.apply(oldUser), changedInfoGetter.apply(newUser));
    }

    @Nonnull
    @Override
    public User getOldUser() {
        return this.getActor();
    }

    @Nonnull
    @Override
    public User getNewUser() {
        return this.newUser;
    }

    @Nonnull
    @Override
    public Change<Type> getChange() {
        return this.change;
    }
}
