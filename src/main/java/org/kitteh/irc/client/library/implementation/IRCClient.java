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
import org.kitteh.irc.client.library.command.AwayCommand;
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.command.ChannelModeCommand;
import org.kitteh.irc.client.library.command.KickCommand;
import org.kitteh.irc.client.library.command.MonitorCommand;
import org.kitteh.irc.client.library.command.OperCommand;
import org.kitteh.irc.client.library.command.TopicCommand;
import org.kitteh.irc.client.library.command.WallopsCommand;
import org.kitteh.irc.client.library.command.WhoisCommand;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.defaults.DefaultServerMessage;
import org.kitteh.irc.client.library.element.mode.ModeStatus;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.exception.KittehNagException;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.exception.KittehServerMessageTagException;
import org.kitteh.irc.client.library.feature.AuthManager;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageMap;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageType;
import org.kitteh.irc.client.library.feature.defaultmessage.SimpleDefaultMessageMap;
import org.kitteh.irc.client.library.feature.sending.MessageSendingQueue;
import org.kitteh.irc.client.library.feature.sending.QueueProcessingThreadSender;
import org.kitteh.irc.client.library.feature.sts.STSMachine;
import org.kitteh.irc.client.library.util.CISet;
import org.kitteh.irc.client.library.util.CTCPUtil;
import org.kitteh.irc.client.library.util.Cutter;
import org.kitteh.irc.client.library.util.Listener;
import org.kitteh.irc.client.library.util.Pair;
import org.kitteh.irc.client.library.util.QueueProcessingThread;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

final class IRCClient extends InternalClient {
    private final class ClientCommands implements Commands {
        @Nonnull
        @Override
        public AwayCommand away() {
            return new AwayCommand(IRCClient.this);
        }

        @Nonnull
        @Override
        public CapabilityRequestCommand capabilityRequest() {
            return new CapabilityRequestCommand(IRCClient.this);
        }

        @Nonnull
        @Override
        public ChannelModeCommand mode(@Nonnull Channel channel) {
            Sanity.nullCheck(channel, "Channel cannot be null");
            Sanity.truthiness(IRCClient.this == channel.getClient(), "Client mismatch");
            return new ChannelModeCommand(IRCClient.this, channel.getMessagingName());
        }

        @Nonnull
        @Override
        public KickCommand kick(@Nonnull Channel channel) {
            Sanity.nullCheck(channel, "Channel cannot be null");
            Sanity.truthiness(IRCClient.this == channel.getClient(), "Client mismatch");
            return new KickCommand(IRCClient.this, channel.getMessagingName());
        }

        @Nonnull
        @Override
        public MonitorCommand monitor() {
            return new MonitorCommand(IRCClient.this);
        }

        @Nonnull
        @Override
        public OperCommand oper() {
            return new OperCommand(IRCClient.this);
        }

        @Nonnull
        @Override
        public TopicCommand topic(@Nonnull Channel channel) {
            Sanity.nullCheck(channel, "Channel cannot be null");
            Sanity.truthiness(IRCClient.this == channel.getClient(), "Client mismatch");
            return new TopicCommand(IRCClient.this, channel.getMessagingName());
        }

        @Nonnull
        @Override
        public WallopsCommand wallops() {
            return new WallopsCommand(IRCClient.this);
        }

        @Nonnull
        @Override
        public WhoisCommand whois() {
            return new WhoisCommand(IRCClient.this);
        }
    }

    private final class InputProcessor extends QueueProcessingThread<String> {
        private InputProcessor() {
            super("KICL Input Processor (" + IRCClient.this.getName() + ')');
        }

        @Override
        protected void processElement(@Nonnull String element) {
            try {
                IRCClient.this.handleLine(element);
            } catch (final Exception thrown) {
                IRCClient.this.exceptionListener.queue(thrown);
            }
        }
    }

    private final String[] pingPurr = new String[]{"MEOW", "MEOW!", "PURR", "PURRRRRRR", "MEOWMEOW", ":3", "HISS"};
    private int pingPurrCount;

    private final Config config;
    private final InputProcessor processor;
    private IRCServerInfo serverInfo = new IRCServerInfo(this);

    private String goalNick;
    private String currentNick;
    private String requestedNick;

    private final Set<String> channelsIntended = new CISet(this);

    private NettyManager.ClientConnection connection;

    private Cutter messageCutter = new Cutter.DefaultWordCutter();

    private final AuthManager authManager;
    private final CapabilityManager.WithManagement capabilityManager;
    private final EventManager eventManager;
    private final ManagerISupport iSupportManager = new ManagerISupport(this);
    private final ManagerMessageTag messageTagManager = new ManagerMessageTag(this);

