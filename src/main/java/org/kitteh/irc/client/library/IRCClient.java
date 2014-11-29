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
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.channel.ChannelCTCPEvent;
import org.kitteh.irc.client.library.event.channel.ChannelInviteEvent;
import org.kitteh.irc.client.library.event.channel.ChannelJoinEvent;
import org.kitteh.irc.client.library.event.channel.ChannelKickEvent;
import org.kitteh.irc.client.library.event.channel.ChannelModeEvent;
import org.kitteh.irc.client.library.event.channel.ChannelNoticeEvent;
import org.kitteh.irc.client.library.event.user.PrivateCTCPReplyEvent;
import org.kitteh.irc.client.library.event.user.PrivateNoticeEvent;
import org.kitteh.irc.client.library.event.user.UserNickChangeEvent;
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

import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class IRCClient implements Client {
    private class InputProcessor extends Thread {
        private final Queue<String> queue = new ConcurrentLinkedQueue<>();

        private InputProcessor() {
            this.setName("Kitteh IRC Client Input Processor (" + IRCClient.this.getName() + ")");
            this.start();
        }

        @Override
        public void run() {
            IRCClient.this.connect();
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

    private enum MessageTarget {
        CHANNEL,
        PRIVATE,
        UNKNOWN
    }

    private final Config config;
    private final InputProcessor processor;

    private String goalNick;
    private String currentNick;
    private String requestedNick;

    // RFC 2812 section 1.3 'Channel names are case insensitive.'
    private final Set<String> channels = new LCSet();

    private AuthType authType;
    private String auth;
    private String authReclaim;

    private NettyManager.ClientConnection connection;

    private long lastCheck;

    private final EventManager eventManager = new EventManager(this);

    private final Listener<Exception> exceptionListener;
    private final Listener<String> inputListener;
    private final Listener<String> outputListener;

    private Map<Character, Character> prefixes = new ConcurrentHashMap<Character, Character>() {
        {
            put('o', '@');
            put('v', '+');
        }
    };
    private static final Pattern PREFIX_PATTERN = Pattern.compile("PREFIX=\\(([a-zA-Z]+)\\)([^ ]+)");
    private Map<Character, ChannelModeType> modes = ChannelModeType.getDefaultModes();
    private static final Pattern CHANMODES_PATTERN = Pattern.compile("CHANMODES=(([,A-Za-z]+)(,([,A-Za-z]+)){0,3})");

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
    }

    @Override
    public void addChannel(String... channels) {
        Sanity.nullCheck(channels, "Channels cannot be null");
        Sanity.truthiness(channels.length > 0, "Channels cannot be empty array");
        for (String channel : channels) {
            if (!Channel.isChannel(channel)) {
                continue;
            }
            this.channels.add(channel);
            this.sendRawLine("JOIN :" + channel);
        }
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
    public void setAuth(AuthType type, String nick, String pass) {
        Sanity.nullCheck(type, "Auth type cannot be null");
        this.authType = type;
        switch (type) {
            case GAMESURGE:
                this.auth = "PRIVMSG AuthServ@services.gamesurge.net :auth " + nick + " " + pass;
                this.authReclaim = "";
                break;
            case NICKSERV:
            default:
                this.auth = "PRIVMSG NickServ :identify " + pass;
                this.authReclaim = "PRIVMSG NickServ :ghost " + nick + " " + pass;
        }
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

    void connect() {
        this.connection = NettyManager.connect(this);

        // If the server has a password, send that along first
        if (this.config.get(Config.SERVER_PASSWORD) != null) {
            this.sendPriorityRawLine("PASS " + this.config.get(Config.SERVER_PASSWORD));
        }

        // Initial USER and NICK messages. Let's just assume we want +iw (send 8)
        this.sendPriorityRawLine("USER " + this.config.get(Config.USER) + " 8 * :" + this.config.get(Config.REAL_NAME));
        this.sendNickChange(this.goalNick);

        // Figure out auth
        if (this.authReclaim != null && !this.currentNick.equals(this.goalNick) && this.authType.isNickOwned()) {
            this.sendPriorityRawLine(this.authReclaim);
            this.sendNickChange(this.goalNick);
        }
        if (this.auth != null) {
            this.sendPriorityRawLine(this.auth);
        }
    }

    private String handleColon(String string) {
        return string.startsWith(":") ? string.substring(1) : string;
    }

    private void handleLine(String line) throws Throwable {
        if ((line == null) || (line.length() == 0)) {
            return;
        }

        final String[] split = line.split(" ");
        if ((split.length <= 1) || !split[0].startsWith(":")) {
            return; // Invalid!
        }
        final Actor actor = Actor.getActor(split[0].substring(1));
        int numeric = -1;
        try {
            numeric = Integer.parseInt(split[1]);
        } catch (NumberFormatException ignored) {
        }
        if (numeric > -1) {
            switch (numeric) {
                case 1: // Welcome
                    break;
                case 2: // Your host is...
                    break;
                case 3: // server created
                    break;
                case 4: // version / modes
                    // We're in! Start sending all messages.
                    this.connection.scheduleSending(this.config.get(Config.MESSAGE_DELAY));
                    break;
                case 5:
                    for (int i = 2; i < split.length; i++) {
                        Matcher prefixMatcher = PREFIX_PATTERN.matcher(split[i]);
                        if (prefixMatcher.find()) {
                            String modes = prefixMatcher.group(1);
                            String display = prefixMatcher.group(2);
                            if (modes.length() == display.length()) {
                                Map<Character, Character> prefixMap = new ConcurrentHashMap<>();
                                for (int index = 0; index < modes.length(); index++) {
                                    prefixMap.put(modes.charAt(index), display.charAt(index));
                                }
                                this.prefixes = prefixMap;
                            }
                            continue;
                        }
                        Matcher modeMatcher = CHANMODES_PATTERN.matcher(split[i]);
                        if (modeMatcher.find()) {
                            String[] modes = modeMatcher.group(1).split(",");
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
                            this.modes = modesMap;
                        }
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
                case 372: // info, such as continued motd
                case 375: // motd start
                case 376: // motd end
                    break;
                // Channel info
                case 332: // Channel topic
                case 333: // Topic set by
                case 353: // Channel users list (/names). format is 353 nick = #channel :names
                case 366: // End of /names
                case 422: // MOTD missing
                    break;
                // Nick errors, try for new nick below
                case 431: // No nick given
                case 432: // Erroneous nickname
                case 433: // Nick in use
                    this.sendNickChange(this.requestedNick + '`');
                    break;
            }
        } else {
            final Command command = Command.getByName(split[1]);
            if (command == null) {
                return; // Unknown command
            }
            // CTCP
            if ((command == Command.NOTICE || command == Command.PRIVMSG) && CTCPUtil.isCTCP(this.handleColon(StringUtil.combineSplit(split, 3)))) {
                final String ctcpMessage = CTCPUtil.fromCTCP(this.handleColon(StringUtil.combineSplit(split, 3)));
                switch (command) {
                    case NOTICE:
                        if (this.getTypeByTarget(split[2]) == MessageTarget.PRIVATE) {
                            this.eventManager.callEvent(new PrivateCTCPReplyEvent(actor, ctcpMessage));
                        }
                        break;
                    case PRIVMSG:
                        switch (this.getTypeByTarget(split[2])) {
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
                                this.eventManager.callEvent(new ChannelCTCPEvent(actor, (Channel) Actor.getActor(split[2]), ctcpMessage));
                                break;
                        }
                        break;
                }
                return; // If handled as CTCP we don't care about further handling.
            }
            switch (command) {
                case NOTICE:
                    final String noticeMessage = this.handleColon(StringUtil.combineSplit(split, 3));
                    switch (this.getTypeByTarget(split[2])) {
                        case CHANNEL:
                            this.eventManager.callEvent(new ChannelNoticeEvent(actor, (Channel) Actor.getActor(split[2]), noticeMessage));
                            break;
                        case PRIVATE:
                            this.eventManager.callEvent(new PrivateNoticeEvent(actor, noticeMessage));
                            break;
                    }
                    break;
                case PRIVMSG:
                    final String message = this.handleColon(StringUtil.combineSplit(split, 3));
                    switch (this.getTypeByTarget(split[2])) {
                        case CHANNEL:
                            this.eventManager.callEvent(new ChannelMessageEvent(actor, (Channel) Actor.getActor(split[2]), message));
                            break;
                        case PRIVATE:
                            this.eventManager.callEvent(new PrivateMessageEvent(actor, message));
                            break;
                    }
                    break;
                case MODE:
                    if (this.getTypeByTarget(split[2]) == MessageTarget.CHANNEL) {
                        Channel channel = (Channel) Actor.getActor(split[2]);
                        String modechanges = split[3];
                        int currentArg = 4;
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
                        for (int i = 1; i < modechanges.length() && currentArg < split.length; i++) {
                            char next = modechanges.charAt(i);
                            if (next == '+') {
                                add = true;
                            } else if (next == '-') {
                                add = false;
                            } else {
                                boolean hasArg;
                                if (this.prefixes.containsKey(next)) {
                                    hasArg = true;
                                } else {
                                    ChannelModeType type = this.modes.get(next);
                                    if (type == null) {
                                        // TODO clean up error handling
                                        return;
                                    }
                                    hasArg = (add && type.isParameterRequiredOnSetting()) || (!add && type.isParameterRequiredOnRemoval());
                                }
                                this.eventManager.callEvent(new ChannelModeEvent(actor, channel, add, next, hasArg ? split[currentArg++] : null));
                            }
                        }
                    }
                    break;
                case JOIN:
                    if (actor instanceof User) { // Just in case
                        this.eventManager.callEvent(new ChannelJoinEvent((Channel) Actor.getActor(split[2]), (User) actor));
                    }
                    break;
                case PART:
                    if (actor instanceof User) { // Just in case
                        this.eventManager.callEvent(new ChannelPartEvent((Channel) Actor.getActor(split[2]), (User) actor, split.length > 2 ? this.handleColon(StringUtil.combineSplit(split, 3)) : ""));
                    }
                    break;
                case QUIT:
                    if (actor instanceof User) { // Just in case
                        this.eventManager.callEvent(new UserQuitEvent((User) actor, split.length > 1 ? this.handleColon(StringUtil.combineSplit(split, 2)) : ""));
                    }
                    break;
                case KICK:
                    this.eventManager.callEvent(new ChannelKickEvent((Channel) Actor.getActor(split[2]), actor, split[3], this.handleColon(StringUtil.combineSplit(split, 4))));
                    break;
                case NICK:
                    if (actor instanceof User) {
                        User user = (User) actor;
                        if (user.getNick().equals(this.currentNick)) {
                            this.currentNick = split[2];
                        }
                        this.eventManager.callEvent(new UserNickChangeEvent(user, split[2]));
                    }
                    break;
                case INVITE:
                    if (this.getTypeByTarget(split[2]) == MessageTarget.PRIVATE && this.channels.contains(split[3])) {
                        this.sendRawLine("JOIN " + split[3]);
                    }
                    this.eventManager.callEvent(new ChannelInviteEvent((Channel) Actor.getActor(split[3]), actor, split[2]));
                    break;
                case TOPIC:
                    this.eventManager.callEvent(new ChannelTopicEvent(actor, (Channel) Actor.getActor(split[2]), this.handleColon(StringUtil.combineSplit(split, 3))));
                    break;
                default:
                    // TODO: Unknown event?
                    break;
            }
        }
    }

    private MessageTarget getTypeByTarget(String target) {
        if (this.currentNick.equalsIgnoreCase(target)) {
            return MessageTarget.PRIVATE;
        }
        if (this.channels.contains(target)) {
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