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
package org.kitteh.irc.client.library;

import org.kitteh.irc.client.library.auth.AuthManager;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.MessageReceiver;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.client.ClientConnectedEvent;
import org.kitteh.irc.client.library.event.user.PrivateCTCPQueryEvent;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * An individual IRC connection, see {@link #builder()} to create one.
 */
public interface Client {
    /**
     * Creates a {@link ClientBuilder} to build clients.
     *
     * @return a client builder
     */
    @Nonnull
    static ClientBuilder builder() {
        try {
            Constructor<?> constructor = Class.forName(Client.class.getPackage().getName() + ".implementation.IRCClientBuilder").getDeclaredConstructor();
            constructor.setAccessible(true);
            return (ClientBuilder) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Kitteh IRC Client Library cannot create a ClientBuilder.", e);
        }
    }

    /**
     * Adds channels to this client.
     * <p>
     * Joins the channel if already connected.
     *
     * @param channels channel(s) to add
     * @throws IllegalArgumentException if null
     */
    void addChannel(@Nonnull String... channels);

    /**
     * Adds channels to this client.
     * <p>
     * Joins the channel if already connected.
     *
     * @param channels channel(s) to add
     * @throws IllegalArgumentException if null
     */
    default void addChannel(@Nonnull Channel... channels) {
        Sanity.nullCheck(channels, "Channels cannot be null");
        Sanity.truthiness(channels.length > 0, "Channels cannot be empty array");
        this.addChannel(Arrays.stream(channels).map(Channel::getName).toArray(String[]::new));
    }

    /**
     * Gets the authentication manager.
     *
     * @return auth manager
     */
    @Nonnull
    AuthManager getAuthManager();

    /**
     * Gets the capability manager.
     *
     * @return the capability manager
     */
    @Nonnull
    CapabilityManager getCapabilityManager();

    /**
     * Gets the named channel.
     *
     * @param name channel name
     * @return a channel snapshot of the named channel if tracked by the
     * client
     * @throws IllegalArgumentException if name is null
     * @see #getChannels()
     */
    @Nonnull
    Optional<Channel> getChannel(@Nonnull String name);

    /**
     * Gets the channels in which the client is currently present.
     *
     * @return the client's current channels
     */
    @Nonnull
    Set<Channel> getChannels();

    /**
     * Gets the client's event manager.
     *
     * @return the event manager for this client
     */
    @Nonnull
    EventManager getEventManager();

    /**
     * Gets the nickname the client intends to possess. May not reflect
     * the current nickname if it's taken. The client will automatically
     * attempt to take back this nickname.
     * <p>
     * Use {@link #getNick()} for the current nick.
     *
     * @return the nickname the client tries to maintain
     */
    @Nonnull
    String getIntendedNick();

    /**
     * Gets the delay between messages sent to the server.
     * <p>
     * Default is 1200ms.
     *
     * @return milliseconds between sent messages
     */
    int getMessageDelay();

    /**
     * Gets the message tag manager.
     *
     * @return message tag manager
     */
    @Nonnull
    MessageTagManager getMessageTagManager();

    /**
     * Gets the client name. This name is just an internal name for reference
     * and is not visible from IRC.
     *
     * @return the client name
     */
    @Nonnull
    String getName();

    /**
     * Gets the current nickname the client has.
     *
     * @return the current nick
     */
    @Nonnull
    String getNick();

    /**
     * Gets information about the server to which the client is currently
     * connected. As long as the client remains connected the information
     * returned by this object will update according to information received
     * from the server. A new one can be acquired from {@link
     * ClientConnectedEvent}
     *
     * @return the server information object
     */
    @Nonnull
    ServerInfo getServerInfo();

    /**
     * Gets the User that the client is represented by. Will return {@link
     * Optional#EMPTY} until the Client is in a Channel for which joining has
     * completed, meaning full user info is available.
     *
     * @return the user of this client if known
     */
    @Nonnull
    Optional<User> getUser();

    /**
     * Checks to see if this client is the same as the given user.
     *
     * @param user user to check this client against
     * @return true if this client is the user, false if not
     * @throws IllegalArgumentException if user null or from different Client
     */
    default boolean isUser(@Nonnull User user) {
        Sanity.nullCheck(user, "User cannot be null");
        Sanity.truthiness(user.getClient() == this, "User cannot come from a different Client");
        return user.equals(this.getUser().orElse(null));
    }

    /**
     * Removes a channel from the client, leaving as necessary.
     *
     * @param channel channel to leave
     * @throws IllegalArgumentException if arguments are null
     */
    void removeChannel(@Nonnull String channel);

    /**
     * Removes a channel from the client, leaving as necessary.
     *
     * @param channel channel to leave
     * @throws IllegalArgumentException if arguments are null
     */
    void removeChannel(@Nonnull Channel channel);

    /**
     * Removes a channel from the client, leaving as necessary.
     *
     * @param channel channel to leave
     * @param reason part reason
     * @throws IllegalArgumentException if arguments are null
     */
    void removeChannel(@Nonnull String channel, @Nonnull String reason);

    /**
     * Removes a channel from the client, leaving as necessary.
     *
     * @param channel channel to leave
     * @param reason part reason
     * @throws IllegalArgumentException if arguments are null
     */
    void removeChannel(@Nonnull Channel channel, @Nonnull String reason);

    /**
     * Removes the exception listener.
     */
    void removeExceptionListener();

    /**
     * Removes the input listener.
     */
    void removeInputListener();

    /**
     * Removes the output listener.
     */
    void removeOutputListener();

    /**
     * Sends a CTCP message to a target user or channel. Automagically adds
     * the CTCP delimiter around the message and escapes the characters that
     * need escaping when sending a CTCP message.
     * <p>
     * <i>Note: CTCP replies should not be sent this way. Catch the message
     * with the {@link PrivateCTCPQueryEvent}</i>
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    void sendCTCPMessage(@Nonnull String target, @Nonnull String message);

    /**
     * Sends a CTCP message to a target user or channel. Automagically adds
     * the CTCP delimiter around the message and escapes the characters that
     * need escaping when sending a CTCP message.
     * <p>
     * <i>Note: CTCP replies should not be sent this way. Catch the message
     * with the {@link PrivateCTCPQueryEvent}</i>
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    default void sendCTCPMessage(@Nonnull MessageReceiver target, @Nonnull String message) {
        Sanity.nullCheck(target, "Target cannot be null");
        this.sendCTCPMessage(target.getMessagingName(), message);
    }

    /**
     * Sends a message to a target user or channel.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    void sendMessage(@Nonnull String target, @Nonnull String message);

    /**
     * Sends a message to a target user or channel.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    default void sendMessage(@Nonnull MessageReceiver target, @Nonnull String message) {
        Sanity.nullCheck(target, "Target cannot be null");
        this.sendMessage(target.getMessagingName(), message);
    }

    /**
     * Sends a notice to a target user or channel.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    void sendNotice(@Nonnull String target, @Nonnull String message);

    /**
     * Sends a notice to a target user or channel.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    default void sendNotice(@Nonnull MessageReceiver target, @Nonnull String message) {
        Sanity.nullCheck(target, "Target cannot be null");
        this.sendNotice(target.getMessagingName(), message);
    }

    /**
     * Sends a raw IRC message.
     *
     * @param message message to send
     * @throws IllegalArgumentException if message is null
     */
    void sendRawLine(@Nonnull String message);

    /**
     * Sends a raw IRC message, unless the exact same message is already in
     * the queue of messages not yet sent.
     *
     * @param message message to send
     * @throws IllegalArgumentException if message is null
     */
    void sendRawLineAvoidingDuplication(@Nonnull String message);

    /**
     * Sends a raw IRC message, disregarding message delays and all sanity.
     * Live life on the wild side with this method designed to ensure you
     * get flood-kicked before you finish dumping your life's work into chat.
     *
     * @param message message to send dangerously, you monster
     * @throws IllegalArgumentException if message is null
     */
    void sendRawLineImmediately(@Nonnull String message);

    /**
     * Sets a listener for all thrown exceptions on this client.
     * <p>
     * All exceptions are passed from a single, separate thread.
     *
     * @param listener catcher of throwable objects
     */
    void setExceptionListener(@Nonnull Consumer<Exception> listener);

    /**
     * Sets a listener for all incoming messages from the server.
     * <p>
     * All messages are passed from a single, separate thread.
     *
     * @param listener input listener
     */
    void setInputListener(@Nonnull Consumer<String> listener);

    /**
     * Sets the delay between messages sent to the server.
     * <p>
     * Default is 1200ms.
     *
     * @param delay milliseconds between sent messages
     */
    void setMessageDelay(int delay);

    /**
     * Sets the nick the client wishes to use.
     *
     * @param nick new nickname
     * @throws IllegalArgumentException if nick is null
     */
    void setNick(@Nonnull String nick);

    /**
     * Sets a listener for all outgoing messages to the server.
     * <p>
     * All messages are passed from a single, separate thread.
     *
     * @param listener output listener
     */
    void setOutputListener(@Nonnull Consumer<String> listener);

    /**
     * Shuts down the client without a quit message.
     */
    void shutdown();

    /**
     * Shuts down the client.
     *
     * @param reason quit message to send
     */
    void shutdown(@Nonnull String reason);
}