    private final Listener<Exception> exceptionListener;
    private final Listener<String> inputListener;
    private final Listener<String> outputListener;

    private final ActorProvider actorProvider = new ActorProvider(this);

    private DefaultMessageMap defaultMessageMap;

    private Map<Character, ModeStatus<UserMode>> userModes;
    private STSMachine stsMachine;

    private final ClientCommands commands = new ClientCommands();

    private final MessageSendingQueue messageSendingImmediate;
    private MessageSendingQueue messageSendingScheduled;
    private final Object messageSendingLock = new Object();

    IRCClient(@Nonnull Config config) {
        this.config = config;

        // Get event manager up and running immediately!
        this.eventManager = ((Function<Client, EventManager>) this.config.getNotNull(Config.MANAGER_EVENT)).apply(this);

        this.currentNick = this.requestedNick = this.goalNick = this.config.get(Config.NICK);

        Config.ExceptionConsumerWrapper exceptionListenerWrapper = this.config.get(Config.LISTENER_EXCEPTION);
        this.exceptionListener = new Listener<>(this, (exceptionListenerWrapper == null) ? null : exceptionListenerWrapper.getConsumer());
        Config.StringConsumerWrapper inputListenerWrapper = this.config.get(Config.LISTENER_INPUT);
        this.inputListener = new Listener<>(this, (inputListenerWrapper == null) ? null : inputListenerWrapper.getConsumer());
        Config.StringConsumerWrapper outputListenerWrapper = this.config.get(Config.LISTENER_OUTPUT);
        this.outputListener = new Listener<>(this, (outputListenerWrapper == null) ? null : outputListenerWrapper.getConsumer());

        if (this.config.get(Config.STS_STORAGE_MANAGER) != null) {
            this.configureSts();
        } else if (!this.isSSL()) {
            this.exceptionListener.queue(new KittehNagException(
                    "Connection is insecure. If the server does not support SSL, consider enabling STS support to " +
                            "facilitate automatic SSL upgrades when it does."
            ));
        }

        this.processor = new InputProcessor();
        this.eventManager.registerEventListener(new EventListener(this));

        this.authManager = ((Function<Client, AuthManager>) this.config.getNotNull(Config.MANAGER_AUTH)).apply(this);
        this.capabilityManager = ((Function<Client, CapabilityManager.WithManagement>) this.config.getNotNull(Config.MANAGER_CAPABILITY)).apply(this);

        DefaultMessageMap defaultMessageMap = this.config.get(Config.DEFAULT_MESSAGE_MAP);
        if (defaultMessageMap == null) {
            defaultMessageMap = new SimpleDefaultMessageMap();
        }
        this.defaultMessageMap = defaultMessageMap;

        this.messageSendingImmediate = new QueueProcessingThreadSender(this, "Immediate");
        this.messageSendingScheduled = this.getMessageSendingQueueSupplier().apply(this);
    }

    private void configureSts() {
        this.stsMachine = new MemorySTSMachine(this.config.getNotNull(Config.STS_STORAGE_MANAGER), this);
        this.eventManager.registerEventListener(new STSHandler(this.stsMachine, this));
    }

    @Override
    public void addChannel(@Nonnull String... channels) {
        Sanity.nullCheck(channels, "Channels cannot be null");
        Sanity.truthiness(channels.length > 0, "Channels cannot be empty array");
        for (String channelName : channels) {
            Sanity.truthiness(this.serverInfo.isValidChannel(channelName), "Invalid channel name " + channelName);
        }
        for (String channelName : channels) {
            this.channelsIntended.add(channelName);
            this.sendRawLine("JOIN :" + channelName);
        }
    }

    @Override
    public void addKeyProtectedChannel(@Nonnull String channel, @Nonnull String key) {
        Sanity.nullCheck(channel, "Channel cannot be null");
        Sanity.nullCheck(key, "Key cannot be null");
        Sanity.truthiness(this.serverInfo.isValidChannel(channel), "Invalid channel name " + channel);
        this.channelsIntended.add(channel);
        this.sendRawLine("JOIN :" + channel + ' ' + key);
    }

