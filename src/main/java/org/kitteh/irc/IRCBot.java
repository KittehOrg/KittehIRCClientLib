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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.kitteh.irc.localization.Localization;
import org.kitteh.irc.util.StringUtil;

public final class IRCBot extends Thread {
    public enum AuthType {
        NICKSERV,
        GAMESURGE(false);

        private final boolean nicksOwned;

        private AuthType() {
            this.nicksOwned = true;
        }

        private AuthType(boolean nickOwned) {
            this.nicksOwned = nickOwned;
        }

        /**
         * Are nicks owned on this network?
         * 
         * @return
         */
        public boolean isNickOwned() {
            return this.nicksOwned;
        }
    }

    private class InputHandler extends Thread {
        private final IRCBot bot;
        private final Socket socket;
        private final BufferedReader bufferedReader;
        private boolean running = true;
        private long lastInputTime;

        private InputHandler(IRCBot bot, Socket socket, BufferedReader bufferedReader) {
            this.bot = bot;
            this.socket = socket;
            this.bufferedReader = bufferedReader;
            this.setName("Kitteh IRCBot Input (" + bot.getName() + ")");
        }

        @Override
        public void run() {
            while (this.running) {
                try {
                    String line = null;
                    while (this.running && ((line = this.bufferedReader.readLine()) != null)) {
                        this.lastInputTime = System.currentTimeMillis();
                        try {
                            this.bot.handleLine(line);
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (final IOException e) {
                    if (this.running) {
                        this.bot.sendRawLine("PING " + (System.currentTimeMillis() / 1000), true);
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
        // private int delay = 1200; // Delay disabled while in development :3
        private String quitReason;
        private boolean running = true;
        private boolean handleLowPriority = false;

        private OutputHandler(IRCBot bot, BufferedWriter bufferedWriter) {
            this.setName("Kitteh IRCBot Output (" + bot.getName() + ")");
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
                String message = IRCBot.this.highPriorityQueue.size() > 0 ? IRCBot.this.highPriorityQueue.remove(0) : null;
                if ((message == null) && this.handleLowPriority && (IRCBot.this.lowPriorityQueue.size() > 0)) {
                    message = IRCBot.this.lowPriorityQueue.remove(0);
                }
                if (message != null) { // You know, just in case
                    try {
                        this.bufferedWriter.write(message + "\r\n");
                        this.bufferedWriter.flush();
                    } catch (final IOException e) {
                    }
                }
                /*if (this.running) { // Delay!
                    try {
                        Thread.sleep(this.delay);
                    } catch (final InterruptedException e) {
                        break;
                    }
                }*/
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

    private final InetSocketAddress bind;
    private final String server;
    private final int port;
    private final String botName;
    private String nick = "Kitteh";
    private String currentNick = "Kitteh";
    private InputHandler inputHandler;
    private final String ircUser = "kitteh";
    private final String ircName = "Meow meow meow";
    private OutputHandler outputHandler;
    private String onNormal;
    private String onFail;
    private String shutdownReason;
    private String serverinfo;
    private AuthType authType;
    private long lastCheck;
    private final List<String> highPriorityQueue = Collections.synchronizedList(new ArrayList<String>());
    private final List<String> lowPriorityQueue = Collections.synchronizedList(new ArrayList<String>());
    private final List<String> channels = new ArrayList<>();
    private boolean connected;
    private final String locale = "en"; // TODO - set locale, call load method etc

    // TODO HACK
    private final java.util.Set<HackyTemp> hacks = Collections.synchronizedSet(new java.util.HashSet<HackyTemp>());

    public void addHack(HackyTemp temp) {
        this.hacks.add(temp);
    } // TODO HACK

    public IRCBot(String botName, String server, int port, String nick) {
        this(botName, null, server, port, nick);
    }

    public IRCBot(String botName, String bind, String server, int port, String nick) {
        if (bind == null) {
            this.bind = null;
        } else {
            InetSocketAddress inetSocketAddress = null;
            try {
                inetSocketAddress = new InetSocketAddress(InetAddress.getByName(bind), 0);
            } catch (final Exception e) {
            }
            this.bind = inetSocketAddress;
        }
        this.server = server;
        this.port = port;
        this.botName = botName;
        this.nick = nick;
        this.setName("Kitteh IRCBot Main (" + botName + ")");
    }

    public void addChannel(String... channel) {
        this.channels.addAll(Arrays.asList(channel));
    }

    @Override
    public void run() {
        try {
            this.connect();
        } catch (final IOException e) {
            e.printStackTrace();
            if ((this.inputHandler != null) && this.inputHandler.isAlive() && !this.inputHandler.isInterrupted()) {
                this.inputHandler.interrupt();
            }
            return;
        }
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                break;
            }
            if ((System.currentTimeMillis() - this.lastCheck) > 5000) {
                this.lastCheck = System.currentTimeMillis();
                if (this.inputHandler.timeSinceInput() > 250000) {
                    this.outputHandler.shutdown(Localization.BOT_PINGTIMEOUT.locale(this.locale).get());
                    this.inputHandler.shutdown();
                    try {
                        Thread.sleep(10000);
                    } catch (final InterruptedException e) {
                        break;
                    }
                    try {
                        this.connect();
                    } catch (final IOException e) {
                    }
                }
            }
        }
        this.outputHandler.shutdown(this.shutdownReason);
        this.inputHandler.shutdown();
    }

    public void sendRawLine(String msg, boolean priority) {
        this.queue(msg, priority);
    }

    public void setAuth(AuthType type, String nick, String pass) {
        this.authType = type;
        switch (type) {
            case GAMESURGE:
                this.onNormal = "PRIVMSG AuthServ@services.gamesurge.net :auth " + nick + " " + pass;
                this.onFail = "";
                break;
            default:
                this.onNormal = "PRIVMSG NickServ :identify " + pass;
                this.onFail = "PRIVMSG NickServ :ghost " + nick + " " + pass;
        }
    }

    public void setNick(String nick) {
        this.nick = nick.trim();
        this.sendNickChange(this.nick);
    }

    public void shutdown(String shutdownReason) {
        this.shutdownReason = shutdownReason;
        this.interrupt();
    }

    private void connect() throws IOException {
        this.connected = false;
        final Socket socket = new Socket();
        if (this.bind != null) {
            try {
                socket.bind(this.bind);
                System.out.println("Bound to " + socket.getLocalAddress() + " " + socket.getLocalPort());
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        final InetSocketAddress target = new InetSocketAddress(this.server, this.port);
        System.out.println("Connecting to " + target.toString());
        socket.connect(target);
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.outputHandler = new OutputHandler(this, bufferedWriter);
        this.outputHandler.start();
        this.sendRawLine("USER " + this.ircUser + " 8 * :" + this.ircName, true);
        this.sendNickChange(this.nick);
        String line = null;
        while ((line = bufferedReader.readLine()) != null) { // TODO hacky
            this.handleLine(line);
            final String[] split = line.split(" ");
            if (split.length > 3) {
                final String code = split[1];
                if (code.equals("004")) {
                    break;
                } else if (code.equals("433") || code.equals("422")) { // TODO ugly handling of valid errors we work around
                } else if (code.startsWith("5") || code.startsWith("4")) {
                    socket.close();
                    throw new RuntimeException("Could not log into the IRC server: " + line);
                }
            }
        }
        if (!this.currentNick.equals(this.nick) && this.authType.isNickOwned()) {
            this.sendRawLine(this.onFail, true);
            this.sendNickChange(this.nick);
        }
        this.sendRawLine(this.onNormal, true);
        for (final String channel : this.channels) {
            this.sendRawLine("JOIN :" + channel, true);
        }
        this.outputHandler.readyForLowPriority();
        this.inputHandler = new InputHandler(this, socket, bufferedReader);
        this.inputHandler.start();
        this.connected = true;
    }

    private String getNickFromActor(String actor) {
        final int i = actor.indexOf("!");
        return actor.substring(0, i > 0 ? i : actor.length());
    }

    private String handleColon(String string) {
        return string.startsWith(":") ? string.substring(1) : string;
    }

    private void handleLine(String line) {
        if ((line == null) || (line.length() == 0)) {
            return;
        }
        if (line.startsWith("PING ")) {
            this.queueInstant("PONG " + line.substring(5));
            return;
        }
        final String[] split = line.split(" ");
        if ((split.length == 1) || !split[0].startsWith(":")) {
            return; // Invalid!
        }
        final String actor = split[0].substring(1);
        if ((this.serverinfo == null) || actor.equals(this.serverinfo)) {
            switch (split[1]) {
                case "NOTICE": // NOTICE from server itself
                case "001": // Welcome
                case "002": // Your host is...
                    System.out.println(this.handleColon(StringUtil.combineSplit(split, 3)));
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
                        this.sendNickChange(this.currentNick + '`'); // TODO This is bad. Handle nicer.
                    }
                    break;
                default:
                    System.out.println("Unknown: " + line);
            }
        } else {
            // CTCP
            if (split[1].equals("PRIVMSG") && (line.indexOf(":\u0001") > 0) && line.endsWith("\u0001")) { // TODO inaccurate
                final String ctcp = line.substring(line.indexOf(":\u0001") + 2, line.length() - 1);
                String reply = null;
                if (ctcp.equals("VERSION")) {
                    reply = "VERSION " + Localization.CTCP_VERSION.locale(this.locale).format("Kitteh");
                } else if (ctcp.equals("TIME")) {
                    reply = "TIME " + Localization.CTCP_TIME.locale(this.locale).format(new Date().toString());
                } else if (ctcp.equals("FINGER")) {
                    reply = "FINGER " + Localization.CTCP_FINGER.locale(this.locale).get();
                } else if (ctcp.startsWith("PING ")) {
                    reply = ctcp;
                } else if (ctcp.startsWith("ACTION ")) {
                    System.out.println("<" + split[2] + "> * " + this.getNickFromActor(actor) + " " + ctcp.substring(7));
                    // TODO HACK
                    final String channel = split[2];
                    final String nick = this.getNickFromActor(actor);
                    if (this.channels.contains(channel)) {
                        for (final HackyTemp temp : this.hacks) {
                            temp.action(channel, nick, ctcp.substring(7));
                        }
                    }
                    // TODO HACK
                }
                if (reply != null) {
                    this.sendRawLine("NOTICE " + this.getNickFromActor(actor) + " :\u0001" + reply + "\u0001", false);
                }
                return;
            }
            switch (split[1]) {
                case "NOTICE":
                case "PRIVMSG":
                    final String message = this.handleColon(StringUtil.combineSplit(split, 3));
                    System.out.println((split[1].equals("NOTICE") ? "N" : "") + "<" + this.getNickFromActor(actor) + "->" + split[2] + "> " + message);
                    // TODO HACK
                    final String channel = split[2];
                    final String nick = this.getNickFromActor(actor);
                    if (this.channels.contains(channel)) {
                        for (final HackyTemp temp : this.hacks) {
                            temp.message(channel, nick, message);
                        }
                    }
                    // TODO HACK
                    break;
                case "MODE":
                    System.out.println(split[2] + ": " + this.getNickFromActor(actor) + " " + split[1] + " " + StringUtil.combineSplit(split, 3));
                    break;
                case "JOIN":
                case "PART":
                case "QUIT":
                    break;
                case "KICK":
                    System.out.println(split[2] + ": " + Localization.CHANNEL_KICK.locale(this.locale).format(this.getNickFromActor(actor), split[3]) + ": " + this.handleColon(StringUtil.combineSplit(split, 4)));
                    break;
                case "NICK":
                    break;
                case "INVITE":
                    if (split[2].equals(this.nick) && this.channels.contains(split[3])) {
                        this.sendRawLine("JOIN " + split[3], false);
                    }
                    break;
                default:
                    System.out.println("Unknown action: " + line);
            }
        }
    }

    private void queue(String msg, boolean priority) {
        if (priority) {
            this.highPriorityQueue.add(msg);
        } else {
            this.lowPriorityQueue.add(msg);
        }
    }

    private void queueInstant(String msg) {
        this.highPriorityQueue.add(0, msg);
    }

    private void sendNickChange(String newnick) {
        this.sendRawLine("NICK " + newnick, true);
        this.currentNick = newnick;
    }

    public String getCurrentNick() {
        return this.currentNick;
    }

    public String getBotName() {
        return this.botName;
    }
}