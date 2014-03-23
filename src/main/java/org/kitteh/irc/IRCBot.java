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
import org.kitteh.irc.event.ChannelCTCPEvent;
import org.kitteh.irc.event.ChannelMessageEvent;
import org.kitteh.irc.event.PrivateCTCPEvent;
import org.kitteh.irc.event.PrivateMessageEvent;
import org.kitteh.irc.util.LCSet;
import org.kitteh.irc.util.Sanity;
import org.kitteh.irc.util.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    private class InputHandler extends Thread {
        private final Socket socket;
        private final BufferedReader bufferedReader;
        private boolean running = true;
        private long lastInputTime;

        private InputHandler(Socket socket, BufferedReader bufferedReader) {
            this.socket = socket;
            this.bufferedReader = bufferedReader;
            this.setName("Kitteh IRCBot Input (" + IRCBot.this.getName() + ")");
        }

        @Override
        public void run() {
            while (this.running) {
                try {
                    String line;
                    while (this.running && ((line = this.bufferedReader.readLine()) != null)) {
                        this.lastInputTime = System.currentTimeMillis();
                        try {
                            IRCBot.this.handleLine(line);
                        } catch (final Throwable thrown) {
                            // NOOP
                        }
                    }
                } catch (final IOException e) {
                    if (this.running) {
                        IRCBot.this.sendRawLine("PING " + (System.currentTimeMillis() / 1000), true);
                    }
                }
            }
            try {
                this.socket.close();
            } catch (final Exception e) {
            }
        }

        private void shutdown() {
            this.running = false;
            try {
                this.bufferedReader.close();
                this.socket.close();
            } catch (final IOException e) {
            }
        }

        private long timeSinceInput() {
            return System.currentTimeMillis() - this.lastInputTime;
        }
    }

    private class OutputHandler extends Thread {
        private final BufferedWriter bufferedWriter;
        private int delay = 1200; // TODO customizable
        private String quitReason;
        private boolean running = true;
        private boolean handleLowPriority = false;

        private OutputHandler(BufferedWriter bufferedWriter) {
            this.setName("Kitteh IRCBot Output (" + IRCBot.this.getName() + ")");
            this.bufferedWriter = bufferedWriter;
        }

        @Override
        public void run() {
            while (this.running) {
                while ((!this.handleLowPriority || IRCBot.this.lowPriorityQueue.isEmpty()) && IRCBot.this.highPriorityQueue.isEmpty()) {
                    if (!this.running) {
                        break;
                    }
                    try {
                        Thread.sleep(200);
                    } catch (final InterruptedException e) {
                        break;
                    }
                }
                String message = IRCBot.this.highPriorityQueue.poll();
                if (message == null) {
                    message = IRCBot.this.lowPriorityQueue.poll();
                }
                if (message != null) {
                    try {
                        this.bufferedWriter.write(message + "\r\n");
                        this.bufferedWriter.flush();
                    } catch (final IOException e) {
                    }
                }
                if (this.running) { // Delay!
                    try {
                        Thread.sleep(this.delay);
                    } catch (final InterruptedException e) {
                        break;
                    }
                }
            }
            try {
                this.bufferedWriter.write("QUIT :" + this.quitReason + "\r\n");
                this.bufferedWriter.flush();
                this.bufferedWriter.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        private void readyForLowPriority() {
            this.handleLowPriority = true;
        }

        private void shutdown(String message) {
            this.quitReason = message;
            IRCBot.this.highPriorityQueue.clear();
            IRCBot.this.lowPriorityQueue.clear();
            this.running = false;
        }

    }

    private final String botName;
    private final BotManager manager;

    private final InetSocketAddress bind;
    private final String server;
    private final int port;
    private final String user;
    private final String realName;
    private String nick;
    private String currentNick;

    private final LCSet channels = new LCSet();

    private AuthType authType;
    private String auth;
    private String authReclaim;

    private InputHandler inputHandler;
    private OutputHandler outputHandler;

    private String shutdownReason;
    private String serverinfo;

    private boolean connected;
    private long lastCheck;

    private final Queue<String> highPriorityQueue = new ConcurrentLinkedQueue<>();
    private final Queue<String> lowPriorityQueue = new ConcurrentLinkedQueue<>();

    private final EventManager eventManager = new EventManager();

    IRCBot(String botName, InetSocketAddress bind, String server, int port, String nick, String user, String realName) {
        this.botName = botName;
        this.bind = bind;
        this.server = server;
        this.port = port;
        this.currentNick = this.nick = nick;
        this.user = user;
        this.realName = realName;
        this.manager = new BotManager();
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
        return this.nick;
    }

    @Override
    public String getName() {
        return this.botName;
    }

    @Override
    public String getNick() {
        return this.currentNick;
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
        if (priority) {
            this.highPriorityQueue.add(message);
        } else {
            this.lowPriorityQueue.add(message);
        }
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
    public void setNick(String nick) {
        Sanity.nullCheck(nick, "Nick cannot be null");
        this.nick = nick.trim();
        this.sendNickChange(this.nick);
        this.currentNick = this.nick;
    }

    @Override
    public void shutdown(String reason) {
        if (reason == null) {
            reason = "";
        }
        this.shutdownReason = reason;
        this.manager.interrupt();
    }

    private void run() {
        try {
            this.connect();
        } catch (final IOException e) {
            e.printStackTrace();
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
        this.connected = false;
        final Socket socket = new Socket();
        if (this.bind != null) {
            try {
                socket.bind(this.bind);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        final InetSocketAddress target = new InetSocketAddress(this.server, this.port);
        socket.connect(target);
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.outputHandler = new OutputHandler(bufferedWriter);
        this.outputHandler.start();
        this.sendRawLine("USER " + this.user + " 8 * :" + this.realName, true);
        this.sendNickChange(this.nick);
        String line;
        while ((line = bufferedReader.readLine()) != null) { // TODO hacky
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
        if (!this.currentNick.equals(this.nick) && this.authType.isNickOwned()) {
            this.sendRawLine(this.authReclaim, true);
            this.sendNickChange(this.nick);
        }
        this.sendRawLine(this.auth, true);
        for (final String channel : this.channels) {
            this.sendRawLine("JOIN :" + channel, true);
        }
        this.outputHandler.readyForLowPriority();
        this.inputHandler = new InputHandler(socket, bufferedReader);
        this.inputHandler.start();
        this.connected = true;
    }

    private String handleColon(String string) {
        return string.startsWith(":") ? string.substring(1) : string;
    }

    private void handleLine(String line) throws Throwable {
        if ((line == null) || (line.length() == 0)) {
            return;
        }
        if (line.startsWith("PING ")) {
            this.sendRawLine("PONG " + line.substring(5), true);
            return;
        }
        final String[] split = line.split(" ");
        if ((split.length <= 1) || !split[0].startsWith(":")) {
            return; // Invalid!
        }
        final String actor = split[0].substring(1);
        if ((this.serverinfo == null) || actor.equals(this.serverinfo)) {
            switch (split[1]) {
                case "NOTICE": // NOTICE from server itself
                case "001": // Welcome
                case "002": // Your host is...
                    break;
                // More stuff sent on startup
                case "003": // server created
                    break;
                case "004": // version / modes
                    this.serverinfo = split[0].substring(1);
                    break;
                case "005": // Should be map, sometimes used to spew supported info
                case "250": // Highest connection count
                case "251": // There are X users
                case "252": // X IRC OPs
                case "253": // X unknown connections
                case "254": // X channels formed
                case "255": // X clients, X servers
                case "265": // Local users, max
                case "266": // global users, max
                case "372": // info, such as continued motd
                case "375": // motd start
                case "376": // motd end
                    break;
                // Channel info
                case "332": // Channel topic
                case "333": // Topic set by
                case "353": // Channel users list (/names). format is 353 nick = #channel :names
                case "366": // End of /names
                case "422": // MOTD missing
                    break;
                case "433":
                    if (!this.connected) {
                        this.currentNick = this.currentNick + '`';
                        this.sendNickChange(this.currentNick); // TODO This is bad. Handle nicer.
                    }
                    break;
            }
        } else {
            // CTCP
            if (split[1].equals("PRIVMSG") && (line.indexOf(":\u0001") > 0) && line.endsWith("\u0001")) { // TODO inaccurate
                final String ctcp = line.substring(line.indexOf(":\u0001") + 2, line.length() - 1);
                String reply = null;
                if (split[2].equalsIgnoreCase(this.nick)) {
                    if (ctcp.equals("VERSION")) {
                        reply = "VERSION I am Kitteh!";
                    } else if (ctcp.equals("TIME")) {
                        reply = "TIME " + new Date().toString();
                    } else if (ctcp.equals("FINGER")) {
                        reply = "FINGER om nom nom tasty finger";
                    } else if (ctcp.startsWith("PING ")) {
                        reply = ctcp;
                    }
                    PrivateCTCPEvent event = new PrivateCTCPEvent(Actor.getActor(actor), ctcp, reply);
                    this.eventManager.callEvent(event);
                    reply = event.getReply();

                } else if (this.channels.contains(split[2])) {
                    this.eventManager.callEvent(new ChannelCTCPEvent(Actor.getActor(actor), (Channel) Actor.getActor(split[2]), ctcp));
                }
                if (reply != null) {
                    this.sendRawLine("NOTICE " + StringUtil.getNick(actor) + " :\u0001" + reply + "\u0001", false);
                }
                return;
            }
            switch (split[1]) {
                case "NOTICE":
                    final String notice = this.handleColon(StringUtil.combineSplit(split, 3));
                    // TODO event
                    break;
                case "PRIVMSG":
                    final String message = this.handleColon(StringUtil.combineSplit(split, 3));
                    if (split[2].equalsIgnoreCase(this.currentNick)) {
                        this.eventManager.callEvent(new PrivateMessageEvent(Actor.getActor(actor), message));
                    } else if (this.channels.contains(split[2])) {
                        this.eventManager.callEvent(new ChannelMessageEvent(Actor.getActor(actor), (Channel) Actor.getActor(split[2]), message));
                    }
                    break;
                case "MODE":
                    // System.out.println(split[2] + ": " + StringUtil.getNick(actor) + " " + split[1] + " " + StringUtil.combineSplit(split, 3)); TODO EVENT
                    break;
                case "JOIN":
                case "PART":
                case "QUIT":
                    break;
                case "KICK":
                    // System.out.println(split[2] + ": " + StringUtil.getNick(actor) + " kicked " + split[3] + ": " + this.handleColon(StringUtil.combineSplit(split, 4))); TODO EVENT
                    break;
                case "NICK":
                    break;
                case "INVITE":
                    if (split[2].equals(this.nick) && this.channels.contains(split[3])) {
                        this.sendRawLine("JOIN " + split[3], false);
                    }
                    break;
            }
        }
    }

    private void sendNickChange(String newnick) {
        this.sendRawLine("NICK " + newnick, true);
        this.currentNick = newnick;
    }
}