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
package org.kitteh.irc.client.library.defaults;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.command.AwayCommand;
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.command.ChannelModeCommand;
import org.kitteh.irc.client.library.command.KickCommand;
import org.kitteh.irc.client.library.command.MonitorCommand;
import org.kitteh.irc.client.library.command.OperCommand;
import org.kitteh.irc.client.library.command.TopicCommand;
import org.kitteh.irc.client.library.command.WallopsCommand;
import org.kitteh.irc.client.library.command.WhoisCommand;
import org.kitteh.irc.client.library.defaults.element.DefaultServerMessage;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ModeStatus;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.exception.KittehNagException;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.exception.KittehServerMessageTagException;
import org.kitteh.irc.client.library.feature.ActorTracker;
import org.kitteh.irc.client.library.feature.AuthManager;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.feature.EventListenerSupplier;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.feature.ISupportManager;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageMap;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageType;
import org.kitteh.irc.client.library.feature.defaultmessage.SimpleDefaultMessageMap;
import org.kitteh.irc.client.library.feature.sending.MessageSendingQueue;
import org.kitteh.irc.client.library.feature.sending.QueueProcessingThreadSender;
import org.kitteh.irc.client.library.feature.sts.MemoryStsMachine;
import org.kitteh.irc.client.library.feature.sts.StsHandler;
import org.kitteh.irc.client.library.feature.sts.StsMachine;
import org.kitteh.irc.client.library.feature.sts.StsStorageManager;
import org.kitteh.irc.client.library.util.CISet;
import org.kitteh.irc.client.library.util.CtcpUtil;
import org.kitteh.irc.client.library.util.Cutter;
import org.kitteh.irc.client.library.util.HostWithPort;
import org.kitteh.irc.client.library.util.Listener;
import org.kitteh.irc.client.library.util.Pair;
import org.kitteh.irc.client.library.util.QueueProcessingThread;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.net.ssl.TrustManagerFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of {@link Client}.
 */
public class DefaultClient implements Client.WithManagement {
    private final class ClientCommands implements Commands {
        @Override
        public @NonNull AwayCommand away() {
            return new AwayCommand(DefaultClient.this);
        }

        @Override
        public @NonNull CapabilityRequestCommand capabilityRequest() {
            return new CapabilityRequestCommand(DefaultClient.this);
        }

        @Override
        public @NonNull ChannelModeCommand mode(@NonNull Channel channel) {
            Sanity.nullCheck(channel, "Channel cannot be null");
            Sanity.truthiness(DefaultClient.this == channel.getClient(), "Client mismatch");
            return new ChannelModeCommand(DefaultClient.this, channel.getMessagingName());
        }

        @Override
        public @NonNull KickCommand kick(@NonNull Channel channel) {
            Sanity.nullCheck(channel, "Channel cannot be null");
            Sanity.truthiness(DefaultClient.this == channel.getClient(), "Client mismatch");
            return new KickCommand(DefaultClient.this, channel.getMessagingName());
        }

        @Override
        public @NonNull MonitorCommand monitor() {
            return new MonitorCommand(DefaultClient.this);
        }

        @Override
        public @NonNull OperCommand oper() {
            return new OperCommand(DefaultClient.this);
        }

        @Override
        public @NonNull TopicCommand topic(@NonNull Channel channel) {
            Sanity.nullCheck(channel, "Channel cannot be null");
            Sanity.truthiness(DefaultClient.this == channel.getClient(), "Client mismatch");
            return new TopicCommand(DefaultClient.this, channel.getMessagingName());
        }

        @Override
        public @NonNull WallopsCommand wallops() {
            return new WallopsCommand(DefaultClient.this);
        }

        @Override
        public @NonNull WhoisCommand whois() {
            return new WhoisCommand(DefaultClient.this);
        }
    }

    private final class InputProcessor extends QueueProcessingThread<String> {
        private InputProcessor() {
            super("KICL Input Processor (" + DefaultClient.this.getName() + ')');
        }

        @Override
        protected void processElement(@NonNull String element) {
            try {
                DefaultClient.this.handleLine(element);
            } catch (final Exception thrown) {
                DefaultClient.this.exceptionListener.queue(thrown);
            }
        }
    }

    private final String[] pingPurr = new String[]{"MEOW", "MEOW!", "PURR", "PURRRRRRR", "MEOWMEOW", ":3", "HISS"};
    private int pingPurrCount;

