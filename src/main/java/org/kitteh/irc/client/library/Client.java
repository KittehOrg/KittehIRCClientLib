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
package org.kitteh.irc.client.library;

import org.kitteh.irc.client.library.command.AwayCommand;
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.command.ChannelModeCommand;
import org.kitteh.irc.client.library.command.Command;
import org.kitteh.irc.client.library.command.KickCommand;
import org.kitteh.irc.client.library.command.MonitorCommand;
import org.kitteh.irc.client.library.command.OperCommand;
import org.kitteh.irc.client.library.command.TopicCommand;
import org.kitteh.irc.client.library.command.WallopsCommand;
import org.kitteh.irc.client.library.command.WhoisCommand;
import org.kitteh.irc.client.library.defaults.DefaultBuilder;
import org.kitteh.irc.client.library.defaults.feature.DefaultActorTracker;
import org.kitteh.irc.client.library.defaults.feature.DefaultAuthManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultCapabilityManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultEventManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultISupportManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultMessageTagManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultServerInfo;
import org.kitteh.irc.client.library.defaults.listener.DefaultListeners;
import org.kitteh.irc.client.library.feature.EventListenerSupplier;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.MessageReceiver;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.event.channel.RequestedChannelJoinCompleteEvent;
import org.kitteh.irc.client.library.event.client.ClientNegotiationCompleteEvent;
import org.kitteh.irc.client.library.event.helper.UnexpectedChannelLeaveEvent;
import org.kitteh.irc.client.library.event.user.PrivateCtcpQueryEvent;
import org.kitteh.irc.client.library.feature.ActorTracker;
import org.kitteh.irc.client.library.feature.AuthManager;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.feature.ISupportManager;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageMap;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageType;
import org.kitteh.irc.client.library.feature.sending.MessageSendingQueue;
import org.kitteh.irc.client.library.feature.sending.SingleDelaySender;
import org.kitteh.irc.client.library.feature.sts.StsMachine;
import org.kitteh.irc.client.library.feature.sts.StsStorageManager;
import org.kitteh.irc.client.library.util.Cutter;
import org.kitteh.irc.client.library.util.Listener;
import org.kitteh.irc.client.library.util.Pair;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.TrustManagerFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An individual IRC connection, see {@link #builder()} to create one.
 */
public interface Client {
    /**
     * Builds {@link Client}s. Create a builder with {@link Client#builder()}.
     * <p>
     * The default built client connects securely via port 6697. See {@link
     * #secure(boolean)} to disable, or the other secure-prefixed methods in this
     * builder to fully utilize the feature. Note that the default
     * TrustManagerFactory relies on your local trust store. The default Oracle
     * trust store does not accept self-signed certificates.
     */
    interface Builder {
        /**
         * Sets the supplier of the actor tracker.
         * <p>
         * By default, the {@link DefaultActorTracker} is used.
         *
         * @param supplier supplier
         * @return this builder
         * @see DefaultActorTracker
         */
        @Nonnull
        Builder actorTracker(@Nonnull Function<Client.WithManagement, ? extends ActorTracker> supplier);

        /**
         * Sets the supplier of the authentication manager.
         * <p>
         * By default, the {@link DefaultAuthManager} is used.
         *
         * @param supplier supplier
         * @return this builder
         * @see DefaultAuthManager
         */
        @Nonnull
        Builder authManager(@Nonnull Function<Client.WithManagement, ? extends AuthManager> supplier);

        /**
         * Binds the client to a host or IP locally.
         * <p>
         * By default, the host is not set which results in wildcard binding.
         *
         * @param host host to bind to, or null for wildcard binding
         * @return this builder
         */
        @Nonnull
        Builder bindHost(@Nullable String host);

        /**
         * Binds the client to the specified port. Invalid ports are set to 0.
         * <p>
         * By default, the port is 0.
         *
         * @param port port to bind to
         * @return this builder
         */
        @Nonnull
        Builder bindPort(int port);

        /**
         * Sets the supplier of the capability manager.
         * <p>
         * By default, the {@link DefaultCapabilityManager} is used.
         *
         * @param supplier supplier
         * @return this builder
         * @see DefaultCapabilityManager
         */
        @Nonnull
        Builder capabilityManager(@Nonnull Function<Client.WithManagement, ? extends CapabilityManager.WithManagement> supplier);

        /**
         * Sets default messages.
         *
         * @param defaultMessageMap default values for messages
         * @return this builder
         * @see DefaultMessageMap
         */
        @Nonnull
        Builder defaultMessageMap(@Nonnull DefaultMessageMap defaultMessageMap);

        /**
         * Sets the suppliers of event listeners to be registered by the
         * event manager upon construction.
         * <p>
         * By default, a list of {@link DefaultListeners} values is used.
         * @param listenerSuppliers
         * @return
         */
        @Nonnull
        Builder eventListeners(@Nonnull List<EventListenerSupplier> listenerSuppliers);

        /**
         * Sets the supplier of the event manager.
         * <p>
         * By default, the {@link DefaultEventManager} is used.
         *
         * @param supplier supplier
         * @return this builder
         * @see DefaultEventManager
         */
        @Nonnull
        Builder eventManager(@Nonnull Function<Client.WithManagement, ? extends EventManager> supplier);

        /**
         * Sets a listener for all thrown exceptions on this client. By default,
         * a consumer exists which calls Throwable#printStackTrace() on all
         * received exceptions.
         * <p>
         * All exceptions are passed from a single, separate thread.
         *
         * @param listener catcher of throwable objects or null to not listen
         * @return this builder
         */
        @Nonnull
        Builder exceptionListener(@Nullable Consumer<Exception> listener);

        /**
         * Sets a listener for all incoming messages from the server.
         * <p>
         * All messages are passed from a single, separate thread.
         *
         * @param listener input listener or null to not listen
         * @return this builder
         */
        @Nonnull
        Builder inputListener(@Nullable Consumer<String> listener);

        /**
         * Sets the supplier of the ISUPPORT manager.
         * <p>
         * By default, the {@link DefaultISupportManager} is used.
         *
         * @param supplier supplier
         * @return this builder
         * @see DefaultEventManager
         */
        @Nonnull
        Builder iSupportManager(@Nonnull Function<Client.WithManagement, ? extends ISupportManager> supplier);

        /**
         * Sets the supplier of message sending queues, which dictate the
         * rate at which messages are sent by the Client to the server.
         * <p>
         * By default, the {@link SingleDelaySender} is used with a delay set
         * to {@link SingleDelaySender#DEFAULT_MESSAGE_DELAY}.
         *
         * @param supplier supplier
         * @return this builder
         * @see MessageSendingQueue
         */
        @Nonnull
        Builder messageSendingQueueSupplier(@Nonnull Function<Client.WithManagement, ? extends MessageSendingQueue> supplier);

        /**
         * Sets the supplier of the message tag manager.
         * <p>
         * By default, the {@link DefaultMessageTagManager} is used.
         *
         * @param supplier supplier
         * @return this builder
         * @see MessageTagManager
         */
        @Nonnull
        Builder messageTagManager(@Nonnull Function<Client.WithManagement, ? extends MessageTagManager> supplier);

        /**
         * Names the client, for internal labeling.
         *
         * @param name a name to label the client internally
         * @return this builder
         * @throws IllegalArgumentException if name is null
         */
        @Nonnull
        Builder name(@Nonnull String name);

        /**
         * Sets the client's nick.
         * <p>
         * By default, the nick is Kitteh.
         *
         * @param nick nick for the client to use
         * @return this builder
         * @throws IllegalArgumentException if nick is null
         */
        @Nonnull
        Builder nick(@Nonnull String nick);

        /**
         * Sets a listener for all outgoing messages to the server.
         * <p>
         * All messages are passed from a single, separate thread.
         *
         * @param listener output listener or null to not listen
         * @return this builder
         */
        @Nonnull
        Builder outputListener(@Nullable Consumer<String> listener);

        /**
         * Sets the realname the client uses.
         * <p>
         * By default, the realname is Kitteh.
         *
         * @param name realname to use
         * @return this builder
         * @throws IllegalArgumentException for null realname
         */
        @Nonnull
        Builder realName(@Nonnull String name);

        /**
         * Sets the server password.
         * <p>
         * If not set, no password is sent
         *
         * @param password server password or null to not send one
         * @return this builder
         */
        @Nonnull
        Builder serverPassword(@Nullable String password);

        /**
         * Sets whether the client connects via TLS/SSL.
         * <p>
         * Note that by default the TrustManager used does not accept the
         * certificates of many popular networks. You must use {@link
         * #secureTrustManagerFactory(TrustManagerFactory)} to set your own
         * TrustManagerFactory.
         *
         * @param secure true for TLS/SSL
         * @return this builder
         */
        @Nonnull
        Builder secure(boolean secure);

        /**
         * Sets the key for SSL connection.
         *
         * @param keyCertChainFile X.509 certificate chain file in PEM format
         * @return this builder
         * @see #secure(boolean)
         */
        @Nonnull
        Builder secureKeyCertChain(@Nullable Path keyCertChainFile);

        /**
         * Sets the private key for SSL connection.
         *
         * @param keyFile PKCS#8 private key file in PEM format
         * @return this builder
         * @see #secure(boolean)
         */
        @Nonnull
        Builder secureKey(@Nullable Path keyFile);

        /**
         * Sets the private key password for SSL connection.
         *
         * @param password password for private key
         * @return this builder
         * @see #secure(boolean)
         */
        @Nonnull
        Builder secureKeyPassword(@Nullable String password);

        /**
         * Sets the {@link TrustManagerFactory} for SSL connection.
         *
         * @param factory trust manager supplier
         * @return this builder
         * @see #secure(boolean)
         */
        @Nonnull
        Builder secureTrustManagerFactory(@Nullable TrustManagerFactory factory);

        /**
         * Sets the server host to which the client will connect.
         * <p>
         * By default, the host is localhost.
         *
         * @param host IRC server host
         * @return this builder
         * @throws IllegalArgumentException for null host
         */
        @Nonnull
        Builder serverHost(@Nonnull String host);

        /**
         * Sets the server port to which the client will connect.
         * <p>
         * By default, the port is 6667.
         *
         * @param port IRC server port
         * @return this builder
         */
        @Nonnull
        Builder serverPort(int port);

        /**
         * Sets the supplier of the server info.
         * <p>
         * By default, the {@link DefaultServerInfo} is used.
         *
         * @param supplier supplier
         * @return this builder
         * @see DefaultServerInfo
         */
        @Nonnull
        Builder serverInfo(@Nonnull Function<Client.WithManagement, ? extends ServerInfo.WithManagement> supplier);

        /**
         * Sets the user the client connects as.
         * <p>
         * By default, the user is Kitteh.
         *
         * @param user user to connect as
         * @return this builder
         * @throws IllegalArgumentException for null user
         */
        @Nonnull
        Builder user(@Nonnull String user);

        /**
         * Sets all the information for, and enables, WebIRC.
         * <p>
         * By default, WebIRC is disabled.
         *
         * @param password password as defined in the IRCd config
         * @param user username part of the client's address
         * @param host hostname part of the client's address
         * @param ip client's IP address
         * @return this builder
         * @throws IllegalArgumentException for any null parameters
         * @see #webircRemove()
         */
        @Nonnull
        Builder webirc(@Nonnull String password, @Nonnull String user, @Nonnull String host, @Nonnull InetAddress ip);

        /**
         * Removes WEBIRC settings from this builder.
         *
         * @return this builder
         * @see #webirc(String, String, String, InetAddress)
         */
        @Nonnull
        Builder webircRemove();

        /**
         * Sets the storage manager for STS (strict transport security) support.
         * <p>
         * By default, this is null and thus STS support is disabled. If you elect to
         * enable STS, you are not permitted to use an insecure trust manager factory.
         *
         * @param storageManager storage system to persist STS information per host
         * @return this builder
         */
        @Nonnull
        Builder stsStorageManager(@Nullable StsStorageManager storageManager);

        /**
         * Clientmaker, clientmaker, make me a client!
         *
         * @return a client designed to your liking
         */
        @Nonnull
        Client build();

        /**
         * Clientmaker, clientmaker, make me a client, build me the client,
         * begin connection!
         *
         * @return a client designed to your liking
         */
        @Nonnull
        Client buildAndConnect();
    }

    /**
     * Provides commands.
     */
    interface Commands {
        /**
         * Provides a new AWAY command.
         *
         * @return new away command
         */
        @Nonnull
        AwayCommand away();

        /**
         * Provides a new CAP REQ command.
         *
         * @return new capability request command
         */
        @Nonnull
        CapabilityRequestCommand capabilityRequest();

        /**
         * Provides a new channel MODE command.
         *
         * @param channel channel in which the mode is being changed
         * @return new mode command
         */
        @Nonnull
        ChannelModeCommand mode(@Nonnull Channel channel);

        /**
         * Provides a new KICK command.
         *
         * @param channel channel in which the kick is happening
         * @return new kick command
         */
        @Nonnull
        KickCommand kick(@Nonnull Channel channel);

        /**
         * Provides a new MONITOR command.
         *
         * @return new monitor command
         */
        @Nonnull
        MonitorCommand monitor();

        /**
         * Provides a new OPER command.
         *
         * @return new oper command
         */
        @Nonnull
        OperCommand oper();

        /**
         * Provides a new TOPIC command.
         *
         * @param channel channel in which the topic is being changed
         * @return new topic command
         */
        @Nonnull
        TopicCommand topic(@Nonnull Channel channel);

        /**
         * Provides a new WALLOPS command.
         *
         * @return new wallops command
         */
        @Nonnull
        WallopsCommand wallops();

        /**
         * Provides a new WHOIS command.
         *
         * @return new whois command
         */
        @Nonnull
        WhoisCommand whois();
    }

    /**
     * A Client with management features.
     */
    interface WithManagement extends Client {
        /**
         * Starts the sending of queued 'immediately' messages.
         *
         * @param consumer consumer with which to handle this queue
         */
        void beginMessageSendingImmediate(@Nonnull Consumer<String> consumer);

        /**
         * Gets the actor tracker.
         *
         * @return actor tracker
         */
        @Nonnull
        ActorTracker getActorTracker();

        /**
         * Gets the bind address
         *
         * @return bind address
         */
        @Nonnull
        InetSocketAddress getBindAddress();

        @Nonnull
        @Override
        CapabilityManager.WithManagement getCapabilityManager();

        /**
         * Gets the currently set input listener.
         *
         * @return input listener
         */
        @Nonnull
        Listener<String> getInputListener();

        /**
         * Gets the channels the client intends to join.
         *
         * @return intended channels
         */
        @Nonnull
        Set<String> getIntendedChannels();

        /**
         * Gets the currently set output listener.
         *
         * @return output listener
         */
        @Nonnull
        Listener<String> getOutputListener();

        /**
         * Gets the nickname the client has last requested.
         *
         * @return requested nick
         */
        @Nonnull
        String getRequestedNick();

        /**
         * Gets the TLS/SSL key.
         *
         * @return key
         */
        @Nullable
        Path getSecureKey();

        /**
         * Gets the TLS/SSL key certificate chain.
         *
         * @return key cert chain
         */
        @Nullable
        Path getSecureKeyCertChain();

        /**
         * Gets the TLS/SSL key password.
         *
         * @return password
         */
        @Nullable
        String getSecureKeyPassword();

        /**
         * Gets the trust manager factory.
         *
         * @return trust manager factory
         */
        @Nullable
        TrustManagerFactory getSecureTrustManagerFactory();

        /**
         * Gets the server address
         *
         * @return server address
         */
        @Nonnull
        InetSocketAddress getServerAddress();

        @Override
        @Nonnull
        ServerInfo.WithManagement getServerInfo();

        /**
         * Pauses message sending, waiting for next successful connection.
         */
        void pauseMessageSending();

        /**
         * Sends a PING.
         */
        void ping();

        /**
         * Processes a line from the IRC server.
         *
         * @param line line to process
         */
        void processLine(@Nonnull String line);

        /**
         * Sends a nick change request.
         *
         * @param newNick new nickname
         */
        void sendNickChange(@Nonnull String newNick);

        /**
         * Sets the current nickname the client knows it has.
         *
         * @param nick nickname
         */
        void setCurrentNick(@Nonnull String nick);

        /**
         * Sets the server address.
         *
         * @param address server address
         */
        void setServerAddress(@Nonnull InetSocketAddress address);

        /**
         * Initialize with pre-connection information.
         *
         * @param name name
         * @param serverAddress serverAddress
         * @param serverPassword serverPassword
         * @param bindAddress bindAddress
         * @param nick nick
         * @param userString userString
         * @param realName realName
         * @param actorTracker actorTracker
         * @param authManager authManager
         * @param capabilityManager capabilityManager
         * @param eventManager eventManager
         * @param messageTagManager messageTagManager
         * @param iSupportManager iSupportManager
         * @param defaultMessageMap defaultMessageMap
         * @param messageSendingQueue messageSendingQueue
         * @param serverInfo serverInfo
         * @param exceptionListener exceptionListener
         * @param inputListener inputListener
         * @param outputListener outputListener
         * @param secure secure
         * @param secureKeyCertChain secureKeyCertChain
         * @param secureKey secureKey
         * @param secureKeyPassword secureKeyPassword
         * @param trustManagerFactory trustManagerFactory
         * @param stsStorageManager stsStorageManager
         * @param webircHost webircHost
         * @param webircIP webircIP
         * @param webircPassword webircPassword
         * @param webircUser webircUser
         */
        void initialize(@Nonnull String name, @Nonnull InetSocketAddress serverAddress, @Nullable String serverPassword,
                        @Nullable InetSocketAddress bindAddress,
                        @Nonnull String nick, @Nonnull String userString, @Nonnull String realName, @Nonnull ActorTracker actorTracker,
                        @Nonnull AuthManager authManager, @Nonnull CapabilityManager.WithManagement capabilityManager,
                        @Nonnull EventManager eventManager, @Nonnull List<EventListenerSupplier> listenerSuppliers,
                        @Nonnull MessageTagManager messageTagManager,
                        @Nonnull ISupportManager iSupportManager, @Nullable DefaultMessageMap defaultMessageMap,
                        @Nonnull Function<Client.WithManagement, ? extends MessageSendingQueue> messageSendingQueue,
                        @Nonnull Function<Client.WithManagement, ? extends ServerInfo.WithManagement> serverInfo,
                        @Nullable Consumer<Exception> exceptionListener, @Nullable Consumer<String> inputListener,
                        @Nullable Consumer<String> outputListener, boolean secure, @Nullable Path secureKeyCertChain,
                        @Nullable Path secureKey, @Nullable String secureKeyPassword, @Nullable TrustManagerFactory trustManagerFactory,
                        @Nullable StsStorageManager stsStorageManager, @Nullable String webircHost,
                        @Nullable InetAddress webircIP, @Nullable String webircPassword, @Nullable String webircUser);

        /**
         * Sets the client's user modes.
         *
         * @param userModes user modes to set
         */
        void setUserModes(@Nonnull ModeStatusList<UserMode> userModes);

        /**
         * Starts sending queued messages.
         */
        void startSending();

        /**
         * Updates the client's user modes.
         *
         * @param userModes mode changes
         */
        void updateUserModes(@Nonnull ModeStatusList<UserMode> userModes);

        /**
         * Gets if the client is configured for a secure connection.
         *
         * @return true if configured for secure
         */
        boolean isSecureConnection();
    }

    /**
     * Creates a {@link Builder} to build clients.
     *
     * @return a client builder
     */
    @Nonnull
    static Builder builder() {
        return new DefaultBuilder();
    }

    /**
     * Adds channels to this client.
     * <p>
     * Joins the channels if already connected.
     *
     * @param channels channel(s) to add
     * @throws IllegalArgumentException if null or invalid
     * @see RequestedChannelJoinCompleteEvent
     * @see UnexpectedChannelLeaveEvent
     */
    void addChannel(@Nonnull String... channels);

    /**
     * Adds a key-protected channel to this client.
     * <p>
     * Joins the channels if already connected.
     *
     * @param channel channel to add
     * @param key channel key
     * @throws IllegalArgumentException if null or invalid
     */
    void addKeyProtectedChannel(@Nonnull String channel, @Nonnull String key);

    /**
     * Adds key-protected channels to this client.
     * <p>
     * Joins the channels if already connected.
     *
     * @param channelsAndKeys pairs of channel, key
     * @throws IllegalArgumentException if null or invalid
     */
    void addKeyProtectedChannel(@Nonnull Pair<String, String>... channelsAndKeys);

    /**
     * Provides access to {@link Command}s.
     *
     * @return commands
     */
    @Nonnull
    Commands commands();

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
     * Gets the channels on the given collection in which the client is
     * currently present.
     *
     * @param channels collection of channel names to get
     * @return the client's current channels that are named in the collection
     */
    @Nonnull
    Set<Channel> getChannels(@Nonnull Collection<String> channels);

    /**
     * Gets the message manager for default messages to reply with
     * when certain messages are being sent. Like a KICK, PART, or
     * QUIT reason.
     *
     * @return the DefaultMessageMap
     */
    @Nonnull
    DefaultMessageMap getDefaultMessageMap();

    /**
     * Gets the client's event manager.
     *
     * @return the event manager for this client
     */
    @Nonnull
    EventManager getEventManager();

    /**
     * Gets the exception listener.
     *
     * @return the exception listener
     */
    @Nonnull
    Listener<Exception> getExceptionListener();

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
     * Gets the STS machine instance, if one is in use.
     *
     * @return the machine, may not be present
     */
    @Nonnull
    Optional<StsMachine> getStsMachine();

    /**
     * Gets the manager of ISUPPORT info.
     *
     * @return the ISUPPORT manager
     */
    @Nonnull
    ISupportManager getISupportManager();

    /**
     * Gets the current message cutter for multi-line messages.
     *
     * @return message cutter
     */
    @Nonnull
    Cutter getMessageCutter();

    /**
     * Gets the message sending queue supplier. Default supplies a
     * {@link SingleDelaySender}
     * with a delay of 1200.
     *
     * @return the supplier
     */
    @Nonnull
    Function<Client.WithManagement, ? extends MessageSendingQueue> getMessageSendingQueueSupplier();

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
     * ClientNegotiationCompleteEvent}
     *
     * @return the server information object
     */
    @Nonnull
    ServerInfo getServerInfo();

    /**
     * Gets the User that the client is represented by. Will return {@link
     * Optional#empty()} until the Client is in a Channel for which joining
     * has completed, meaning full user info is available.
     *
     * @return the user of this client if known
     */
    @Nonnull
    Optional<User> getUser();

    /**
     * Gets the user's modes. Will return {@link Optional#empty()} until the
     * Client has queried this information, which is requested after the
     * connection registration has completed.
     *
     * @return user modes of this client if known
     */
    @Nonnull
    Optional<ModeStatusList<UserMode>> getUserModes();

    /**
     * Checks to see if this client is the same as the given user.
     *
     * @param user user to check this client against
     * @return true if this client is the user, false if not
     */
    default boolean isUser(@Nullable User user) {
        return (user != null) && user.equals(this.getUser().orElse(null));
    }

    /**
     * KNOCKs on a +i (but not +p) channel, requesting an INVITE.
     *
     * @param channelName the channel to send the KNOCK for.
     */
    void knockChannel(@Nonnull String channelName);

    /**
     * Triggers a reconnect, quitting with the default {@link
     * DefaultMessageType#RECONNECT} message.
     */
    void reconnect();

    /**
     * Triggers a reconnect, quitting with the given reason.
     *
     * @param reason disconnect reason
     */
    void reconnect(@Nullable String reason);

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
     * @param reason part reason
     * @throws IllegalArgumentException if channel is null
     */
    void removeChannel(@Nonnull String channel, @Nullable String reason);

    /**
     * Sends a CTCP message to a target user or channel. Automagically adds
     * the CTCP delimiter around the message and escapes the characters that
     * need escaping when sending a CTCP message.
     * <p>
     * <i>Note: CTCP replies should not be sent this way. Catch the message
     * with the {@link PrivateCtcpQueryEvent} and reply there or use
     * {@link #sendCtcpReply(String, String)}</i>
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    void sendCtcpMessage(@Nonnull String target, @Nonnull String message);

    /**
     * Sends a CTCP message to a target user or channel. Automagically adds
     * the CTCP delimiter around the message and escapes the characters that
     * need escaping when sending a CTCP message.
     * <p>
     * <i>Note: CTCP replies should not be sent this way. Catch the message
     * with the {@link PrivateCtcpQueryEvent} and reply there or use
     * {@link #sendCtcpReply(MessageReceiver, String)}</i>
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    default void sendCtcpMessage(@Nonnull MessageReceiver target, @Nonnull String message) {
        Sanity.nullCheck(target, "Target cannot be null");
        this.sendCtcpMessage(target.getMessagingName(), message);
    }

    /**
     * Sends a CTCP reply to a target user or channel. Automagically adds
     * the CTCP delimiter around the message and escapes the characters that
     * need escaping when sending a CTCP message.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    void sendCtcpReply(@Nonnull String target, @Nonnull String message);

    /**
     * Sends a CTCP reply to a target user or channel. Automagically adds
     * the CTCP delimiter around the message and escapes the characters that
     * need escaping when sending a CTCP message.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    default void sendCtcpReply(@Nonnull MessageReceiver target, @Nonnull String message) {
        Sanity.nullCheck(target, "Target cannot be null");
        this.sendCtcpMessage(target.getMessagingName(), message);
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
     * Sends a potentially multi-line message to a target user or channel
     * using the client's current {@link Cutter}.
     * <p>
     * Note that bots may not react appropriately to a message split across
     * multiple lines.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    default void sendMultiLineMessage(@Nonnull String target, @Nonnull String message) {
        this.sendMultiLineMessage(target, message, this.getMessageCutter());
    }

    /**
     * Sends a potentially multi-line message to a target user or channel
     * using the defined {@link Cutter}.
     * <p>
     * Note that bots may not react appropriately to a message split across
     * multiple lines.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @param cutter cutter to utilize
     * @throws IllegalArgumentException for null parameters
     */
    void sendMultiLineMessage(@Nonnull String target, @Nonnull String message, @Nonnull Cutter cutter);

    /**
     * Sends a potentially multi-line message to a target user or channel
     * using the client's current {@link Cutter}.
     * <p>
     * Note that bots may not react appropriately to a message split across
     * multiple lines.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    default void sendMultiLineMessage(@Nonnull MessageReceiver target, @Nonnull String message) {
        this.sendMultiLineMessage(target, message, this.getMessageCutter());
    }

    /**
     * Sends a potentially multi-line message to a target user or channel
     * using the defined {@link Cutter}.
     * <p>
     * Note that bots may not react appropriately to a message split across
     * multiple lines.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @param cutter cutter to utilize
     * @throws IllegalArgumentException for null parameters
     */
    default void sendMultiLineMessage(@Nonnull MessageReceiver target, @Nonnull String message, @Nonnull Cutter cutter) {
        Sanity.nullCheck(target, "Target cannot be null");
        this.sendMultiLineMessage(target.getMessagingName(), message, cutter);
    }

    /**
     * Sends a potentially multi-line notice to a target user or channel
     * using the client's current {@link Cutter}.
     * <p>
     * Note that bots may not react appropriately to a message split across
     * multiple lines.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    default void sendMultiLineNotice(@Nonnull String target, @Nonnull String message) {
        this.sendMultiLineNotice(target, message, this.getMessageCutter());
    }

    /**
     * Sends a potentially multi-line notice to a target user or channel
     * using the defined {@link Cutter}.
     * <p>
     * Note that bots may not react appropriately to a message split across
     * multiple lines.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @param cutter cutter to utilize
     * @throws IllegalArgumentException for null parameters
     */
    void sendMultiLineNotice(@Nonnull String target, @Nonnull String message, @Nonnull Cutter cutter);

    /**
     * Sends a potentially multi-line notice to a target user or channel
     * using the client's current {@link Cutter}.
     * <p>
     * Note that bots may not react appropriately to a message split across
     * multiple lines.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @throws IllegalArgumentException for null parameters
     */
    default void sendMultiLineNotice(@Nonnull MessageReceiver target, @Nonnull String message) {
        this.sendMultiLineNotice(target, message, this.getMessageCutter());
    }

    /**
     * Sends a potentially multi-line notice to a target user or channel
     * using the defined {@link Cutter}.
     * <p>
     * Note that bots may not react appropriately to a message split across
     * multiple lines.
     *
     * @param target the destination of the message
     * @param message the message to send
     * @param cutter cutter to utilize
     * @throws IllegalArgumentException for null parameters
     */
    default void sendMultiLineNotice(@Nonnull MessageReceiver target, @Nonnull String message, @Nonnull Cutter cutter) {
        Sanity.nullCheck(target, "Target cannot be null");
        this.sendMultiLineNotice(target.getMessagingName(), message, cutter);
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
    void setExceptionListener(@Nullable Consumer<Exception> listener);

    /**
     * Sets the message manager for default messages to reply with
     * when certain messages are being sent. Like a KICK, PART, or QUIT
     * reason.
     *
     * @param defaults DefaultMessageMap to set
     */
    void setDefaultMessageMap(@Nonnull DefaultMessageMap defaults);

    /**
     * Sets a listener for all incoming messages from the server.
     * <p>
     * All messages are passed from a single, separate thread.
     *
     * @param listener input listener
     */
    void setInputListener(@Nullable Consumer<String> listener);

    /**
     * Sets the default message cutter to use for multi-line messages.
     *
     * @param cutter cutter to set
     */
    void setMessageCutter(@Nonnull Cutter cutter);

    /**
     * Sets the message sending queue supplier.
     *
     * @param supplier the supplier
     */
    void setMessageSendingQueueSupplier(@Nonnull Function<Client.WithManagement, ? extends MessageSendingQueue> supplier);

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
    void setOutputListener(@Nullable Consumer<String> listener);

    /**
     * Begin connecting to the server.
     *
     * @throws IllegalStateException if the client is already connecting
     */
    void connect();

    /**
     * Shuts down the client without a quit message.
     */
    void shutdown();

    /**
     * Shuts down the client.
     *
     * @param reason quit message to send
     */
    void shutdown(@Nullable String reason);
}