    @Override
    public void addKeyProtectedChannel(@Nonnull Pair<String, String>... channelsAndKeys) {
        Sanity.nullCheck(channelsAndKeys, "Channel/key pairs cannot be null");
        Sanity.truthiness(channelsAndKeys.length > 0, "Channel/key pairs cannot be empty array");
        for (Pair<String, String> channelAndKey : channelsAndKeys) {
            String channelName = channelAndKey.getLeft();
            Sanity.nullCheck(channelName, "Channel/key pairs cannot contain null channel name");
            Sanity.truthiness(this.serverInfo.isValidChannel(channelName), "Channel/key pairs cannot contain invalid channel name " + channelName);
        }
        for (Pair<String, String> channelAndKey : channelsAndKeys) {
            this.channelsIntended.add(channelAndKey.getLeft());
            this.sendRawLine("JOIN :" + channelAndKey.getLeft() + (channelAndKey.getRight() == null ? "" : (' ' + channelAndKey.getRight())));
        }
    }

    @Override
    @Nonnull
    public AuthManager getAuthManager() {
        return this.authManager;
    }

    @Override
    @Nonnull
    public CapabilityManager.WithManagement getCapabilityManager() {
        return this.capabilityManager;
    }

    @Nonnull
    @Override
    public Optional<Channel> getChannel(@Nonnull String name) {
        Sanity.nullCheck(name, "Channel name cannot be null");
        ActorProvider.IRCChannel channel = this.actorProvider.getTrackedChannel(name);
        return (channel == null) ? Optional.empty() : Optional.of(channel.snapshot());
    }