    private final InputProcessor processor;
    private ServerInfo.WithManagement serverInfo;

    private String goalNick;
    private String currentNick;
    private String requestedNick;

    private final Set<String> channelsIntended = new CISet(this);

    private NettyManager.ClientConnection connection;

    private Cutter messageCutter = new Cutter.DefaultWordCutter();

    private AuthManager authManager;
    private CapabilityManager.WithManagement capabilityManager;
    private EventManager eventManager;
    private ISupportManager iSupportManager;
    private MessageTagManager messageTagManager;
    private ActorTracker actorTracker;

    private Listener<Exception> exceptionListener;
    private Listener<String> inputListener;
    private Listener<String> outputListener;

    private DefaultMessageMap defaultMessageMap;

    private Map<Character, ModeStatus<UserMode>> userModes;
    private StsMachine stsMachine;

    private final ClientCommands commands = new ClientCommands();

    private final MessageSendingQueue messageSendingImmediate;
    private MessageSendingQueue messageSendingScheduled;
    private final Object messageSendingLock = new Object();
    private boolean isSending = false;

    private String name;
    private InetSocketAddress bindAddress;
    private HostWithPort serverAddress;
    private HostWithPort proxyAddress;
    private ProxyType proxyType;
    private String serverPassword;
    private String userString;
    private String realName;
    private boolean secure;
    private Path secureKeyCertChain;
    private Path secureKey;
    private String secureKeyPassword;
    private TrustManagerFactory secureTrustManagerFactory;
    private StsStorageManager stsStorageManager;
    private String webircHost;
    private InetAddress webircIP;
    private String webircPassword;
    private String webircGateway;
    private Function<Client.WithManagement, ? extends MessageSendingQueue> messageSendingQueueSupplier;
    private Function<Client.WithManagement, ? extends ServerInfo.WithManagement> serverInfoSupplier;

    /**
     * Creates a new default client.
     */
    public DefaultClient() {
        this.processor = new InputProcessor();
        this.messageSendingImmediate = new QueueProcessingThreadSender(this, "Immediate");
    }

    @Override
    public void initialize(@NonNull String name, @NonNull HostWithPort serverAddress, @Nullable String serverPassword,
                           @Nullable InetSocketAddress bindAddress,
                           @Nullable HostWithPort proxyAddress, @Nullable ProxyType proxyType,
                           @NonNull String nick, @NonNull String userString, @NonNull String realName, @NonNull ActorTracker actorTracker,
                           @NonNull AuthManager authManager, CapabilityManager.@NonNull WithManagement capabilityManager,
                           @NonNull EventManager eventManager, @NonNull List<EventListenerSupplier> listenerSuppliers,
                           @NonNull MessageTagManager messageTagManager,
                           @NonNull ISupportManager iSupportManager, @Nullable DefaultMessageMap defaultMessageMap,
                           @NonNull Function<Client.WithManagement, ? extends MessageSendingQueue> messageSendingQueue,
                           @NonNull Function<Client.WithManagement, ? extends ServerInfo.WithManagement> serverInfo,
                           @Nullable Consumer<Exception> exceptionListener, @Nullable Consumer<String> inputListener,
                           @Nullable Consumer<String> outputListener, boolean secure, @Nullable Path secureKeyCertChain,
                           @Nullable Path secureKey, @Nullable String secureKeyPassword, @Nullable TrustManagerFactory trustManagerFactory,
                           @Nullable StsStorageManager stsStorageManager, @Nullable String webircHost,
                           @Nullable InetAddress webircIP, @Nullable String webircPassword, @Nullable String webircGateway) {
        this.name = name;
        this.serverAddress = serverAddress;
        this.proxyAddress = proxyAddress;
        this.proxyType = proxyType;
        this.serverPassword = serverPassword;
        this.bindAddress = bindAddress;
        this.currentNick = this.requestedNick = this.goalNick = nick;
        this.userString = userString;
        this.realName = realName;
        this.actorTracker = actorTracker;
        this.authManager = authManager;
        this.capabilityManager = capabilityManager;
        this.eventManager = eventManager;
        this.messageTagManager = messageTagManager;
        this.iSupportManager = iSupportManager;
        this.defaultMessageMap = (defaultMessageMap == null) ? new SimpleDefaultMessageMap() : defaultMessageMap;
        this.messageSendingQueueSupplier = messageSendingQueue;
        this.serverInfoSupplier = serverInfo;
        this.exceptionListener = new Listener<>(this, exceptionListener);
        this.inputListener = new Listener<>(this, inputListener);
        this.outputListener = new Listener<>(this, outputListener);
        this.secure = secure;
        this.secureKeyCertChain = secureKeyCertChain;
        this.secureKey = secureKey;
        this.secureKeyPassword = secureKeyPassword;
        this.secureTrustManagerFactory = trustManagerFactory;
        this.stsStorageManager = stsStorageManager;
        this.webircHost = webircHost;
        this.webircIP = webircIP;
        this.webircPassword = webircPassword;
        this.webircGateway = webircGateway;

        for (EventListenerSupplier eventListenerSupplier : listenerSuppliers) {
            this.eventManager.registerEventListener(eventListenerSupplier.getConstructingFunction().apply(this));
        }

        this.serverInfo = this.serverInfoSupplier.apply(this);

        if (this.stsStorageManager != null) {
            this.configureSts();
        } else if (!this.isSecureConnection()) {
            this.exceptionListener.queue(new KittehNagException(
                    "Connection is insecure. If the server does not support TLS, consider enabling STS support to " +
                            "facilitate automatic TLS upgrades when it does."
            ));
        }

        this.messageSendingScheduled = this.getMessageSendingQueueSupplier().apply(this);
    }

