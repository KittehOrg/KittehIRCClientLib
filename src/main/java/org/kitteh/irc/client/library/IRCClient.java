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

import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.References;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.kitteh.irc.client.library.element.MessageReceiver;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.abstractbase.CapabilityNegotiationResponseEventBase;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesAcknowledgedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesListEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesRejectedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.event.channel.ChannelCTCPEvent;
import org.kitteh.irc.client.library.event.channel.ChannelInviteEvent;
import org.kitteh.irc.client.library.event.channel.ChannelJoinEvent;
import org.kitteh.irc.client.library.event.channel.ChannelKickEvent;
import org.kitteh.irc.client.library.event.channel.ChannelKnockEvent;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.event.channel.ChannelModeEvent;
import org.kitteh.irc.client.library.event.channel.ChannelNamesUpdatedEvent;
import org.kitteh.irc.client.library.event.channel.ChannelNoticeEvent;
import org.kitteh.irc.client.library.event.channel.ChannelPartEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTargetedCTCPEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTargetedMessageEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTargetedNoticeEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTopicEvent;
import org.kitteh.irc.client.library.event.channel.ChannelUsersUpdatedEvent;
import org.kitteh.irc.client.library.event.client.ClientConnectedEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.client.NickRejectedEvent;
import org.kitteh.irc.client.library.event.user.PrivateCTCPQueryEvent;
import org.kitteh.irc.client.library.event.user.PrivateCTCPReplyEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;
import org.kitteh.irc.client.library.event.user.PrivateNoticeEvent;
import org.kitteh.irc.client.library.event.user.UserNickChangeEvent;
import org.kitteh.irc.client.library.event.user.UserQuitEvent;
import org.kitteh.irc.client.library.exception.KittehISupportProcessingFailureException;
import org.kitteh.irc.client.library.util.CISet;
import org.kitteh.irc.client.library.util.CommandFilter;
import org.kitteh.irc.client.library.util.NumericFilter;
import org.kitteh.irc.client.library.util.QueueProcessingThread;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class IRCClient extends InternalClient {
    private final class InputProcessor extends QueueProcessingThread<String> {
        private InputProcessor() {
            super("Kitteh IRC Client Input Processor (" + IRCClient.this.getName() + ')');
        }

        @Override
        protected void processElement(@Nullable String element) {
            try {
                IRCClient.this.handleLine(element);
            } catch (final Exception thrown) {
                IRCClient.this.exceptionListener.queue(thrown);
            } catch (final Throwable ignored) {
                // TODO do something!
            }
        }
    }

    private enum ISupport {
        CASEMAPPING {
            @Override
            boolean process(@Nonnull String value, @Nonnull IRCClient client) {
                CaseMapping caseMapping = CaseMapping.getByName(value);
                if (caseMapping != null) {
                    client.serverInfo.setCaseMapping(caseMapping);
                    return true;
                }
                return false;
            }
        },
        CHANNELLEN {
            @Override
            boolean process(@Nonnull String value, @Nonnull IRCClient client) {
                try {
                    client.serverInfo.setChannelLengthLimit(Integer.parseInt(value));
                    return true;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        },
        CHANLIMIT {
            @Override
            boolean process(@Nonnull String value, @Nonnull IRCClient client) {
                String[] pairs = value.split(",");
                Map<Character, Integer> limits = new HashMap<>();
                for (String p : pairs) {
                    String[] pair = p.split(":");
                    if (pair.length != 2) {
                        return false;
                    }
                    int limit;
                    try {
                        limit = Integer.parseInt(pair[1]);
                    } catch (Exception e) {
                        return false;
                    }
                    for (char prefix : pair[0].toCharArray()) {
                        limits.put(prefix, limit);
                    }
                }
                if (limits.isEmpty()) {
                    return false;
                }
                client.serverInfo.setChannelLimits(limits);
                return true;
            }
        },
        CHANMODES {
            @Override
            boolean process(@Nonnull String value, @Nonnull IRCClient client) {
                String[] modes = value.split(",");
                Map<Character, ChannelModeType> modesMap = new ConcurrentHashMap<>();
                for (int typeId = 0; typeId < modes.length; typeId++) {
                    for (char mode : modes[typeId].toCharArray()) {
                        ChannelModeType type = null;
                        switch (typeId) {
                            case 0:
                                type = ChannelModeType.A_MASK;
                                break;
                            case 1:
                                type = ChannelModeType.B_PARAMETER_ALWAYS;
                                break;
                            case 2:
                                type = ChannelModeType.C_PARAMETER_ON_SET;
                                break;
                            case 3:
                                type = ChannelModeType.D_PARAMETER_NEVER;
                        }
                        modesMap.put(mode, type);
                    }
                }
                client.serverInfo.setChannelModes(modesMap);
                return true;
            }
        },
        CHANTYPES {
            @Override
            boolean process(@Nonnull String value, @Nonnull IRCClient client) {
                if (value.isEmpty()) {
                    return false;
                }
                List<Character> prefixes = new ArrayList<>();
                for (char c : value.toCharArray()) {
                    prefixes.add(c);
                }
                client.serverInfo.setChannelPrefixes(prefixes);
                return true;
            }
        },
        NETWORK {
            @Override
            boolean process(@Nonnull String value, @Nonnull IRCClient client) {
                client.serverInfo.setNetworkName(value);
                return true;
            }
        },
        NICKLEN {
            @Override
            boolean process(@Nonnull String value, @Nonnull IRCClient client) {
                try {
                    client.serverInfo.setNickLengthLimit(Integer.parseInt(value));
                    return true;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        },
        PREFIX {
            private final Pattern PATTERN = Pattern.compile("\\(([a-zA-Z]+)\\)([^ ]+)");

            @Override
            boolean process(@Nonnull String value, @Nonnull IRCClient client) {
                Matcher matcher = this.PATTERN.matcher(value);
                if (!matcher.find()) {
                    return false;
                }
                String modes = matcher.group(1);
                String display = matcher.group(2);
                if (modes.length() == display.length()) {
                    List<ChannelUserMode> prefixList = new ArrayList<>();
                    for (int index = 0; index < modes.length(); index++) {
                        prefixList.add(new ActorProvider.IRCChannelUserMode(client, modes.charAt(index), display.charAt(index)));
                    }
                    client.serverInfo.setChannelUserModes(prefixList);
                }
                return true;
            }
        },
        WHOX {
            @Override
            boolean process(@Nonnull String value, @Nonnull IRCClient client) {
                client.serverInfo.setWhoXSupport();
                return true;
            }
        };

        private static final Map<String, ISupport> MAP;
        private static final Pattern PATTERN = Pattern.compile("([A-Z0-9]+)=(.*)");

        static {
            MAP = new ConcurrentHashMap<>();
            for (ISupport iSupport : ISupport.values()) {
                MAP.put(iSupport.name(), iSupport);
            }
        }

        private static void handle(@Nonnull String arg, @Nonnull IRCClient client) {
            Matcher matcher = PATTERN.matcher(arg);
            if (!matcher.find()) {
                return;
            }
            ISupport iSupport = MAP.get(matcher.group(1));
            if (iSupport != null) {
                boolean failure = !iSupport.process(matcher.group(2), client);
                if (failure) {
                    client.exceptionListener.queue(new KittehISupportProcessingFailureException(arg));
                }
            }
        }

        abstract boolean process(@Nonnull String value, @Nonnull IRCClient client);
    }

    private enum MessageTarget {
        CHANNEL,
        CHANNEL_TARGETED,
        PRIVATE,
        UNKNOWN
    }

    private final String[] pingPurr = new String[]{"MEOW", "MEOW!", "PURR", "PURRRRRRR"};
    private int pingPurrCount;

    private final Config config;
    private final InputProcessor processor;
    private IRCServerInfo serverInfo = new IRCServerInfo(this);

    private String goalNick;
    private String currentNick;
    private String requestedNick;

    private final Set<String> channels = new CISet(this);
    private final Set<String> channelsIntended = new CISet(this);

    private NettyManager.ClientConnection connection;

    private final AuthManager authManager = new AuthManager(this);
    private final CapabilityManager capabilityManager = new CapabilityManager(this);
    private final EventManager eventManager = new EventManager(this);

    private final Listener<Exception> exceptionListener;
    private final Listener<String> inputListener;
    private final Listener<String> outputListener;

    private final ActorProvider actorProvider = new ActorProvider(this);

    IRCClient(@Nonnull Config config) {
        this.config = config;
        this.currentNick = this.requestedNick = this.goalNick = this.config.get(Config.NICK);

        final String name = this.config.get(Config.NAME);

        Config.ExceptionConsumerWrapper exceptionListenerWrapper = this.config.get(Config.LISTENER_EXCEPTION);
        this.exceptionListener = new Listener<>(name, (exceptionListenerWrapper == null) ? null : exceptionListenerWrapper.getConsumer());
        Config.StringConsumerWrapper inputListenerWrapper = this.config.get(Config.LISTENER_INPUT);
        this.inputListener = new Listener<>(name, (inputListenerWrapper == null) ? null : inputListenerWrapper.getConsumer());
        Config.StringConsumerWrapper outputListenerWrapper = this.config.get(Config.LISTENER_OUTPUT);
        this.outputListener = new Listener<>(name, (outputListenerWrapper == null) ? null : outputListenerWrapper.getConsumer());

        this.processor = new InputProcessor();
        EventListener eventListener = new EventListener();
        this.eventManager.registerEventListener(eventListener);
        this.connect();
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
    public void addChannel(@Nonnull Channel... channels) {
        Sanity.nullCheck(channels, "Channels cannot be null");
        Sanity.truthiness(channels.length > 0, "Channels cannot be empty array");
        for (Channel channel : channels) {
            if (channel.getClient().equals(this) && (channel instanceof ActorProvider.IRCChannel)) {
                this.channelsIntended.add(channel.getName());
                this.sendRawLine("JOIN :" + channel.getName());
            }
        }
    }

    @Override
    @Nonnull
    public AuthManager getAuthManager() {
        return this.authManager;
    }

    @Override
    @Nonnull
    public CapabilityManager getCapabilityManager() {
        return this.capabilityManager;
    }

    @Override
    @Nullable
    public Channel getChannel(@Nonnull String name) {
        Sanity.nullCheck(name, "Channel name cannot be null");
        ActorProvider.IRCChannel channel = this.actorProvider.getChannel(name);
        return (channel == null) ? null : channel.snapshot();
    }

    @Nonnull
    @Override
    public Set<Channel> getChannels() {
        return this.channels.stream().map(this.actorProvider::getChannel).map(ActorProvider.IRCChannel::snapshot).collect(Collectors.toSet());
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

    @Override
    public int getMessageDelay() {
        return this.config.getNotNull(Config.MESSAGE_DELAY);
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

    @Override
    public void removeChannel(@Nonnull String channelName, @Nullable String reason) {
        Sanity.nullCheck(channelName, "Channel cannot be null");
        ActorProvider.IRCChannel channel = this.actorProvider.getChannel(channelName);
        if (channel != null) {
            this.removeChannel(channel.snapshot(), reason);
        }
    }

    @Override
    public void removeChannel(@Nonnull Channel channel, @Nullable String reason) {
        Sanity.nullCheck(channel, "Channel cannot be null");
        if (reason != null) {
            Sanity.safeMessageCheck(reason, "part reason");
        }
        String channelName = channel.getName();
        this.channelsIntended.remove(channelName);
        if (this.channels.contains(channel.getName())) {
            this.sendRawLine("PART " + channelName + (reason == null ? "" : " :" + reason));
        }
    }

    @Override
    public void sendCTCPMessage(@Nonnull String target, @Nonnull String message) {
        Sanity.nullCheck(target, "Target cannot be null");
        Sanity.safeMessageCheck(message, "target");
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.safeMessageCheck(message);
        Sanity.truthiness(target.indexOf(' ') == -1, "Target cannot have spaces");
        this.sendRawLine("PRIVMSG " + target + " :" + CTCPUtil.toCTCP(message));
    }

    @Override
    public void sendCTCPMessage(@Nonnull MessageReceiver target, @Nonnull String message) {
        Sanity.nullCheck(target, "Target cannot be null");
        this.sendCTCPMessage(target.getMessagingName(), message);
    }

    @Override
    public void sendMessage(@Nonnull String target, @Nonnull String message) {
        Sanity.nullCheck(target, "Target cannot be null");
        Sanity.safeMessageCheck(message, "target");
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.safeMessageCheck(message);
        Sanity.truthiness(target.indexOf(' ') == -1, "Target cannot have spaces");
        this.sendRawLine("PRIVMSG " + target + " :" + message);
    }

    @Override
    public void sendMessage(@Nonnull MessageReceiver target, @Nonnull String message) {
        Sanity.nullCheck(target, "Target cannot be null");
        this.sendMessage(target.getMessagingName(), message);
    }

    @Override
    public void sendNotice(@Nonnull String target, @Nonnull String message) {
        Sanity.nullCheck(target, "Target cannot be null");
        Sanity.safeMessageCheck(message, "target");
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.safeMessageCheck(message);
        Sanity.truthiness(target.indexOf(' ') == -1, "Target cannot have spaces");
        this.sendRawLine("NOTICE " + target + " :" + message);
    }

    @Override
    public void sendNotice(@Nonnull MessageReceiver target, @Nonnull String message) {
        Sanity.nullCheck(target, "Target cannot be null");
        this.sendNotice(target.getMessagingName(), message);
    }

    @Override
    public void sendRawLine(@Nonnull String message) {
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.safeMessageCheck(message);
        this.connection.sendMessage(message, false);
    }

    @Override
    public void sendRawLineAvoidingDuplication(@Nonnull String message) {
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.safeMessageCheck(message);
        this.connection.sendMessage(message, false, true);
    }

    @Override
    public void sendRawLineImmediately(@Nonnull String message) {
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.safeMessageCheck(message);
        this.connection.sendMessage(message, true);
    }

    @Override
    public void setExceptionListener(@Nullable Consumer<Exception> listener) {
        this.exceptionListener.setConsumer(listener);
    }

    @Override
    public void setInputListener(@Nullable Consumer<String> listener) {
        this.inputListener.setConsumer(listener);
    }

    @Override
    public void setMessageDelay(int delay) {
        Sanity.truthiness(delay > 0, "Delay must be at least 1");
        this.config.set(Config.MESSAGE_DELAY, delay);
        if (this.connection != null) {
            this.connection.updateScheduling();
        }
    }

    @Override
    public void setNick(@Nonnull String nick) {
        Sanity.nullCheck(nick, "Nick cannot be null");
        Sanity.safeMessageCheck(nick, "nick");
        this.goalNick = nick.trim();
        this.sendNickChange(this.goalNick);
    }

    @Override
    public void setOutputListener(@Nullable Consumer<String> listener) {
        this.outputListener.setConsumer(listener);
    }

    @Override
    public void shutdown(@Nullable String reason) {
        if (reason != null) {
            Sanity.safeMessageCheck(reason, "quit reason");
        }
        this.processor.interrupt();

        this.connection.shutdown(((reason != null) && reason.isEmpty()) ? null : reason);

        // Shut these down last, so they get any last firings
        this.exceptionListener.shutdown();
        this.inputListener.shutdown();
        this.outputListener.shutdown();
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
        } else {
            this.processor.queue(line);
        }
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
    Listener<String> getOutputListener() {
        return this.outputListener;
    }

    @Override
    void connect() {
        this.connection = NettyManager.connect(this);

        this.sendRawLineImmediately("CAP LS");

        // If we have WebIRC information, send it before PASS, USER, and NICK.
        if (this.config.get(Config.WEBIRC_PASSWORD) != null) {
            this.sendRawLineImmediately("WEBIRC " + this.config.get(Config.WEBIRC_PASSWORD) + ' ' + this.config.get(Config.WEBIRC_USER) + ' ' + this.config.get(Config.WEBIRC_HOST) + ' ' + this.config.getNotNull(Config.WEBIRC_IP).getHostAddress());
        }

        // If the server has a password, send that along before USER and NICK.
        if (this.config.get(Config.SERVER_PASSWORD) != null) {
            this.sendRawLineImmediately("PASS " + this.config.get(Config.SERVER_PASSWORD));
        }

        // Initial USER and NICK messages. Let's just assume we want +iw (send 8)
        this.sendRawLineImmediately("USER " + this.config.get(Config.USER) + " 8 * :" + this.config.get(Config.REAL_NAME));
        this.sendNickChange(this.goalNick);
    }

    @Override
    void ping() {
        this.sendRawLine("PING :" + this.pingPurr[this.pingPurrCount++ % this.pingPurr.length]); // Connection's asleep, post cat sounds
    }

    private String[] handleArgs(@Nonnull String[] split, int start) {
        final List<String> argsList = new LinkedList<>();

        int index = start;
        for (; index < split.length; index++) {
            if (split[index].startsWith(":")) {
                split[index] = split[index].substring(1);
                argsList.add(StringUtil.combineSplit(split, index));
                break;
            }
            argsList.add(split[index]);
        }

        return argsList.toArray(new String[argsList.size()]);
    }

    private void handleLine(@Nullable final String line) {
        if ((line == null) || (line.isEmpty())) {
            return;
        }

        final String[] split = line.split(" ");

        int argsIndex = 1;

        final String actorName;
        if (split[0].startsWith(":")) {
            argsIndex++;
            actorName = split[0].substring(1);
        } else {
            actorName = "";
        }
        final ActorProvider.IRCActor actor = this.actorProvider.getActor(actorName);

        final String commandString = split[argsIndex - 1];

        final String[] args = this.handleArgs(split, argsIndex);

        try {
            int numeric = Integer.parseInt(commandString);
            this.eventManager.callEvent(new ClientReceiveNumericEvent(this, actor.snapshot(), numeric, args));
        } catch (NumberFormatException exception) {
            this.eventManager.callEvent(new ClientReceiveCommandEvent(this, actor.snapshot(), commandString, args));
        }
    }

    @net.engio.mbassy.listener.Listener(references = References.Strong)
    private class EventListener {
        @NumericFilter(1)
        @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void welcome(ClientReceiveNumericEvent event) {
            IRCClient.this.currentNick = event.getArgs()[0];
        }

        @NumericFilter(4)
        @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void version(ClientReceiveNumericEvent event) {
            try {
                IRCClient.this.authManager.authenticate();
            } catch (IllegalStateException | UnsupportedOperationException ignored) {
            }
            IRCClient.this.serverInfo = new IRCServerInfo(IRCClient.this);
            IRCClient.this.serverInfo.setServerAddress(event.getArgs()[1]);
            IRCClient.this.serverInfo.setServerVersion(event.getArgs()[2]);
            IRCClient.this.eventManager.callEvent(new ClientConnectedEvent(IRCClient.this, event.getServer(), IRCClient.this.serverInfo));
            IRCClient.this.connection.startSending();
        }

        @NumericFilter(5) // WHO completed
        @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void iSupport(ClientReceiveNumericEvent event) {
            for (String arg : event.getArgs()) {
                ISupport.handle(arg, IRCClient.this);
            }
        }

        @NumericFilter(352) // WHO
        @NumericFilter(354) // WHOX
        @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void who(ClientReceiveNumericEvent event) {
            if (IRCClient.this.serverInfo.isValidChannel(event.getArgs()[1])) {
                final String channelName = event.getArgs()[1];
                final String ident = event.getArgs()[2];
                final String host = event.getArgs()[3];
                final String server = event.getArgs()[4];
                final String nick = event.getArgs()[5];
                final ActorProvider.IRCUser user = (ActorProvider.IRCUser) IRCClient.this.actorProvider.getActor(nick + '!' + ident + '@' + host);
                user.setServer(server);
                final String status = event.getArgs()[6];
                String realName = null;
                switch (event.getNumeric()) {
                    case 352:
                        realName = event.getArgs()[7];
                        break;
                    case 354:
                        String account = event.getArgs()[7];
                        if ("0".equals(account)) {
                            account = null;
                        }
                        user.setAccount(account);
                        realName = event.getArgs()[8];
                        break;
                }
                user.setRealName(realName);
                final ActorProvider.IRCChannel channel = IRCClient.this.actorProvider.getChannel(channelName);
                final Set<ChannelUserMode> modes = new HashSet<>();
                for (char prefix : status.substring(1).toCharArray()) {
                    if (prefix == 'G') {
                        user.setAway(true);
                        continue;
                    }
                    for (ChannelUserMode mode : IRCClient.this.serverInfo.getChannelUserModes()) {
                        if (mode.getPrefix() == prefix) {
                            modes.add(mode);
                            break;
                        }
                    }
                }
                channel.trackUser(user, modes);
            }
        }

        @NumericFilter(315) // WHO completed
        @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void whoComplete(ClientReceiveNumericEvent event) {
            ActorProvider.IRCChannel whoChannel = IRCClient.this.actorProvider.getChannel(event.getArgs()[1]);
            if (whoChannel != null) {
                whoChannel.setListReceived();
                IRCClient.this.eventManager.callEvent(new ChannelUsersUpdatedEvent(IRCClient.this, whoChannel.snapshot()));
            }
        }

        @NumericFilter(332) // Topic
        @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void topic(ClientReceiveNumericEvent event) {
            ActorProvider.IRCChannel topicChannel = IRCClient.this.actorProvider.getChannel(event.getArgs()[1]);
            if (topicChannel != null) {
                topicChannel.setTopic(event.getArgs()[2]);
            }
        }

        @NumericFilter(333) // Topic info
        @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void topicInfo(ClientReceiveNumericEvent event) {
            ActorProvider.IRCChannel topicSetChannel = IRCClient.this.actorProvider.getChannel(event.getArgs()[1]);
            if (topicSetChannel != null) {
                topicSetChannel.setTopic(Long.parseLong(event.getArgs()[3]) * 1000, IRCClient.this.actorProvider.getActor(event.getArgs()[2]).snapshot());
                IRCClient.this.eventManager.callEvent(new ChannelTopicEvent(IRCClient.this, topicSetChannel.snapshot(), false));
            }
        }

        @NumericFilter(353) // NAMES
        @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void names(ClientReceiveNumericEvent event) {
            if (IRCClient.this.serverInfo.isValidChannel(event.getArgs()[2])) {
                ActorProvider.IRCChannel channel = IRCClient.this.actorProvider.getChannel(event.getArgs()[2]);
                List<ChannelUserMode> channelUserModes = IRCClient.this.serverInfo.getChannelUserModes();
                for (String combo : event.getArgs()[3].split(" ")) {
                    Set<ChannelUserMode> modes = new HashSet<>();
                    for (int i = 0; i < combo.length(); i++) {
                        char c = combo.charAt(i);
                        Optional<ChannelUserMode> mode = channelUserModes.stream().filter(m -> m.getPrefix() == c).findFirst();
                        if (mode.isPresent()) {
                            modes.add(mode.get());
                        } else {
                            channel.trackNick(combo.substring(i), modes);
                            break;
                        }
                    }
                }
            }
        }

        @NumericFilter(366) // End of NAMES
        @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void namesComplete(ClientReceiveNumericEvent event) {
            if (IRCClient.this.serverInfo.isValidChannel(event.getArgs()[1])) {
                ActorProvider.IRCChannel channel = IRCClient.this.actorProvider.getChannel(event.getArgs()[1]);
                IRCClient.this.eventManager.callEvent(new ChannelNamesUpdatedEvent(IRCClient.this, channel.snapshot()));
            }
        }

        @NumericFilter(431) // No nick given
        @NumericFilter(432) // Erroneous nickname
        @NumericFilter(433) // Nick in use
        @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void nickInUse(ClientReceiveNumericEvent event) {
            NickRejectedEvent nickRejectedEvent = new NickRejectedEvent(IRCClient.this, IRCClient.this.requestedNick, IRCClient.this.requestedNick + '`');
            IRCClient.this.eventManager.callEvent(nickRejectedEvent);
            IRCClient.this.sendNickChange(nickRejectedEvent.getNewNick());
        }

        @NumericFilter(710) // Knock
        @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void knock(ClientReceiveNumericEvent event) {
            ActorProvider.IRCChannel channel = IRCClient.this.actorProvider.getChannel(event.getArgs()[1]);
            ActorProvider.IRCUser user = (ActorProvider.IRCUser) IRCClient.this.actorProvider.getActor(event.getArgs()[2]);
            IRCClient.this.eventManager.callEvent(new ChannelKnockEvent(IRCClient.this, channel.snapshot(), user.snapshot()));
        }

        @CommandFilter("NOTICE")
        @CommandFilter("PRIVMSG")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void ctcp(ClientReceiveCommandEvent event) {
            if (CTCPUtil.isCTCP(event.getArgs()[1])) {
                final String ctcpMessage = CTCPUtil.fromCTCP(event.getArgs()[1]);
                final MessageTarget messageTarget = IRCClient.this.getTypeByTarget(event.getArgs()[0]);
                User user = (User) event.getActor();
                switch (event.getCommand()) {
                    case "NOTICE":
                        if (messageTarget == MessageTarget.PRIVATE) {
                            IRCClient.this.eventManager.callEvent(new PrivateCTCPReplyEvent(IRCClient.this, user, ctcpMessage));
                        }
                        break;
                    case "PRIVMSG":
                        switch (messageTarget) {
                            case PRIVATE:
                                String reply = null; // Message to send as CTCP reply (NOTICE). Send nothing if null.
                                switch (ctcpMessage) {
                                    case "VERSION":
                                        reply = "VERSION I am Kitteh!";
                                        break;
                                    case "TIME":
                                        reply = "TIME " + new Date().toString();
                                        break;
                                    case "FINGER":
                                        reply = "FINGER om nom nom tasty finger";
                                        break;
                                }
                                if (ctcpMessage.startsWith("PING ")) {
                                    reply = ctcpMessage;
                                }
                                PrivateCTCPQueryEvent ctcpEvent = new PrivateCTCPQueryEvent(IRCClient.this, user, ctcpMessage, reply);
                                IRCClient.this.eventManager.callEvent(ctcpEvent);
                                String eventReply = ctcpEvent.getReply();
                                if (eventReply != null) {
                                    IRCClient.this.sendRawLine("NOTICE " + user.getNick() + " :" + CTCPUtil.toCTCP(eventReply));
                                }
                                break;
                            case CHANNEL:
                                IRCClient.this.eventManager.callEvent(new ChannelCTCPEvent(IRCClient.this, user, IRCClient.this.actorProvider.getChannel(event.getArgs()[0]).snapshot(), ctcpMessage));
                                break;
                            case CHANNEL_TARGETED:
                                IRCClient.this.eventManager.callEvent(new ChannelTargetedCTCPEvent(IRCClient.this, user, IRCClient.this.actorProvider.getChannel(event.getArgs()[0].substring(1)).snapshot(), IRCClient.this.serverInfo.getTargetedChannelInfo(event.getArgs()[0]), ctcpMessage));
                                break;
                        }
                        break;
                }
            }
        }

        @CommandFilter("CAP")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void cap(ClientReceiveCommandEvent event) {
            CapabilityNegotiationResponseEventBase responseEvent = null;
            List<CapabilityState> capabilityStateList = Arrays.stream(event.getArgs()[2].split(" ")).map(CapabilityManager.IRCCapabilityState::new).collect(Collectors.toList());
            switch (event.getArgs()[1].toLowerCase()) {
                case "ack":
                    IRCClient.this.capabilityManager.updateCapabilities(capabilityStateList);
                    responseEvent = new CapabilitiesAcknowledgedEvent(IRCClient.this, IRCClient.this.capabilityManager.isNegotiating(), capabilityStateList);
                    IRCClient.this.eventManager.callEvent(responseEvent);
                    break;
                case "list":
                    IRCClient.this.capabilityManager.setCapabilities(capabilityStateList);
                    IRCClient.this.eventManager.callEvent(new CapabilitiesListEvent(IRCClient.this, capabilityStateList));
                    break;
                case "ls":
                    IRCClient.this.capabilityManager.setSupportedCapabilities(capabilityStateList);
                    responseEvent = new CapabilitiesSupportedListEvent(IRCClient.this, IRCClient.this.capabilityManager.isNegotiating(), capabilityStateList);
                    Set<String> capabilities = capabilityStateList.stream().map(CapabilityState::getCapabilityName).collect(Collectors.toCollection(HashSet::new));
                    capabilities.retainAll(Arrays.asList("account-notify", "away-notify", "extended-join", "multi-prefix"));
                    if (!capabilities.isEmpty()) { // TODO if too large, split across lines
                        IRCClient.this.sendRawLineImmediately("CAP REQ :" + StringUtil.combineSplit(capabilities.toArray(new String[capabilities.size()]), 0));
                    }
                    IRCClient.this.eventManager.callEvent(responseEvent);
                    break;
                case "nak":
                    IRCClient.this.capabilityManager.updateCapabilities(capabilityStateList);
                    responseEvent = new CapabilitiesRejectedEvent(IRCClient.this, IRCClient.this.capabilityManager.isNegotiating(), capabilityStateList);
                    IRCClient.this.eventManager.callEvent(responseEvent);
                    break;
            }
            if (responseEvent != null) {
                if (responseEvent.isNegotiating() && responseEvent.isEndingNegotiation()) {
                    IRCClient.this.sendRawLineImmediately("CAP END");
                    IRCClient.this.capabilityManager.endNegotiation();
                }
            }
        }

        @CommandFilter("ACCOUNT")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void account(ClientReceiveCommandEvent event) {
            String account = event.getArgs()[0];
            if ("*".equals(account)) {
                account = null;
            }
            IRCClient.this.actorProvider.trackUserAccount(((User) event.getActor()).getNick(), account);
        }

        @CommandFilter("AWAY")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void away(ClientReceiveCommandEvent event) {
            IRCClient.this.actorProvider.trackUserAway(((User) event.getActor()).getNick(), event.getArgs().length > 0);
        }

        @CommandFilter("NOTICE")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void notice(ClientReceiveCommandEvent event) {
            switch (IRCClient.this.getTypeByTarget(event.getArgs()[0])) {
                case CHANNEL:
                    IRCClient.this.eventManager.callEvent(new ChannelNoticeEvent(IRCClient.this, (User) event.getActor(), IRCClient.this.actorProvider.getChannel(event.getArgs()[0]).snapshot(), event.getArgs()[1]));
                    break;
                case CHANNEL_TARGETED:
                    IRCClient.this.eventManager.callEvent(new ChannelTargetedNoticeEvent(IRCClient.this, (User) event.getActor(), IRCClient.this.actorProvider.getChannel(event.getArgs()[0].substring(1)).snapshot(), IRCClient.this.serverInfo.getTargetedChannelInfo(event.getArgs()[0]), event.getArgs()[1]));
                    break;
                case PRIVATE:
                    IRCClient.this.eventManager.callEvent(new PrivateNoticeEvent(IRCClient.this, (User) event.getActor(), event.getArgs()[1]));
                    break;
            }
        }

        @CommandFilter("PRIVMSG")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void privmsg(ClientReceiveCommandEvent event) {
            if (CTCPUtil.isCTCP(event.getArgs()[1])) {
                return;
            }
            switch (IRCClient.this.getTypeByTarget(event.getArgs()[0])) {
                case CHANNEL:
                    IRCClient.this.eventManager.callEvent(new ChannelMessageEvent(IRCClient.this, (User) event.getActor(), IRCClient.this.actorProvider.getChannel(event.getArgs()[0]).snapshot(), event.getArgs()[1]));
                    break;
                case CHANNEL_TARGETED:
                    IRCClient.this.eventManager.callEvent(new ChannelTargetedMessageEvent(IRCClient.this, (User) event.getActor(), IRCClient.this.actorProvider.getChannel(event.getArgs()[0].substring(1)).snapshot(), IRCClient.this.serverInfo.getTargetedChannelInfo(event.getArgs()[0]), event.getArgs()[1]));
                    break;
                case PRIVATE:
                    IRCClient.this.eventManager.callEvent(new PrivateMessageEvent(IRCClient.this, (User) event.getActor(), event.getArgs()[1]));
                    break;
            }
        }

        @CommandFilter("MODE")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void mode(ClientReceiveCommandEvent event) {
            if (IRCClient.this.getTypeByTarget(event.getArgs()[0]) == MessageTarget.CHANNEL) {
                ActorProvider.IRCChannel channel = IRCClient.this.actorProvider.getChannel(event.getArgs()[0]);
                List<ChannelUserMode> channelUserModes = IRCClient.this.serverInfo.getChannelUserModes();
                Map<Character, ChannelModeType> channelModes = IRCClient.this.serverInfo.getChannelModes();
                for (int currentArg = 1; currentArg < event.getArgs().length; currentArg++) { // Note: currentArg changes outside here too
                    String changes = event.getArgs()[currentArg];
                    if (!((changes.charAt(0) == '+') || (changes.charAt(0) == '-'))) {
                        // TODO Inform of failed MODE processing
                        return;
                    }
                    boolean add = true;
                    for (char modeChar : changes.toCharArray()) {
                        switch (modeChar) {
                            case '+':
                                add = true;
                                break;
                            case '-':
                                add = false;
                                break;
                            default:
                                ChannelModeType mode = channelModes.get(modeChar);
                                ChannelUserMode prefixMode = null;
                                String target = null;
                                if (mode == null) {
                                    for (ChannelUserMode prefix : channelUserModes) {
                                        if (prefix.getMode() == modeChar) {
                                            target = event.getArgs()[++currentArg];
                                            if (add) {
                                                channel.trackUserModeAdd(target, prefix);
                                            } else {
                                                channel.trackUserModeRemove(target, prefix);
                                            }
                                            prefixMode = prefix;
                                            break;
                                        }
                                    }
                                    if (prefixMode == null) {
                                        // TODO Inform of failed MODE processing
                                        return;
                                    }
                                } else if (add ? mode.isParameterRequiredOnSetting() : mode.isParameterRequiredOnRemoval()) {
                                    target = event.getArgs()[++currentArg];
                                }
                                IRCClient.this.eventManager.callEvent(new ChannelModeEvent(IRCClient.this, event.getActor(), channel.snapshot(), add, modeChar, prefixMode, target));
                                break;
                        }
                    }
                }
            }
        }

        @CommandFilter("JOIN")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void join(ClientReceiveCommandEvent event) {
            if (event.getActor() instanceof User) { // Just in case
                ActorProvider.IRCChannel channel = IRCClient.this.actorProvider.getChannel(event.getArgs()[0]);
                ActorProvider.IRCUser user = (ActorProvider.IRCUser) IRCClient.this.actorProvider.getActor(event.getActor().getName());
                channel.trackUser(user, null);
                if (user.getNick().equals(IRCClient.this.currentNick)) {
                    IRCClient.this.channels.add(event.getArgs()[0]);
                    IRCClient.this.actorProvider.channelTrack(channel);
                    IRCClient.this.sendRawLine("WHO " + channel.getName() + (IRCClient.this.serverInfo.hasWhoXSupport() ? " %cuhsnfar" : ""));
                }
                if (event.getArgs().length > 2) {
                    if (!"*".equals(event.getArgs()[1])) {
                        user.setAccount(event.getArgs()[1]);
                    }
                    user.setRealName(event.getArgs()[2]);
                }
                IRCClient.this.eventManager.callEvent(new ChannelJoinEvent(IRCClient.this, channel.snapshot(), user.snapshot()));
            }
        }

        @CommandFilter("PART")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void part(ClientReceiveCommandEvent event) {
            if (event.getActor() instanceof User) { // Just in case
                ActorProvider.IRCChannel channel = IRCClient.this.actorProvider.getChannel(event.getArgs()[0]);
                ActorProvider.IRCUser user = IRCClient.this.actorProvider.getUser((((User) event.getActor()).getNick()));
                IRCClient.this.eventManager.callEvent(new ChannelPartEvent(IRCClient.this, channel.snapshot(), user.snapshot(), (event.getArgs().length > 1) ? event.getArgs()[1] : ""));
                channel.trackUserPart(user.getNick());
                if (user.getNick().equals(IRCClient.this.currentNick)) {
                    IRCClient.this.channels.remove(channel.getName());
                    IRCClient.this.actorProvider.channelUntrack(channel);
                }
            }
        }

        @CommandFilter("QUIT")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void quit(ClientReceiveCommandEvent event) {
            if (event.getActor() instanceof User) { // Just in case
                IRCClient.this.eventManager.callEvent(new UserQuitEvent(IRCClient.this, (User) event.getActor(), (event.getArgs().length > 0) ? event.getArgs()[0] : ""));
                IRCClient.this.actorProvider.trackUserQuit(((User) event.getActor()).getNick());
            }
        }

        @CommandFilter("KICK")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void kick(ClientReceiveCommandEvent event) {
            ActorProvider.IRCChannel kickedChannel = IRCClient.this.actorProvider.getChannel(event.getArgs()[0]);
            ActorProvider.IRCUser kickedUser = IRCClient.this.actorProvider.getUser(event.getArgs()[1]);
            IRCClient.this.eventManager.callEvent(new ChannelKickEvent(IRCClient.this, kickedChannel.snapshot(), (User) event.getActor(), kickedUser.snapshot(), (event.getArgs().length > 2) ? event.getArgs()[2] : ""));
            kickedChannel.trackUserPart(event.getArgs()[1]);
            if (event.getArgs()[1].equals(IRCClient.this.currentNick)) {
                IRCClient.this.channels.remove(kickedChannel.getName());
                IRCClient.this.actorProvider.channelUntrack(kickedChannel);
            }
        }

        @CommandFilter("NICK")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void nick(ClientReceiveCommandEvent event) {
            if (event.getActor() instanceof User) {
                ActorProvider.IRCUser user = IRCClient.this.actorProvider.getUser((((User) event.getActor()).getNick()));
                User oldUser = user.snapshot();
                if (user.getNick().equals(IRCClient.this.currentNick)) {
                    IRCClient.this.currentNick = event.getArgs()[0];
                }
                IRCClient.this.actorProvider.trackUserNick(user.getNick(), event.getArgs()[0]);
                User newUser = user.snapshot();
                IRCClient.this.eventManager.callEvent(new UserNickChangeEvent(IRCClient.this, oldUser, newUser));
            }
        }

        @CommandFilter("INVITE")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void invite(ClientReceiveCommandEvent event) {
            ActorProvider.IRCChannel invitedChannel = IRCClient.this.actorProvider.getChannel(event.getArgs()[1]);
            if ((IRCClient.this.getTypeByTarget(event.getArgs()[0]) == MessageTarget.PRIVATE) && IRCClient.this.channelsIntended.contains(invitedChannel.getName())) {
                IRCClient.this.sendRawLine("JOIN " + invitedChannel.getName());
            }
            IRCClient.this.eventManager.callEvent(new ChannelInviteEvent(IRCClient.this, invitedChannel.snapshot(), event.getActor(), event.getArgs()[0]));
        }

        @CommandFilter("TOPIC")
        @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
        public void topic(ClientReceiveCommandEvent event) {
            ActorProvider.IRCChannel topicChannel = IRCClient.this.actorProvider.getChannel(event.getArgs()[0]);
            topicChannel.setTopic(event.getArgs()[1]);
            topicChannel.setTopic(System.currentTimeMillis(), event.getActor());
            IRCClient.this.eventManager.callEvent(new ChannelTopicEvent(IRCClient.this, topicChannel.snapshot(), true));
        }
    }

    private MessageTarget getTypeByTarget(@Nonnull String target) {
        if (this.currentNick.equalsIgnoreCase(target)) {
            return MessageTarget.PRIVATE;
        }
        if (this.serverInfo.isTargetedChannel(target)) {
            return MessageTarget.CHANNEL_TARGETED;
        }
        if (this.serverInfo.isValidChannel(target)) {
            return MessageTarget.CHANNEL;
        }
        return MessageTarget.UNKNOWN;
    }

    private void sendNickChange(@Nonnull String newNick) {
        this.requestedNick = newNick;
        this.sendRawLineImmediately("NICK " + newNick);
    }
}