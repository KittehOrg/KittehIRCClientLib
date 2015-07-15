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

import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.MessageReceiver;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.util.CISet;
import org.kitteh.irc.client.library.util.QueueProcessingThread;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
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

    private final String[] pingPurr = new String[]{"MEOW", "MEOW!", "PURR", "PURRRRRRR"};
    private int pingPurrCount;

    private final Config config;
    private final InputProcessor processor;
    private IRCServerInfo serverInfo = new IRCServerInfo(this);

    private String goalNick;
    private String currentNick;
    private String requestedNick;

    private final Set<String> channelsIntended = new CISet(this);

    private NettyManager.ClientConnection connection;

    private final AuthManager authManager = new IRCAuthManager(this);
    private final IRCCapabilityManager capabilityManager = new IRCCapabilityManager();
    private final EventManager eventManager = new IRCEventManager(this);

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
        this.eventManager.registerEventListener(new IRCEventListener(this));
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
    public IRCCapabilityManager getCapabilityManager() {
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
        if (this.actorProvider.getTrackedChannelNames().contains(channel.getName())) {
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
    void startSending() {
        this.connection.startSending();
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
}