    private void configureSts() {
        this.stsMachine = new MemoryStsMachine(this.stsStorageManager, this);
        this.eventManager.registerEventListener(new StsHandler(this.stsMachine, this));
    }

    @Override
    public void addChannel(@NonNull String... channels) {
        Sanity.nullCheck(channels, "Channels cannot be null");
        Sanity.truthiness(channels.length > 0, "Channels cannot be empty array");
        for (String channelName : channels) {
            Sanity.truthiness(this.serverInfo.isValidChannel(channelName), "Invalid channel name " + channelName);
        }
        for (String channelName : channels) {
            this.channelsIntended.add(channelName);
            this.sendRawLine("JOIN " + channelName);
        }
    }

    @Override
    public void addKeyProtectedChannel(@NonNull String channel, @NonNull String key) {
        Sanity.nullCheck(channel, "Channel cannot be null");
        Sanity.nullCheck(key, "Key cannot be null");
        Sanity.truthiness(this.serverInfo.isValidChannel(channel), "Invalid channel name " + channel);
        this.channelsIntended.add(channel);
        this.sendRawLine("JOIN " + channel + ' ' + key);
    }

    @Override
    public void addKeyProtectedChannel(@NonNull Pair<String, String>... channelsAndKeys) {
        Sanity.nullCheck(channelsAndKeys, "Channel/key pairs cannot be null");
        Sanity.truthiness(channelsAndKeys.length > 0, "Channel/key pairs cannot be empty array");
        for (Pair<String, String> channelAndKey : channelsAndKeys) {
            String channelName = channelAndKey.getLeft();
            Sanity.nullCheck(channelName, "Channel/key pairs cannot contain null channel name");
            Sanity.truthiness(this.serverInfo.isValidChannel(channelName), "Channel/key pairs cannot contain invalid channel name " + channelName);
        }
        for (Pair<String, String> channelAndKey : channelsAndKeys) {
            this.channelsIntended.add(channelAndKey.getLeft());
            this.sendRawLine("JOIN " + channelAndKey.getLeft() + (channelAndKey.getRight() == null ? "" : (' ' + channelAndKey.getRight())));
        }
    }

    @Override
    public @NonNull AuthManager getAuthManager() {
        return this.authManager;
    }

    @Override
    public @NonNull InetSocketAddress getBindAddress() {
        return this.bindAddress;
    }

    @Override
    public CapabilityManager.@NonNull WithManagement getCapabilityManager() {
        return this.capabilityManager;
    }

    @Override
    public @NonNull Optional<Channel> getChannel(@NonNull String name) {
        return this.actorTracker.getTrackedChannel(Sanity.nullCheck(name, "Channel name cannot be null"));
    }

    @Override
    public @NonNull Set<Channel> getChannels() {
        return this.actorTracker.getTrackedChannels();
    }

