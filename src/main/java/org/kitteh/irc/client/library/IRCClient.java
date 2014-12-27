/*
 * * Copyright (C) 2013-2014 Matt Baxter http://kitteh.org
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

import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.channel.ChannelCTCPEvent;
import org.kitteh.irc.client.library.event.channel.ChannelInviteEvent;
import org.kitteh.irc.client.library.event.channel.ChannelJoinEvent;
import org.kitteh.irc.client.library.event.channel.ChannelKickEvent;
import org.kitteh.irc.client.library.event.channel.ChannelModeEvent;
import org.kitteh.irc.client.library.event.channel.ChannelNoticeEvent;
import org.kitteh.irc.client.library.event.channel.ChannelUsersUpdatedEvent;
import org.kitteh.irc.client.library.event.client.ClientConnectedEvent;
import org.kitteh.irc.client.library.event.user.PrivateCTCPReplyEvent;
import org.kitteh.irc.client.library.event.user.PrivateNoticeEvent;
import org.kitteh.irc.client.library.event.user.UserNickChangeEvent;
import org.kitteh.irc.client.library.exception.KittehISupportProcessingFailureException;
import org.kitteh.irc.client.library.util.LCSet;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.event.channel.ChannelPartEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTopicEvent;
import org.kitteh.irc.client.library.event.user.PrivateCTCPQueryEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;
import org.kitteh.irc.client.library.event.user.UserQuitEvent;
import org.kitteh.irc.client.library.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class IRCClient implements Client {
    private class ConnectedServerInfo implements ServerInfo {
        private Map<Character, Integer> channelLimits = new HashMap<>();

        @Override
        public int getChannelLengthLimit() {
            return IRCClient.this.actorProvider.getChannelLength();
        }

        @Override
        public Map<Character, Integer> getChannelLimits() {
            return new HashMap<>(this.channelLimits);
        }

        @Override
        public Map<Character, ChannelModeType> getChannelModes() {
            return new HashMap<>(IRCClient.this.modes);
        }

        @Override
        public List<Character> getChannelPrefixes() {
            List<Character> list = new ArrayList<>();
            for (char prefix : IRCClient.this.actorProvider.getChannelPrefixes()) {
                list.add(prefix);
            }
            return list;
        }

        @Override
        public List<ChannelUserMode> getChannelUserModes() {
            return new ArrayList<>(IRCClient.this.prefixes);
        }

        @Override
        public int getNickLengthLimit() {
            return IRCClient.this.actorProvider.getNickLength();
        }
    }

    private class InputProcessor extends Thread {
        private final Queue<String> queue = new ConcurrentLinkedQueue<>();

        private InputProcessor() {
            this.setName("Kitteh IRC Client Input Processor (" + IRCClient.this.getName() + ")");
            this.start();
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                synchronized (this.queue) {
                    if (this.queue.isEmpty()) {
                        try {
                            this.queue.wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
                try {
                    IRCClient.this.handleLine(this.queue.poll());
                } catch (final Throwable thrown) {
                    // NOOP
                }
            }
        }

        private void queue(String message) {
            synchronized (this.queue) {
                this.queue.add(message);
                this.queue.notify();
            }
        }
    }

    private enum ISupport {
        CHANNELLEN {
            @Override
            boolean process(String value, IRCClient client) {
                try {
                    int length = Integer.parseInt(value);
                    client.actorProvider.setChannelLength(length);
                    return true;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        },
        CHANLIMIT {
            @Override
            boolean process(String value, IRCClient client) {
                String[] pairs = value.split(",");
                Map<Character, Integer> limits = new HashMap<>();
                for (String p : pairs) {
                    String[] pair = p.split(":");
                    if (pair.length!=2) {
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
                client.serverInfo.channelLimits = limits;
                return true;
            }
        },
        CHANMODES {
            @Override
            boolean process(String value, IRCClient client) {
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
                client.modes = modesMap;
                return true;
            }
        },
        CHANTYPES {
            @Override
            boolean process(String value, IRCClient client) {
                if (value.isEmpty()) {
                    return false;
                }
                client.actorProvider.setChannelPrefixes(value.toCharArray());
                return true;
            }
        },
        NICKLEN {
            @Override
            boolean process(String value, IRCClient client) {
                try {
                    int length = Integer.parseInt(value);
                    client.actorProvider.setNickLength(length);
                    return true;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        },
        PREFIX {
            final Pattern PATTERN = Pattern.compile("\\(([a-zA-Z]+)\\)([^ ]+)");

            @Override
            boolean process(String value, IRCClient client) {
                Matcher matcher = PATTERN.matcher(value);
                if (!matcher.find()) {
                    return false;
                }
                String modes = matcher.group(1);
                String display = matcher.group(2);
                if (modes.length() == display.length()) {
                    List<ChannelUserMode> prefixList = new ArrayList<>();
                    for (int index = 0; index < modes.length(); index++) {
                        prefixList.add(new ActorProvider.IRCChannelUserMode(modes.charAt(index), display.charAt(index)));
                    }
                    client.prefixes = prefixList;
                }
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

        private static void handle(String arg, IRCClient client) {
            Matcher matcher = PATTERN.matcher(arg);
            if (!matcher.find()) {
                return;
            }
            ISupport iSupport = MAP.get(matcher.group(1));
            if (iSupport != null) {
                boolean success = iSupport.process(matcher.group(2), client);
                if (!success) {
                    client.exceptionListener.queue(new KittehISupportProcessingFailureException(arg));
                }
            }
        }

        abstract boolean process(String value, IRCClient client);
    }

    private enum MessageTarget {
        CHANNEL,
        PRIVATE,
        UNKNOWN
    }

    private final Config config;
    private final InputProcessor processor;
    private ConnectedServerInfo serverInfo = new ConnectedServerInfo();

    private String goalNick;
    private String currentNick;
    private String requestedNick;

    private final Set<Channel> channels = new CopyOnWriteArraySet<>();
    private final Set<String> channelsIntended = new LCSet(); // TODO use lowercasing dependent on ISUPPORT

    private NettyManager.ClientConnection connection;

    private final EventManager eventManager = new EventManager(this);

    private final Listener<Exception> exceptionListener;
    private final Listener<String> inputListener;
    private final Listener<String> outputListener;

    private final ActorProvider actorProvider = new ActorProvider(this);

    private List<ChannelUserMode> prefixes = new ArrayList<ChannelUserMode>() {
        {
            this.add(new ActorProvider.IRCChannelUserMode('o', '@'));
            this.add(new ActorProvider.IRCChannelUserMode('v', '+'));
        }
    };
    private Map<Character, ChannelModeType> modes = ChannelModeType.getDefaultModes();

    IRCClient(Config config) {
        this.config = config;
        this.currentNick = this.requestedNick = this.goalNick = this.config.get(Config.NICK);

        Config.ExceptionConsumerWrapper exceptionListenerWrapper = this.config.get(Config.LISTENER_EXCEPTION);
        this.exceptionListener = new Listener<>(exceptionListenerWrapper == null ? null : exceptionListenerWrapper.getConsumer());
        Config.StringConsumerWrapper inputListenerWrapper = this.config.get(Config.LISTENER_INPUT);
        this.inputListener = new Listener<>(inputListenerWrapper == null ? null : inputListenerWrapper.getConsumer());
        Config.StringConsumerWrapper outputListenerWrapper = this.config.get(Config.LISTENER_OUTPUT);
        this.outputListener = new Listener<>(outputListenerWrapper == null ? null : outputListenerWrapper.getConsumer());

        this.processor = new InputProcessor();
        this.connect();
    }

    @Override
    public void addChannel(String... channels) {
        Sanity.nullCheck(channels, "Channels cannot be null");
        Sanity.truthiness(channels.length > 0, "Channels cannot be empty array");
        for (String channelName : channels) {
            if (!this.actorProvider.isValidChannel(channelName)) {
                continue;
            }
            this.channelsIntended.add(channelName);
            this.sendRawLine("JOIN :" + channelName);
        }
    }

    @Override
    public Set<Channel> getChannels() {
        return Collections.unmodifiableSet(new HashSet<>(this.channels));
    }

    @Override
    public EventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public String getIntendedNick() {
        return this.goalNick;
    }

    @Override
    public String getName() {
        return this.config.get(Config.NAME);
    }

    @Override
    public String getNick() {
        return this.currentNick;
    }

    @Override
    public void removeChannel(String channelName) {
        Channel channel = this.actorProvider.getChannel(channelName);
        if (channel != null) {
            this.channelsIntended.remove(channelName);
            if (this.channels.contains(channel)) {
                this.sendRawLine("PART " + channelName);
            }
        }
    }

    @Override
    public void sendCTCPMessage(String target, String message) {
        Sanity.nullCheck(target, "Target cannot be null");
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.truthiness(target.indexOf(' ') == -1, "Target cannot have spaces");
        this.sendRawLine("PRIVMSG " + target + " :" + CTCPUtil.toCTCP(message));
    }

    @Override
    public void sendMessage(String target, String message) {
        Sanity.nullCheck(target, "Target cannot be null");
        Sanity.nullCheck(message, "Message cannot be null");
        Sanity.truthiness(target.indexOf(' ') == -1, "Target cannot have spaces");
        this.sendRawLine("PRIVMSG " + target + " :" + message);
    }

    @Override
    public void sendRawLine(String message) {
        Sanity.nullCheck(message, "Message cannot be null");
        this.connection.sendMessage(message, false);
    }

    @Override
    public void setAuth(AuthType authType, String name, String pass) {
        Sanity.nullCheck(authType, "Auth type cannot be null!");
        Sanity.nullCheck(name, "Name cannot be null!");
        Sanity.nullCheck(pass, "Password cannot be null!");
        this.config.set(Config.AUTH_TYPE, authType);
        this.config.set(Config.AUTH_NAME, name);
        this.config.set(Config.AUTH_PASS, pass);
    }

    @Override
    public void setMessageDelay(int delay) {
        Sanity.truthiness(delay > -1, "Delay must be a positive value");
        this.config.set(Config.MESSAGE_DELAY, delay);
        if (this.connection != null) {
            this.connection.scheduleSending(delay);
        }
    }

    @Override
    public void setNick(String nick) {
        Sanity.nullCheck(nick, "Nick cannot be null");
        this.goalNick = nick.trim();
        this.sendNickChange(this.goalNick);
    }

    @Override
    public void shutdown(String reason) {
        this.processor.interrupt();

        this.connection.shutdown(reason != null && reason.isEmpty() ? null : reason);

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
    void processLine(String line) {
        if (line.startsWith("PING ")) {
            this.sendPriorityRawLine("PONG " + line.substring(5));
        } else {
            this.processor.queue(line);
        }
    }

    Config getConfig() {
        return this.config;
    }

    Listener<Exception> getExceptionListener() {
        return this.exceptionListener;
    }

    Listener<String> getInputListener() {
        return this.inputListener;
    }

    Listener<String> getOutputListener() {
        return this.outputListener;
    }

    void authenticate() {
        AuthType authType = this.config.get(Config.AUTH_TYPE);
        if (authType != null) {
            String auth;
            String authReclaim;
            String name = this.config.get(Config.AUTH_NAME);
            String pass = this.config.get(Config.AUTH_PASS);
            switch (authType) {
                case GAMESURGE:
                    auth = "PRIVMSG AuthServ@services.gamesurge.net :auth " + name + " " + pass;
                    authReclaim = "";
                    break;
                case NICKSERV:
                default:
                    auth = "PRIVMSG NickServ :identify " + name + " " + pass;
                    authReclaim = "PRIVMSG NickServ :ghost " + name + " " + pass;
            }
            if (!this.currentNick.equals(this.goalNick) && authType.isNickOwned()) {
                this.sendPriorityRawLine(authReclaim);
                this.sendNickChange(this.goalNick);
            }
            this.sendPriorityRawLine(auth);
        }
    }

    void connect() {
        this.connection = NettyManager.connect(this);

        // If the server has a password, send that along first
        if (this.config.get(Config.SERVER_PASSWORD) != null) {
            this.sendPriorityRawLine("PASS " + this.config.get(Config.SERVER_PASSWORD));
        }

        // Initial USER and NICK messages. Let's just assume we want +iw (send 8)
        this.sendPriorityRawLine("USER " + this.config.get(Config.USER) + " 8 * :" + this.config.get(Config.REAL_NAME));
        this.sendNickChange(this.goalNick);
    }

    private String[] handleArgs(String[] split, int start) {
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

    private void handleLine(final String line) {
        if ((line == null) || (line.length() == 0)) {
            return;
        }

        final String[] split = line.split(" ");

        int argsIndex = 1;
        final Actor actor;
        if (split[0].startsWith(":")) {
            argsIndex++;
            actor = this.actorProvider.getActor(split[0].substring(1));
        } else {
            actor = null; // TODO provide a default actor for when it's the server
        }

        final String commandString = split[argsIndex - 1];

        final String[] args = this.handleArgs(split, argsIndex);

        int numeric = -1;
        try {
            numeric = Integer.parseInt(commandString);
        } catch (NumberFormatException ignored) {
        }
        if (numeric > -1) {
            this.handleLineNumeric(actor, numeric, args);
        } else {
            Command command = Command.getByName(commandString);
            if (command != null) {
                this.handleLineCommand(actor, command, args);
            }
        }
    }

    private void handleLineNumeric(final Actor actor, final int command, final String[] args) {
        switch (command) {
            case 1: // Welcome
                break;
            case 2: // Your host is...
                break;
            case 3: // server created
                break;
            case 4: // version / modes
                // We're in! Start sending all messages.
                this.authenticate();
                this.eventManager.callEvent(new ClientConnectedEvent(actor));
                this.connection.scheduleSending(this.config.get(Config.MESSAGE_DELAY));
                break;
            case 5: // ISUPPORT
                for (String arg : args) {
                    ISupport.handle(arg, this);
                }
                break;
            case 250: // Highest connection count
            case 251: // There are X users
            case 252: // X IRC OPs
            case 253: // X unknown connections
            case 254: // X channels formed
            case 255: // X clients, X servers
            case 265: // Local users, max
            case 266: // global users, max
                break;
            case 315:
                // Self is arg 0
                if (this.actorProvider.isValidChannel(args[1])) { // target
                    this.eventManager.callEvent(new ChannelUsersUpdatedEvent(this.actorProvider.getChannel(args[1])));
                }
                break;
            // Channel info
            case 332: // Channel topic
            case 333: // Topic set by
                break;
            case 352: // WHO list
                // Self is arg 0
                if (this.actorProvider.isValidChannel(args[1])) {
                    final String channelName = args[1];
                    final String ident = args[2];
                    final String host = args[3];
                    // server is arg 4
                    final String nick = args[5];
                    final String status = args[6];
                    // The rest I don't care about
                    final User user = (User) this.actorProvider.getActor(nick + "!" + ident + "@" + host);
                    final ActorProvider.IRCChannel channel = this.actorProvider.getChannel(channelName);
                    final Set<ChannelUserMode> modes = new HashSet<>();
                    for (char prefix : status.substring(1).toCharArray()) {
                        for (ChannelUserMode mode : this.prefixes) {
                            if (mode.getPrefix() == prefix) {
                                modes.add(mode);
                                break;
                            }
                        }
                    }
                    channel.trackUser(user, modes);
                }
                break;
            case 353: // Channel users list (/names). format is 353 nick = #channel :names
            case 366: // End of /names
            case 372: // info, such as continued motd
            case 375: // motd start
            case 376: // motd end
            case 422: // MOTD missing
                break;
            // Nick errors, try for new nick below
            case 431: // No nick given
            case 432: // Erroneous nickname
            case 433: // Nick in use
                this.sendNickChange(this.requestedNick + '`');
                break;
        }
    }

    private void handleLineCommand(final Actor actor, final Command command, final String[] args) {
        // CTCP
        if ((command == Command.NOTICE || command == Command.PRIVMSG) && CTCPUtil.isCTCP(args[1])) {
            final String ctcpMessage = CTCPUtil.fromCTCP(args[1]);
            final MessageTarget messageTarget = this.getTypeByTarget(args[0]);
            switch (command) {
                case NOTICE:
                    if (messageTarget == MessageTarget.PRIVATE) {
                        this.eventManager.callEvent(new PrivateCTCPReplyEvent(actor, ctcpMessage));
                    }
                    break;
                case PRIVMSG:
                    switch (messageTarget) {
                        case PRIVATE:
                            String reply = null; // Message to send as CTCP reply (NOTICE). Send nothing if null.
                            if (ctcpMessage.equals("VERSION")) {
                                reply = "VERSION I am Kitteh!";
                            } else if (ctcpMessage.equals("TIME")) {
                                reply = "TIME " + new Date().toString();
                            } else if (ctcpMessage.equals("FINGER")) {
                                reply = "FINGER om nom nom tasty finger";
                            } else if (ctcpMessage.startsWith("PING ")) {
                                reply = ctcpMessage;
                            }
                            PrivateCTCPQueryEvent event = new PrivateCTCPQueryEvent(actor, ctcpMessage, reply);
                            this.eventManager.callEvent(event);
                            reply = event.getReply();
                            if (reply != null) {
                                this.sendRawLine("NOTICE " + actor.getName() + " :" + CTCPUtil.toCTCP(reply));
                            }
                            break;
                        case CHANNEL:
                            this.eventManager.callEvent(new ChannelCTCPEvent(actor, this.actorProvider.getChannel(args[0]), ctcpMessage));
                            break;
                    }
                    break;
            }
            return; // If handled as CTCP we don't care about further handling.
        }
        switch (command) {
            case NOTICE:
                switch (this.getTypeByTarget(args[0])) {
                    case CHANNEL:
                        this.eventManager.callEvent(new ChannelNoticeEvent(actor, this.actorProvider.getChannel(args[0]), args[1]));
                        break;
                    case PRIVATE:
                        this.eventManager.callEvent(new PrivateNoticeEvent(actor, args[1]));
                        break;
                }
                break;
            case PRIVMSG:
                switch (this.getTypeByTarget(args[0])) {
                    case CHANNEL:
                        this.eventManager.callEvent(new ChannelMessageEvent(actor, this.actorProvider.getChannel(args[0]), args[1]));
                        break;
                    case PRIVATE:
                        this.eventManager.callEvent(new PrivateMessageEvent(actor, args[1]));
                        break;
                }
                break;
            case MODE: // TODO handle this format: "+mode param +mode param"
                if (this.getTypeByTarget(args[0]) == MessageTarget.CHANNEL) {
                    ActorProvider.IRCChannel channel = this.actorProvider.getChannel(args[0]);
                    String modechanges = args[1];
                    int currentArg = 2;
                    boolean add;
                    switch (modechanges.charAt(0)) {
                        case '+':
                            add = true;
                            break;
                        case '-':
                            add = false;
                            break;
                        default:
                            return;
                    }
                    for (int i = 1; i < modechanges.length() && currentArg < args.length; i++) {
                        char next = modechanges.charAt(i);
                        if (next == '+') {
                            add = true;
                        } else if (next == '-') {
                            add = false;
                        } else {
                            boolean hasArg;
                            boolean isPrefix = false;
                            for (ChannelUserMode prefix : this.prefixes) {
                                if (prefix.getMode() == next) {
                                    isPrefix = true;
                                    break;
                                }
                            }
                            if (isPrefix) {
                                hasArg = true;
                            } else {
                                ChannelModeType type = this.modes.get(next);
                                if (type == null) {
                                    // TODO clean up error handling
                                    return;
                                }
                                hasArg = (add && type.isParameterRequiredOnSetting()) || (!add && type.isParameterRequiredOnRemoval());
                            }
                            final String nick = hasArg ? args[currentArg++] : null;
                            if (isPrefix) {
                                if (add) {
                                    channel.trackUserModeAdd(nick, this.prefixes.get(next));
                                } else {
                                    channel.trackUserModeRemove(nick, this.prefixes.get(next));
                                }
                            }
                            this.eventManager.callEvent(new ChannelModeEvent(actor, channel, add, next, nick));
                        }
                    }
                }
                break;
            case JOIN:
                if (actor instanceof User) { // Just in case
                    ActorProvider.IRCChannel channel = this.actorProvider.getChannel(args[0]);
                    User user = (User) actor;
                    channel.trackUserJoin(user);
                    if (user.getNick().equals(this.currentNick)) {
                        this.channels.add(channel);
                        this.sendRawLine("WHO " + channel.getName());
                    }
                    this.eventManager.callEvent(new ChannelJoinEvent(channel, user));
                }
                break;
            case PART:
                if (actor instanceof User) { // Just in case
                    ActorProvider.IRCChannel channel = this.actorProvider.getChannel(args[0]);
                    User user = (User) actor;
                    channel.trackUserPart(user);
                    if (user.getNick().equals(this.currentNick)) {
                        this.channels.remove(channel);
                    }
                    this.eventManager.callEvent(new ChannelPartEvent(channel, user, args.length > 1 ? args[1] : ""));
                }
                break;
            case QUIT:
                if (actor instanceof User) { // Just in case
                    this.actorProvider.trackUserQuit((User) actor);
                    this.eventManager.callEvent(new UserQuitEvent((User) actor, args.length > 0 ? args[0] : ""));
                }
                break;
            case KICK:
                Channel kickedChannel = this.actorProvider.getChannel(args[0]);
                if (args[1].equals(this.currentNick)) {
                    this.channels.remove(kickedChannel);
                }
                this.eventManager.callEvent(new ChannelKickEvent(kickedChannel, actor, args[1], args.length > 2 ? args[2] : ""));
                break;
            case NICK:
                if (actor instanceof User) {
                    User user = (User) actor;
                    if (user.getNick().equals(this.currentNick)) {
                        this.currentNick = args[0];
                    }
                    this.eventManager.callEvent(new UserNickChangeEvent(user, args[0]));
                }
                break;
            case INVITE:
                Channel invitedChannel = this.actorProvider.getChannel(args[1]);
                if (this.getTypeByTarget(args[0]) == MessageTarget.PRIVATE && this.channelsIntended.contains(invitedChannel.getName())) {
                    this.sendRawLine("JOIN " + invitedChannel.getName());
                }
                this.eventManager.callEvent(new ChannelInviteEvent(invitedChannel, actor, args[0]));
                break;
            case TOPIC:
                this.eventManager.callEvent(new ChannelTopicEvent(actor, this.actorProvider.getChannel(args[0]), args[1]));
                break;
            default:
                break;
        }
    }

    private MessageTarget getTypeByTarget(String target) {
        if (this.currentNick.equalsIgnoreCase(target)) {
            return MessageTarget.PRIVATE;
        }
        if (this.actorProvider.isValidChannel(target)) {
            return MessageTarget.CHANNEL;
        }
        return MessageTarget.UNKNOWN;
    }

    private void sendNickChange(String newnick) {
        this.requestedNick = newnick;
        this.sendPriorityRawLine("NICK " + newnick);
    }

    private void sendPriorityRawLine(String message) {
        this.connection.sendMessage(message, true);
    }
}