    @Nonnull
    @Override
    public Set<Channel> getChannels() {
        return this.actorProvider.getTrackedChannels().stream().map(ActorProvider.IRCChannel::snapshot).collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public Set<Channel> getChannels(@Nonnull Collection<String> channels) {
        return Sanity.nullCheck(channels, "Channels collection cannot be null").stream()
                .filter(Objects::nonNull)
                .map(this.actorProvider::getTrackedChannel)
                .filter(Objects::nonNull)
                .map(ActorProvider.IRCChannel::snapshot)
                .collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public DefaultMessageMap getDefaultMessageMap() {
        return this.defaultMessageMap;
    }

    @Nonnull
    @Override
    public EventManager getEventManager() {
        return this.eventManager;
    }

    @Nonnull
    @Override
    public Listener<Exception> getExceptionListener() {
        return this.exceptionListener;
    }

    @Nonnull
    @Override
    public String getIntendedNick() {
        return this.goalNick;
    }

    @Nonnull
    @Override
    public ManagerISupport getISupportManager() {
        return this.iSupportManager;
    }

    @Nonnull
    @Override
    public Optional<STSMachine> getSTSMachine() {
        return Optional.ofNullable(this.stsMachine);
    }

    @Nonnull
    @Override
    public Cutter getMessageCutter() {
        return this.messageCutter;
    }

    @Nonnull
    @Override
    public Function<Client, ? extends MessageSendingQueue> getMessageSendingQueueSupplier() {
        return this.config.getNotNull(Config.MESSAGE_DELAY);
    }

    @Nonnull
    @Override
    public MessageTagManager getMessageTagManager() {
        return this.messageTagManager;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.config.getNotNull(Config.NAME);
    }

    @Nonnull
    @Override
    public String getNick() {
        return this.currentNick;
    }

    @Nonnull
    @Override
    public IRCServerInfo getServerInfo() {
        return this.serverInfo;
    }

    @Nonnull
    @Override
    public Optional<User> getUser() {
        final ActorProvider.IRCUser user = this.actorProvider.getUser(this.getNick());
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(user.snapshot());
    }

    @Override
    @Nonnull
    public Optional<ModeStatusList<UserMode>> getUserModes() {
        return (this.userModes == null) ? Optional.empty() : Optional.of(ModeStatusList.of(this.userModes.values()));
    }

    @Override
    public void knockChannel(@Nonnull String channelName) {
        this.sendRawLine("KNOCK " + Sanity.nullCheck(channelName, "Channel cannot be null"));
    }

    @Override
    public void removeChannel(@Nonnull String channelName) {
        this.removeChannelPlease(channelName, this.defaultMessageMap.getDefault(DefaultMessageType.PART).orElse(null));
    }

    @Override
    public void removeChannel(@Nonnull String channelName, @Nullable String reason) {
        this.removeChannelPlease(channelName, reason);
    }

    private void removeChannelPlease(@Nonnull String channelName, @Nullable String reason) {
        Sanity.truthiness(this.serverInfo.isValidChannel(channelName), "Invalid channel name " + channelName);
        if (reason != null) {
            Sanity.safeMessageCheck(reason, "Part reason");
        }
        this.channelsIntended.remove(channelName);
        this.sendRawLine("PART " + channelName + (reason != null ? (" :" + reason) : ""));
    }

    @Override
    public void sendCTCPMessage(@Nonnull String target, @Nonnull String message) {
        Sanity.safeMessageCheck(target, "Target");
        Sanity.safeMessageCheck(message);
        Sanity.truthiness(target.indexOf(' ') == -1, "Target cannot have spaces");
        this.sendRawLine("PRIVMSG " + target + " :" + CTCPUtil.toCTCP(message));
    }

    @Override
    public void sendCTCPReply(@Nonnull String target, @Nonnull String message) {
        Sanity.safeMessageCheck(target, "Target");
        Sanity.safeMessageCheck(message);
        Sanity.truthiness(target.indexOf(' ') == -1, "Target cannot have spaces");
        this.sendRawLine("NOTICE " + target + " :" + CTCPUtil.toCTCP(message));
    }

    @Override
    public void sendMessage(@Nonnull String target, @Nonnull String message) {
        Sanity.safeMessageCheck(target, "Target");
        Sanity.safeMessageCheck(message);
        Sanity.truthiness(target.indexOf(' ') == -1, "Target cannot have spaces");
        this.sendRawLine("PRIVMSG " + target + " :" + message);
    }

    @Override
    public void sendMultiLineMessage(@Nonnull String target, @Nonnull String message, @Nonnull Cutter cutter) {
        Sanity.nullCheck(target, "Target cannot be null");
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.nullCheck(cutter, "Cutter cannot be null");
        cutter.split(message, this.getRemainingLength("PRIVMSG", target)).forEach(line -> this.sendMessage(target, line));
    }

    @Override
    public void sendMultiLineNotice(@Nonnull String target, @Nonnull String message, @Nonnull Cutter cutter) {
        Sanity.nullCheck(target, "Target cannot be null");
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.nullCheck(cutter, "Cutter cannot be null");
        cutter.split(message, this.getRemainingLength("NOTICE", target)).forEach(line -> this.sendNotice(target, line));
    }

    private int getRemainingLength(@Nonnull String type, @Nonnull String target) {
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
    public void sendNotice(@Nonnull String target, @Nonnull String message) {
        Sanity.safeMessageCheck(target, "Target");
        Sanity.safeMessageCheck(message);
        Sanity.truthiness(target.indexOf(' ') == -1, "Target cannot have spaces");
        this.sendRawLine("NOTICE " + target + " :" + message);
    }

    @Override
    public void sendRawLine(@Nonnull String message) {
        this.sendRawLine(message, false, false);
    }

    @Override
    public void sendRawLineAvoidingDuplication(@Nonnull String message) {
        this.sendRawLine(message, false, true);
    }

    @Override
    public void sendRawLineImmediately(@Nonnull String message) {
        this.sendRawLine(message, true, false);
    }

    private void sendRawLine(@Nonnull String message, boolean priority, boolean avoidDuplicates) {
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
    public void setDefaultMessageMap(@Nonnull DefaultMessageMap defaults) {
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
    public void setMessageCutter(@Nonnull Cutter cutter) {
        this.messageCutter = Sanity.nullCheck(cutter, "Cutter cannot be null");
    }

    @Override
    public void setMessageSendingQueueSupplier(@Nonnull Function<Client, ? extends MessageSendingQueue> supplier) {
        this.config.set(Config.MESSAGE_DELAY, Sanity.nullCheck(supplier, "Supplier cannot be null"));
        synchronized (this.messageSendingLock) {
            MessageSendingQueue newQueue = this.getMessageSendingQueueSupplier().apply(this);
            this.messageSendingScheduled.shutdown().forEach(newQueue::queue);
            Optional<Consumer<String>> consumer = this.messageSendingScheduled.getConsumer();
            this.messageSendingScheduled = newQueue;
            consumer.ifPresent(con -> this.messageSendingScheduled.beginSending(con));
        }
    }

    @Override
    public void setNick(@Nonnull String nick) {
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
    @Nonnull
    public String toString() {
        return new ToStringer(this).add("name", this.getName()).add("server", this.getConfig().getNotNull(Config.SERVER_ADDRESS)).toString();
    }

    /**
     * Queue up a line for processing.
     *
     * @param line line to be processed
     */
    @Override
    void processLine(@Nonnull String line) {
        if (line.startsWith("PING ")) {
            this.sendRawLineImmediately("PONG " + line.substring(5));
        } else if (!line.isEmpty()) {
            this.processor.queue(line);
        }
    }

    @Nonnull
    @Override
    ActorProvider getActorProvider() {
        return this.actorProvider;
    }

    @Nonnull
    @Override
    Config getConfig() {
        return this.config;
    }

    @Nonnull
    @Override
    Listener<String> getInputListener() {
        return this.inputListener;
    }

    @Nonnull
    @Override
    Set<String> getIntendedChannels() {
        return this.channelsIntended;
    }

    @Nonnull
    @Override
    Listener<String> getOutputListener() {
        return this.outputListener;
    }

    @Nonnull
    @Override
    String getRequestedNick() {
        return this.requestedNick;
    }

    @Override
    public void connect() {
        if (this.connection != null) {
            throw new IllegalStateException("Client is already connecting");
        }

        this.connection = NettyManager.connect(this);
        this.processor.queue("");

        this.sendRawLineImmediately("CAP LS 302");

        // If we have WebIRC information, send it before PASS, USER, and NICK.
        if (this.config.get(Config.WEBIRC_PASSWORD) != null) {
            this.sendRawLineImmediately("WEBIRC " + this.config.get(Config.WEBIRC_PASSWORD) + ' ' + this.config.get(Config.WEBIRC_USER) + ' ' + this.config.get(Config.WEBIRC_HOST) + ' ' + this.config.getNotNull(Config.WEBIRC_IP).getHostAddress());
        }

        // If the server has a password, send that along before USER and NICK.
        String password = this.config.get(Config.SERVER_PASSWORD);
        if (password != null) {
            this.sendRawLineImmediately("PASS " + (password.contains(" ") ? ":" : "") + password);
        }

        // Initial USER and NICK messages. Let's just assume we want +iw (send 8)
        this.sendRawLineImmediately("USER " + this.config.get(Config.USER) + " 8 * :" + this.config.get(Config.REAL_NAME));
        this.sendNickChange(this.goalNick);
    }

    @Override
    void beginMessageSendingImmediate(@Nonnull Consumer<String> consumer) {
        synchronized (this.messageSendingLock) {
            this.messageSendingImmediate.beginSending(consumer);
        }
    }

    @Override
    void beginMessageSendingScheduled(@Nonnull Consumer<String> consumer) {
        synchronized (this.messageSendingLock) {
            this.messageSendingScheduled.beginSending(consumer);
        }
    }

    @Override
    void pauseMessageSending() {
        synchronized (this.messageSendingLock) {
            this.messageSendingImmediate.pause();
            this.messageSendingScheduled.pause();
        }
    }

    @Override
    void ping() {
        this.sendRawLine("PING " + this.pingPurr[this.pingPurrCount++ % this.pingPurr.length]); // Connection's asleep, post cat sounds
    }

    @Override
    void resetServerInfo() {
        this.serverInfo = new IRCServerInfo(this);
    }

    @Override
    void sendNickChange(@Nonnull String newNick) {
        this.requestedNick = newNick;
        this.sendRawLineImmediately("NICK " + newNick);
    }

    @Override
    void setCurrentNick(@Nonnull String nick) {
        this.currentNick = nick;
    }

    @Override
    void setUserModes(@Nonnull ModeStatusList<UserMode> userModes) {
        this.userModes = new HashMap<>(userModes.getStatuses().stream().collect(Collectors.toMap(modeStatus -> modeStatus.getMode().getChar(), Function.identity())));
    }

    @Override
    void startSending() {
        this.connection.startSending();
        synchronized (this.messageSendingLock) {
            this.messageSendingScheduled.beginSending(this.messageSendingImmediate::queue);
        }
    }

    @Override
    void updateUserModes(@Nonnull ModeStatusList<UserMode> userModes) {
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
    void reconnect() {
        this.connection.shutdown(DefaultMessageType.RECONNECT, true);
    }

    @Override
    boolean isSSL() {
        return this.config.getNotNull(Config.SSL);
    }

    private void handleLine(@Nonnull final String line) {
        if (line.isEmpty()) {
            this.actorProvider.reset();
            this.capabilityManager.reset();
            this.serverInfo.reset();
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
            tags = this.messageTagManager.getTags(tagSection.substring(1));
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
        final ActorProvider.IRCActor actor = this.actorProvider.getActor(actorName);

        String commandString = null;
        List<String> args = new ArrayList<>();

        free:
        while ((next = line.indexOf(' ', position)) != -1) {
            if (line.charAt(position) == ':') {
                position++;
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
            String bit = line.substring((line.charAt(position) == ':') ? (position + 1) : position, line.length());
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
            this.eventManager.callEvent(new ClientReceiveNumericEvent(this, new DefaultServerMessage.NumericCommand(numeric, line, tags), actor.snapshot(), commandString, numeric, args));
        } catch (NumberFormatException exception) {
            this.eventManager.callEvent(new ClientReceiveCommandEvent(this, new DefaultServerMessage.StringCommand(commandString, line, tags), actor.snapshot(), commandString, args));
        }
    }

    @Nonnull
    @Override
    public Commands commands() {
        return this.commands;
    }
}