    @Override
    public @NonNull Set<Channel> getChannels(@NonNull Collection<String> channels) {
        return Sanity.nullCheck(channels, "Channels collection cannot be null").stream()
                .filter(Objects::nonNull)
                .map(this.actorTracker::getTrackedChannel)
                .flatMap(optional -> optional.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toSet());
    }

    @Override
    public @NonNull DefaultMessageMap getDefaultMessageMap() {
        return this.defaultMessageMap;
    }

    @Override
    public @NonNull EventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public @NonNull Listener<Exception> getExceptionListener() {
        return this.exceptionListener;
    }

    @Override
    public @NonNull String getIntendedNick() {
        return this.goalNick;
    }

    @Override
    public @NonNull ISupportManager getISupportManager() {
        return this.iSupportManager;
    }

    @Override
    public @NonNull Optional<StsMachine> getStsMachine() {
        return Optional.ofNullable(this.stsMachine);
    }

    @Override
    public @NonNull Cutter getMessageCutter() {
        return this.messageCutter;
    }

    @Override
    public @NonNull Function<Client.WithManagement, ? extends MessageSendingQueue> getMessageSendingQueueSupplier() {
        return this.messageSendingQueueSupplier;
    }

    @Override
    public @NonNull MessageTagManager getMessageTagManager() {
        return this.messageTagManager;
    }

    @Override
    public @NonNull String getName() {
        return this.name;
    }

    @Override
    public @NonNull String getNick() {
        return this.currentNick;
    }

    @Override
    public ServerInfo.@NonNull WithManagement getServerInfo() {
        return this.serverInfo;
    }

    @Override
    public @NonNull Optional<User> getUser() {
        return this.actorTracker.getTrackedUser(this.getNick());
    }

    @Override
    public @NonNull Optional<ModeStatusList<UserMode>> getUserModes() {
        return (this.userModes == null) ? Optional.empty() : Optional.of(ModeStatusList.of(this.userModes.values()));
    }

    @Override
    public void knockChannel(@NonNull String channelName) {
        this.sendRawLine("KNOCK " + Sanity.nullCheck(channelName, "Channel cannot be null"));
    }

    @Override
    public void removeChannel(@NonNull String channelName) {
        this.removeChannelPlease(channelName, this.defaultMessageMap.getDefault(DefaultMessageType.PART).orElse(null));
    }

    @Override
    public void removeChannel(@NonNull String channelName, @Nullable String reason) {
        this.removeChannelPlease(channelName, reason);
    }

    private void removeChannelPlease(@NonNull String channelName, @Nullable String reason) {
        Sanity.truthiness(this.serverInfo.isValidChannel(channelName), "Invalid channel name " + channelName);
        if (reason != null) {
            Sanity.safeMessageCheck(reason, "Part reason");
        }
        this.channelsIntended.remove(channelName);
        this.sendRawLine("PART " + channelName + (reason != null ? (" :" + reason) : ""));
    }

    @Override
    public void sendCtcpMessage(@NonNull String target, @NonNull String message) {
        Sanity.safeMessageCheck(target, "Target");
        Sanity.safeMessageCheck(message);
        Sanity.noSpaces(target, "Target");
        this.sendRawLine("PRIVMSG " + target + " :" + CtcpUtil.toCtcp(message));
    }

    @Override
    public void sendCtcpReply(@NonNull String target, @NonNull String message) {
        Sanity.safeMessageCheck(target, "Target");
        Sanity.safeMessageCheck(message);
        Sanity.noSpaces(target, "Target");
        this.sendRawLine("NOTICE " + target + " :" + CtcpUtil.toCtcp(message));
    }

    @Override
    public void sendMessage(@NonNull String target, @NonNull String message) {
        Sanity.safeMessageCheck(target, "Target");
        Sanity.safeMessageCheck(message);
        Sanity.noSpaces(target, "Target");
        this.sendRawLine("PRIVMSG " + target + " :" + message);
    }

    @Override
    public void sendMultiLineMessage(@NonNull String target, @NonNull String message, @NonNull Cutter cutter) {
        Sanity.nullCheck(target, "Target cannot be null");
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.nullCheck(cutter, "Cutter cannot be null");
        cutter.split(message, this.getRemainingLength("PRIVMSG", target)).forEach(line -> this.sendMessage(target, line));
    }

