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
package org.kitteh.irc;

import org.kitteh.irc.elements.Actor;
import org.kitteh.irc.elements.Channel;
import org.kitteh.irc.elements.User;
import org.kitteh.irc.event.channel.ChannelCTCPEvent;
import org.kitteh.irc.event.channel.ChannelInviteEvent;
import org.kitteh.irc.event.channel.ChannelJoinEvent;
import org.kitteh.irc.event.channel.ChannelKickEvent;
import org.kitteh.irc.event.channel.ChannelMessageEvent;
import org.kitteh.irc.event.channel.ChannelModeEvent;
import org.kitteh.irc.event.channel.ChannelNoticeEvent;
import org.kitteh.irc.event.channel.ChannelPartEvent;
import org.kitteh.irc.event.channel.ChannelTopicEvent;
import org.kitteh.irc.event.user.PrivateCTCPQueryEvent;
import org.kitteh.irc.event.user.PrivateCTCPReplyEvent;
import org.kitteh.irc.event.user.PrivateMessageEvent;
import org.kitteh.irc.event.user.PrivateNoticeEvent;
import org.kitteh.irc.event.user.UserNickChangeEvent;
import org.kitteh.irc.event.user.UserQuitEvent;
import org.kitteh.irc.util.LCSet;
import org.kitteh.irc.util.Sanity;
import org.kitteh.irc.util.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class IRCBot implements Bot {
    private class BotManager extends Thread {
        private BotManager() {
            this.setName("Kitteh IRCBot Main (" + IRCBot.this.getName() + ")");
            this.start();
        }

        @Override
        public void run() {
            IRCBot.this.run();
        }
    }

    private class BotProcessor extends Thread {
        private final Queue<String> queue = new ConcurrentLinkedQueue<>();

        private BotProcessor() {
            this.setName("Kitteh IRCBot Input Processor (" + IRCBot.this.getName() + ")");
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
                    IRCBot.this.handleLine(this.queue.poll());
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
    private final BotManager manager;
    private final BotProcessor processor;

    private String goalNick;
    private String currentNick;
    private String requestedNick;

    // RFC 2812 section 1.3 'Channel names are case insensitive.'
    private final Set<String> channels = new LCSet();

    private AuthType authType;
    private String auth;
    private String authReclaim;

    private IRCBotInput inputHandler;
    private IRCBotOutput outputHandler;

    private String shutdownReason = "KITTEH AWAY!";

    private boolean connected;
    private long lastCheck;

    private final EventManager eventManager = new EventManager();

    private Map<Character, Character> prefixes = new ConcurrentHashMap<Character, Character>() {
        {
            put('o', '@');
            put('v', '+');
        }
    };
    private static final Pattern PREFIX_PATTERN = Pattern.compile("PREFIX=\\(([a-zA-Z]+)\\)([^ ]+)");
    private Map<Character, ChannelModeType> modes = ChannelModeType.getDefaultModes();
    private static final Pattern CHANMODES_PATTERN = Pattern.compile("CHANMODES=(([,A-Za-z]+)(,([,A-Za-z]+)){0,3})");

    IRCBot(Config config) {
        this.config = config;
        this.currentNick = this.requestedNick = this.goalNick = this.config.get(Config.NICK);
        this.manager = new BotManager();
        this.processor = new BotProcessor();
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
            if (this.connected) {
                this.sendRawLine("JOIN :" + channel, true);
            }
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
        return this.config.get(Config.BOT_NAME);
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
        this.sendRawLine(message, false);
    }

    @Override
    public void sendRawLine(String message, boolean priority) {
        Sanity.nullCheck(message, "Message cannot be null");
        this.outputHandler.queueMessage(message, priority);
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
        if (this.outputHandler != null) {
            this.outputHandler.setMessageDelay(delay);
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
        this.shutdownReason = reason != null ? reason : "";
        this.manager.interrupt();
        this.processor.interrupt();
    }

    /**
     * Queue up a line for processing.
     *
     * @param line line to be processed
     */
    void processLine(String line) {
        if (!this.pingCheck(line)) {
            this.processor.queue(line);
        }
    }

    private boolean pingCheck(String line) {
        if (line.startsWith("PING ")) {
            this.sendRawLine("PONG " + line.substring(5), true);
            return true;
        }
        return false;
    }

    private void run() {
        try {
            this.connect();
        } catch (final IOException e) {
            e.printStackTrace(); // TODO clean up error handling
            if ((this.inputHandler != null) && this.inputHandler.isAlive() && !this.inputHandler.isInterrupted()) {
                this.inputHandler.interrupt();
            }
            return;
        }
        while (!this.manager.isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                break;
            }
            if ((System.currentTimeMillis() - this.lastCheck) > 5000) {
                this.lastCheck = System.currentTimeMillis();
                if (this.inputHandler.timeSinceInput() > 250000) {
                    this.outputHandler.shutdown("Ping timeout! Reconnecting..."); // TODO event
                    this.inputHandler.shutdown();
                    try {
                        Thread.sleep(10000);
                    } catch (final InterruptedException e) {
                        break;
                    }
                    try {
                        this.connect();
                    } catch (final IOException e) {
                        // System.out.println("Unable to reconnect!");
                        // TODO log
                    }
                }
            }
        }
        this.outputHandler.shutdown(this.shutdownReason);
        this.inputHandler.shutdown();
    }

    private void connect() throws IOException {
        // I AM NOT CONNECTED
        this.connected = false;
        final Socket socket = new Socket();

        // Bind, if set
        if (this.config.get(Config.BIND_ADDRESS) != null) {
            try {
                socket.bind(this.config.get(Config.BIND_ADDRESS));
            } catch (final Exception e) {
                e.printStackTrace(); // TODO clean up error handling
            }
        }

        // Make connection and get ready to handle input and output
        socket.connect(this.config.get(Config.SERVER_ADDRESS));
        final BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final BufferedWriter outputWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // Need to start processing output immediately as we don't do this separately
        this.outputHandler = new IRCBotOutput(outputWriter, this.getName(), this.config.get(Config.MESSAGE_DELAY));
        this.outputHandler.start();

        // If the server has a password, send that along first
        if (this.config.get(Config.SERVER_PASSWORD) != null) {
            this.sendRawLine("PASS " + this.config.get(Config.SERVER_PASSWORD), true);
        }

        // Initial USER and NICK messages. Let's just assume we want +iw (send 8)
        this.sendRawLine("USER " + this.config.get(Config.USER) + " 8 * :" + this.config.get(Config.REAL_NAME), true);
        this.sendNickChange(this.goalNick);

        // Handle initial connection
        String line;
        while ((line = inputReader.readLine()) != null) { // TODO hacky
            if (this.pingCheck(line)) {
                continue;
            }
            // Still process lines, just with added handling of errors or connection success
            // WARNING: processes in this thread at this stage
            // TODO determine if this is acceptable or not
            try {
                this.handleLine(line);
            } catch (Throwable thrown) {
                // NOOP
            }
            final String[] split = line.split(" ");
            if (split.length > 3) {
                final String code = split[1];
                if (code.equals("004")) {
                    break;
                } else if (code.startsWith("5") || code.startsWith("4")) {
                    socket.close();
                    throw new RuntimeException("Could not log into the IRC server: " + line);
                }
            }
        }

        // Figure out auth
        if (this.authReclaim != null && !this.currentNick.equals(this.goalNick) && this.authType.isNickOwned()) {
            this.sendRawLine(this.authReclaim, true);
            this.sendNickChange(this.goalNick);
        }
        if (this.auth != null) {
            this.sendRawLine(this.auth, true);
        }

        // Join all channels
        for (final String channel : this.channels) {
            this.sendRawLine("JOIN :" + channel, true);
        }

        // Send those queued messages
        this.outputHandler.setLowPriorityEnabled();

        // Only after we're done handling the initial connection should we process normally
        this.inputHandler = new IRCBotInput(socket, inputReader, this);
        this.inputHandler.start();

        // If we haven't blown up by now, we're good
        this.connected = true;
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
                case 2: // Your host is...
                    break;
                // More stuff sent on startup
                case 3: // server created
                    break;
                case 4: // version / modes
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
                    if (!this.connected) {
                        this.sendNickChange(this.requestedNick + '`');
                    }
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
                                    this.sendRawLine("NOTICE " + actor.getName() + " :" + CTCPUtil.toCTCP(reply), false);
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
                        this.sendRawLine("JOIN " + split[3], false);
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
        this.sendRawLine("NICK " + newnick, true);
    }
}