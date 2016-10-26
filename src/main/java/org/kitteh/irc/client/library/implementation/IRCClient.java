/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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
import org.kitteh.irc.client.library.element.mode.ModeStatus;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.exception.KittehAuthorNagException;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.exception.KittehServerMessageTagException;
import org.kitteh.irc.client.library.feature.AuthManager;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.feature.sts.STSMachine;
import org.kitteh.irc.client.library.util.CISet;
import org.kitteh.irc.client.library.util.Cutter;
import org.kitteh.irc.client.library.util.Pair;
import org.kitteh.irc.client.library.util.QueueProcessingThread;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.StringUtil;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            super("Kitteh IRC Client Input Processor (" + IRCClient.this.getName() + ')');
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

    private final AuthManager authManager = new ManagerAuth(this);
    private final ManagerCapability capabilityManager = new ManagerCapability(this);
    private final EventManager eventManager = new ManagerEvent(this);
    private final ManagerISupport iSupportManager = new ManagerISupport(this);
    private final ManagerMessageTag messageTagManager = new ManagerMessageTag(this);

    private final Listener<Exception> exceptionListener;
    private final Listener<String> inputListener;
    private final Listener<String> outputListener;

    private final ActorProvider actorProvider = new ActorProvider(this);

    private Map<Character, ModeStatus<UserMode>> userModes;
    private STSMachine stsMachine;
    private boolean usingSts = false;

    private final ClientCommands commands = new ClientCommands();

    IRCClient(@Nonnull Config config) {
        this.config = config;
        if (this.config.get(Config.STS_STORAGE_MANAGER) != null) {
            this.configureSts();
        } else if (!this.config.getNotNull(Config.SSL)) {
            throw new KittehAuthorNagException(
                    "Connection is insecure. If the server does not support SSL, consider enabling STS support to " +
                    "facilitate automatic SSL upgrades when it does."
            );
        }

        this.currentNick = this.requestedNick = this.goalNick = this.config.get(Config.NICK);

        final String name = this.config.getNotNull(Config.NAME);

        Config.ExceptionConsumerWrapper exceptionListenerWrapper = this.config.get(Config.LISTENER_EXCEPTION);
        this.exceptionListener = new Listener<>(name, (exceptionListenerWrapper == null) ? null : exceptionListenerWrapper.getConsumer());
        Config.StringConsumerWrapper inputListenerWrapper = this.config.get(Config.LISTENER_INPUT);
        this.inputListener = new Listener<>(name, (inputListenerWrapper == null) ? null : inputListenerWrapper.getConsumer());
        Config.StringConsumerWrapper outputListenerWrapper = this.config.get(Config.LISTENER_OUTPUT);
        this.outputListener = new Listener<>(name, (outputListenerWrapper == null) ? null : outputListenerWrapper.getConsumer());

        this.processor = new InputProcessor();
        this.eventManager.registerEventListener(new EventListener(this));
    }

    private void configureSts() {
        this.stsMachine = new MemorySTSMachine(this.config.get(Config.STS_STORAGE_MANAGER), this);
        this.eventManager.registerEventListener(new STSHandler(this.stsMachine, this));
        this.usingSts = true;
    }

    @Override
    public void addChannel(@Nonnull String... channels) {
        Sanity.nullCheck(channels, "Channels cannot be null");
        Sanity.truthiness(channels.length > 0, "Channels cannot be empty array");
        for (String channelName : channels) {
            if (!this.serverInfo.isValidChannel(channelName)) {
                continue;
            }
            this.channelsIntended.add(channelName);
            this.sendRawLine("JOIN :" + channelName);
        }
    }

    @Override
    public void addKeyProtectedChannel(@Nonnull String channel, @Nonnull String key) {
        Sanity.nullCheck(channel, "Channel cannot be null");
        Sanity.nullCheck(key, "Key cannot be null");
        Sanity.truthiness(this.serverInfo.isValidChannel(channel), "Invalid channel name");
        this.channelsIntended.add(channel);
        this.sendRawLine("JOIN :" + channel + ' ' + key);
    }

    /**
     * Adds key-protected channels to this client.
     * <p>
     * Joins the channels if already connected.
     *
     * @param channelsAndKeys pairs of channel, key
     */
    @Override
    public void addKeyProtectedChannel(@Nonnull Pair<String, String>... channelsAndKeys) {
        Sanity.nullCheck(channelsAndKeys, "Channel/key pairs cannot be null");
        Sanity.truthiness(channelsAndKeys.length > 0, "Channel/key pairs cannot be empty array");
        for (Pair<String, String> channelAndKey : channelsAndKeys) {
            if ((channelAndKey.getLeft() == null) || !this.serverInfo.isValidChannel(channelAndKey.getLeft())) {
                continue;
            }
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
    public ManagerCapability getCapabilityManager() {
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
    public EventManager getEventManager() {
        return this.eventManager;
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

    @Override
    public int getMessageDelay() {
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
        Sanity.nullCheck(channelName, "Channel cannot be null");
        this.sendRawLine("KNOCK " + channelName);
    }

    @Override
    public void removeChannel(@Nonnull String channelName) {
        this.removeChannelPlease(channelName, null);
    }

    @Override
    public void removeChannel(@Nonnull String channelName, @Nonnull String reason) {
        Sanity.nullCheck(reason, "Reason cannot be null");
        this.removeChannelPlease(channelName, reason);
    }

    private void removeChannelPlease(@Nonnull String channelName, @Nullable String reason) {
        Sanity.nullCheck(channelName, "Channel cannot be null");
        ActorProvider.IRCChannel channel = this.actorProvider.getChannel(channelName);
        if (channel != null) {
            if (reason != null) {
                Sanity.safeMessageCheck(reason, "Part reason");
            }
            String name = channel.getName();
            this.channelsIntended.remove(name);
            if (this.actorProvider.getTrackedChannelNames().contains(channel.getName())) {
                this.sendRawLine("PART " + name + (reason != null ? (" :" + reason) : ""));
            }
        }
    }

    @Override
    public void sendCTCPMessage(@Nonnull String target, @Nonnull String message) {
        Sanity.safeMessageCheck(target, "Target");
        Sanity.safeMessageCheck(message);
        Sanity.truthiness(target.indexOf(' ') == -1, "Target cannot have spaces");
        this.sendRawLine("PRIVMSG " + target + " :" + CTCPUtil.toCTCP(message));
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
        cutter.split(message, this.getRemainingLength("PRIVMSG", target)).forEach(line -> this.sendMessage(target, line));
    }

    @Override
    public void sendMultiLineNotice(@Nonnull String target, @Nonnull String message, @Nonnull Cutter cutter) {
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
        this.sendRawLineCheck(message);
        this.connection.sendMessage(message, false);
    }

    @Override
    public void sendRawLineAvoidingDuplication(@Nonnull String message) {
        this.sendRawLineCheck(message);
        this.connection.sendMessage(message, false, true);
    }

    @Override
    public void sendRawLineImmediately(@Nonnull String message) {
        this.sendRawLineCheck(message);
        this.connection.sendMessage(message, true);
    }

    private void sendRawLineCheck(@Nonnull String message) {
        Sanity.safeMessageCheck(message);
        if (!message.isEmpty() && (message.length() > ((message.charAt(0) == '@') ? 1022 : 510))) {
            throw new IllegalArgumentException("Message too long: " + message.length());
        }
        if (this.connection == null) {
            throw new IllegalStateException("Cannot send messages prior to connection");
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
    public void setMessageDelay(int delay) {
        Sanity.truthiness(delay >= 0, "Delay cannot be negative");
        this.config.set(Config.MESSAGE_DELAY, delay);
        if (this.connection != null) {
            this.connection.updateScheduling();
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
        this.shutdownInternal(null);
    }

    @Override
    public void shutdown(@Nonnull String reason) {
        Sanity.safeMessageCheck(reason, "Quit reason");
        this.shutdownInternal(reason);
    }

    private void shutdownInternal(@Nullable String reason) {
        this.processor.interrupt();

        if (this.connection != null) { // In case shutdown is called while building.
            this.connection.shutdown(reason);
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
    Listener<Exception> getExceptionListener() {
        return this.exceptionListener;
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
    void connect() {
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
    }

    @Override
    public void updateUserModes(@Nonnull ModeStatusList<UserMode> userModes) {
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

    private List<String> handleArgs(@Nonnull String[] split, int start) {
        final List<String> argsList = new ArrayList<>();

        int index = start;
        for (; index < split.length; index++) {
            if (split[index].startsWith(":")) {
                split[index] = split[index].substring(1);
                argsList.add(StringUtil.combineSplit(split, index));
                break;
            }
            argsList.add(split[index]);
        }

        return argsList;
    }

    private void handleLine(@Nonnull final String line) {
        if (line.isEmpty()) {
            this.actorProvider.reset();
            this.capabilityManager.reset();
            this.serverInfo.reset();
        }

        final String[] split = line.split(" ");

        int index = 0;

        List<MessageTag> tags;
        if (split[index].startsWith("@")) {
            if (split.length <= index) {
                throw new KittehServerMessageException(new IRCServerMessage(line, new ArrayList<>()), "Server sent a message without a command");
            }
            String tagSection = split[index];
            if (tagSection.length() < 2) {
                throw new KittehServerMessageTagException(line, "Server sent an empty tag section");
            }
            tags = this.messageTagManager.getTags(tagSection.substring(1));
            index++;
        } else {
            tags = Collections.unmodifiableList(new ArrayList<>());
        }

        final String actorName;
        if (split[index].startsWith(":")) {
            actorName = split[index].substring(1);
            index++;
        } else {
            actorName = "";
        }
        final ActorProvider.IRCActor actor = this.actorProvider.getActor(actorName);

        if (split.length <= index) {
            throw new KittehServerMessageException(new IRCServerMessage(line, tags), "Server sent a message without a command");
        }

        final String commandString = split[index++];

        final List<String> args = this.handleArgs(split, index);

        try {
            int numeric = Integer.parseInt(commandString);
            this.eventManager.callEvent(new ClientReceiveNumericEvent(this, new IRCServerMessage.IRCNumericCommandServerMessage(numeric, line, tags), actor.snapshot(), commandString, numeric, args));
        } catch (NumberFormatException exception) {
            this.eventManager.callEvent(new ClientReceiveCommandEvent(this, new IRCServerMessage.IRCStringCommandServerMessage(commandString, line, tags), actor.snapshot(), commandString, args));
        }
    }

    @Nonnull
    @Override
    public Commands commands() {
        return this.commands;
    }
}