    @Override
    public void sendMultiLineNotice(@NonNull String target, @NonNull String message, @NonNull Cutter cutter) {
        Sanity.nullCheck(target, "Target cannot be null");
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.nullCheck(cutter, "Cutter cannot be null");
        cutter.split(message, this.getRemainingLength("NOTICE", target)).forEach(line -> this.sendNotice(target, line));
    }

    private int getRemainingLength(@NonNull String type, @NonNull String target) {
        // :nick!name@host PRIVMSG/NOTICE TARGET :MESSAGE\r\n
        // So that's two colons, three spaces, CR, and LF. 7 chars.
        // 512 - 7 = 505
        // Then, drop the user's full name (nick!name@host) and target
        // If self name is unknown, let's just do 100 for now
        // This will only happen for messages prior to getting a self WHOIS
        // Lastly drop the PRIVMSG or NOTICE length
        return 505 - this.getUser().map(user -> user.getName().length()).orElse(100) - target.length() - type.length();
    }

    @Override
    public @Nullable Path getSecureKey() {
        return this.secureKey;
    }

    @Override
    public @Nullable Path getSecureKeyCertChain() {
        return this.secureKeyCertChain;
    }

    @Override
    public @Nullable String getSecureKeyPassword() {
        return this.secureKeyPassword;
    }

    @Override
    public @Nullable TrustManagerFactory getSecureTrustManagerFactory() {
        return this.secureTrustManagerFactory;
    }

    @Override
    public @NonNull HostWithPort getServerAddress() {
        return this.serverAddress;
    }

    @Override
    public @NonNull Optional<ProxyType> getProxyType() {
        return Optional.ofNullable(this.proxyType);
    }

    @Override
    public @NonNull Optional<HostWithPort> getProxyAddress() {
        return Optional.ofNullable(this.proxyAddress);
    }

    @Override
    public void sendNotice(@NonNull String target, @NonNull String message) {
        Sanity.safeMessageCheck(target, "Target");
        Sanity.safeMessageCheck(message);
        Sanity.noSpaces(target, "Target");
        this.sendRawLine("NOTICE " + target + " :" + message);
    }

    @Override
    public void sendRawLine(@NonNull String message) {
        this.sendRawLine(message, false, false);
    }

    @Override
    public void sendRawLineAvoidingDuplication(@NonNull String message) {
        this.sendRawLine(message, false, true);
    }

    @Override
    public void sendRawLineImmediately(@NonNull String message) {
        this.sendRawLine(message, true, false);
    }

    private void sendRawLine(@NonNull String message, boolean priority, boolean avoidDuplicates) {
        Sanity.safeMessageCheck(message);
        if (!message.isEmpty() && (message.length() > ((message.charAt(0) == '@') ? 1022 : 510))) {
            throw new IllegalArgumentException("Message too long: " + message.length());
        }
        synchronized (this.messageSendingLock) {
            if (priority) {
                this.messageSendingImmediate.queue(message);
            } else if (!avoidDuplicates || !this.messageSendingScheduled.contains(message)) {
                this.messageSendingScheduled.queue(message);
            }
        }
    }

    @Override
    public void setExceptionListener(@Nullable Consumer<Exception> listener) {
        if (listener == null) {
            this.exceptionListener.removeConsumer();
        } else {
            this.exceptionListener.setConsumer(listener);
        }
    }

    @Override
    public void setDefaultMessageMap(@NonNull DefaultMessageMap defaults) {
        Sanity.nullCheck(defaults, "Defaults cannot be null");
        this.defaultMessageMap = defaults;
    }

    @Override
    public void setInputListener(@Nullable Consumer<String> listener) {
        if (listener == null) {
            this.inputListener.removeConsumer();
        } else {
            this.inputListener.setConsumer(listener);
        }
    }

    @Override
    public void setMessageCutter(@NonNull Cutter cutter) {
        this.messageCutter = Sanity.nullCheck(cutter, "Cutter cannot be null");
    }

    @Override
    public void setMessageSendingQueueSupplier(@NonNull Function<Client.WithManagement, ? extends MessageSendingQueue> supplier) {
        this.messageSendingQueueSupplier = Sanity.nullCheck(supplier, "Supplier cannot be null");
        synchronized (this.messageSendingLock) {
            MessageSendingQueue newQueue = this.getMessageSendingQueueSupplier().apply(this);
            this.messageSendingScheduled.shutdown().forEach(newQueue::queue);
            Optional<Consumer<String>> consumer = this.messageSendingScheduled.getConsumer();
            this.messageSendingScheduled = newQueue;
            if (this.isSending && consumer.isPresent()) {
                this.messageSendingScheduled.beginSending(consumer.get());
            }
        }
    }

    @Override
    public void setNick(@NonNull String nick) {
        Sanity.safeMessageCheck(nick, "Nick");
        this.goalNick = nick.trim();
        this.sendNickChange(this.goalNick);
    }

    @Override
    public void setOutputListener(@Nullable Consumer<String> listener) {
        if (listener == null) {
            this.outputListener.removeConsumer();
        } else {
            this.outputListener.setConsumer(listener);
        }
    }

    @Override
    public void shutdown() {
        this.shutdownInternal(this.defaultMessageMap.getDefault(DefaultMessageType.QUIT).orElse(null));
    }

    @Override
    public void shutdown(@Nullable String reason) {
        if (reason != null) {
            Sanity.safeMessageCheck(reason, "Quit reason");
        }
        this.shutdownInternal(reason);
    }

    private void shutdownInternal(@Nullable String reason) {
        this.processor.interrupt();

        this.messageSendingImmediate.shutdown();
        this.messageSendingScheduled.shutdown();

        if (this.connection != null) { // In case shutdown is called while building.
            this.connection.shutdown(reason, false);
        }

        // Shut these down last, so they get any last firings
        this.exceptionListener.shutdown();
        this.inputListener.shutdown();
        this.outputListener.shutdown();
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).add("name", this.getName()).add("server", this.serverAddress).toString();
    }

    /**
     * Queue up a line for processing.
     *
     * @param line line to be processed
     */
    @Override
    public void processLine(@NonNull String line) {
        if (line.startsWith("PING ")) {
            this.sendRawLineImmediately("PONG " + line.substring(5));
        } else if (!line.isEmpty()) {
            this.processor.queue(line);
        }
    }

    @Override
    public @NonNull ActorTracker getActorTracker() {
        return this.actorTracker;
    }

    @Override
    public boolean isConnectionAlive() {
        return (this.connection != null) && this.connection.isAlive();
    }

    @Override
    public @NonNull Listener<String> getInputListener() {
        return this.inputListener;
    }

    @Override
    public @NonNull Set<String> getIntendedChannels() {
        return Collections.unmodifiableSet(new HashSet<>(this.channelsIntended));
    }

    @Override
    public @NonNull Listener<String> getOutputListener() {
        return this.outputListener;
    }

    @Override
    public @NonNull String getRequestedNick() {
        return this.requestedNick;
    }

    @Override
    public void connect() {
        if (this.isConnectionAlive()) {
            throw new IllegalStateException("Client is already connecting");
        }

        this.connection = NettyManager.connect(this);
        this.processor.queue("");

        // If we have WebIRC information, send it before everything.
        // "The WEBIRC command MUST be the first command sent from the WebIRC gateway to the IRC server and MUST be sent before capability negotiation."
        // https://ircv3.net/specs/extensions/webirc.html
        if (this.webircPassword != null) {
            this.sendRawLineImmediately("WEBIRC " + this.webircPassword + ' ' + this.webircGateway + ' ' + this.webircHost + ' ' + this.webircIP.getHostAddress());
        }

        this.sendRawLineImmediately("CAP LS 302");

        // If the server has a password, send that along before USER and NICK.
        String password = this.serverPassword;
        if (password != null) {
            this.sendRawLineImmediately("PASS " + (password.contains(" ") ? ":" : "") + password);
        }

        // Initial USER and NICK messages. Let's just assume we want +iw (send 8)
        this.sendRawLineImmediately("USER " + this.userString + " 8 * :" + this.realName);
        this.sendNickChange(this.goalNick);
    }

    @Override
    public void beginMessageSendingImmediate(@NonNull Consumer<String> consumer) {
        synchronized (this.messageSendingLock) {
            this.messageSendingImmediate.beginSending(consumer);
        }
    }

    @Override
    public void pauseMessageSending() {
        this.isSending = false;
        synchronized (this.messageSendingLock) {
            this.messageSendingImmediate.pause();
            this.messageSendingScheduled.pause();
        }
    }

    @Override
    public void ping() {
        this.sendRawLine("PING " + this.pingPurr[this.pingPurrCount++ % this.pingPurr.length]); // Connection's asleep, post cat sounds
    }

    @Override
    public void sendNickChange(@NonNull String newNick) {
        this.requestedNick = newNick;
        this.sendRawLineImmediately("NICK " + newNick);
    }

    @Override
    public void setCurrentNick(@NonNull String nick) {
        this.currentNick = nick;
    }

    @Override
    public void setServerAddress(@NonNull HostWithPort address) {
        this.serverAddress = address;
    }

    @Override
    public void setUserModes(@NonNull ModeStatusList<UserMode> userModes) {
        this.userModes = new HashMap<>(userModes.getStatuses().stream().collect(Collectors.toMap(modeStatus -> modeStatus.getMode().getChar(), Function.identity())));
    }

    @Override
    public void startSending() {
        this.isSending = true;
        this.connection.startPing();
        synchronized (this.messageSendingLock) {
            this.messageSendingScheduled.beginSending(this.messageSendingImmediate::queue);
        }
    }

    @Override
    public void updateUserModes(@NonNull ModeStatusList<UserMode> userModes) {
        if (this.userModes == null) {
            this.userModes = new HashMap<>();
        }
        for (ModeStatus<UserMode> status : userModes.getStatuses()) {
            if (status.isSetting()) {
                this.userModes.put(status.getMode().getChar(), status);
            } else {
                this.userModes.remove(status.getMode().getChar());
            }
        }
    }

    @Override
    public void reconnect() {
        this.connection.shutdown(DefaultMessageType.RECONNECT, true);
    }

    @Override
    public void reconnect(@Nullable String reason) {
        this.connection.shutdown(reason, true);
    }

    @Override
    public boolean isSecureConnection() {
        return this.secure;
    }

    private void handleLine(final @NonNull String line) {
        if (line.isEmpty()) {
            this.actorTracker.reset();
            this.capabilityManager.reset();
            this.serverInfo = this.serverInfoSupplier.apply(this);
            return;
        }

        int position = 0;
        int next;
        // Skip starting spaces just in case
        while ((next = line.indexOf(' ', position)) == position) {
            position = next + 1;
        }

        List<MessageTag> tags;
        if (line.charAt(position) == '@') {
            String tagSection = line.substring(position, next);
            position = next + 1;
            if (tagSection.length() < 2) {
                throw new KittehServerMessageTagException(line, "Server sent an empty tag section");
            }
            tags = this.messageTagManager.getCapabilityTags(tagSection.substring(1));
            // Skip more spaces just in case
            while ((next = line.indexOf(' ', position)) == position) {
                position = next + 1;
            }
        } else {
            tags = Collections.unmodifiableList(new ArrayList<>());
        }

        final String actorName;
        if (line.charAt(position) == ':') {
            actorName = line.substring(position + 1, next);
            position = next + 1;
        } else {
            actorName = "";
        }
        final Actor actor = this.actorTracker.getActor(actorName);

        String commandString = null;
        List<String> args = new ArrayList<>();

        boolean dobbyIsFreeElf = false;
        free:
        while ((next = line.indexOf(' ', position)) != -1) {
            if (line.charAt(position) == ':') {
                position++;
                dobbyIsFreeElf = true;
                /* I've got to */
                break free;
            } else if (position != next) {
                String bit = line.substring(position, next);
                if (commandString == null) {
                    commandString = bit;
                } else {
                    args.add(bit);
                }
            }
            position = next + 1;
        }
        if (position != line.length()) {
            String bit = line.substring((!dobbyIsFreeElf && (line.charAt(position) == ':')) ? (position + 1) : position);
            if (commandString == null) {
                commandString = bit;
            } else {
                args.add(bit);
            }
        }

        if (commandString == null) {
            throw new KittehServerMessageException(new DefaultServerMessage(line, tags), "Server sent a message without a command");
        }

        try {
            int numeric = Integer.parseInt(commandString);
            this.eventManager.callEvent(new ClientReceiveNumericEvent(this, new DefaultServerMessage.NumericCommand(numeric, line, tags), actor, commandString, numeric, args));
        } catch (NumberFormatException exception) {
            this.eventManager.callEvent(new ClientReceiveCommandEvent(this, new DefaultServerMessage.StringCommand(commandString, line, tags), actor, commandString, args));
        }
    }

    @Override
    public @NonNull Commands commands() {
        return this.commands;
    }
}
