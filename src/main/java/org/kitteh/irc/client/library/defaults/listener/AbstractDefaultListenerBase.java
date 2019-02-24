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
package org.kitteh.irc.client.library.defaults.listener;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.event.channel.ChannelCtcpEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTargetedCtcpEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.helper.ClientEvent;
import org.kitteh.irc.client.library.event.helper.ClientReceiveServerMessageEvent;
import org.kitteh.irc.client.library.event.user.PrivateCtcpQueryEvent;
import org.kitteh.irc.client.library.event.user.PrivateCtcpReplyEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.ActorTracker;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.util.CtcpUtil;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.Date;
import java.util.Optional;

/**
 * A base for listening to server message events.
 */
public class AbstractDefaultListenerBase {
    private final Client.WithManagement client;

    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public AbstractDefaultListenerBase(Client.@NonNull WithManagement client) {
        this.client = client;
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).toString();
    }

    /**
     * Gets the client.
     *
     * @return client
     */
    protected Client.@NonNull WithManagement getClient() {
        return this.client;
    }

    /**
     * Fires an event. Convenience method.
     *
     * @param event event to fire
     * @see EventManager#callEvent(Object)
     */
    protected void fire(@NonNull ClientEvent event) {
        this.client.getEventManager().callEvent(event);
    }

    /**
     * Processes a CTCP message.
     *
     * @param event the currently handled event
     */
    protected void ctcp(ClientReceiveCommandEvent event) {
        final String ctcpMessage = CtcpUtil.fromCtcp(event.getParameters().get(1));
        final MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().get(0));
        User user = (User) event.getActor();
        switch (event.getCommand()) {
            case "NOTICE":
                if (messageTargetInfo instanceof MessageTargetInfo.Private) {
                    this.fire(new PrivateCtcpReplyEvent(this.getClient(), event.getOriginalMessages(), user, event.getParameters().get(0), ctcpMessage));
                }
                break;
            case "PRIVMSG":
                if (messageTargetInfo instanceof MessageTargetInfo.Private) {
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
                    PrivateCtcpQueryEvent ctcpEvent = new PrivateCtcpQueryEvent(this.getClient(), event.getOriginalMessages(), user, event.getParameters().get(0), ctcpMessage, reply);
                    this.fire(ctcpEvent);
                    Optional<String> replyMessage = ctcpEvent.getReply();
                    if (ctcpEvent.isToClient()) {
                        replyMessage.ifPresent(message -> this.getClient().sendRawLine("NOTICE " + user.getNick() + " :" + CtcpUtil.toCtcp(message)));
                    }
                } else if (messageTargetInfo instanceof MessageTargetInfo.ChannelInfo) {
                    MessageTargetInfo.ChannelInfo channelInfo = (MessageTargetInfo.ChannelInfo) messageTargetInfo;
                    this.fire(new ChannelCtcpEvent(this.getClient(), event.getOriginalMessages(), user, channelInfo.getChannel(), ctcpMessage));
                } else if (messageTargetInfo instanceof MessageTargetInfo.TargetedChannel) {
                    MessageTargetInfo.TargetedChannel channelInfo = (MessageTargetInfo.TargetedChannel) messageTargetInfo;
                    this.fire(new ChannelTargetedCtcpEvent(this.getClient(), event.getOriginalMessages(), user, channelInfo.getChannel(), channelInfo.getPrefix(), ctcpMessage));
                }
                break;
        }
    }

    /**
     * Fires an exception in processing a server message event.
     *
     * @param event event causing trouble
     * @param reason reason for the trouble
     */
    protected void trackException(@NonNull ClientReceiveServerMessageEvent event, @NonNull String reason) {
        this.client.getExceptionListener().queue(new KittehServerMessageException(event.getServerMessage(), reason));
    }

    /**
     * Gets the actor tracker. Convenience method.
     *
     * @return actor tracker
     * @see Client.WithManagement#getActorTracker()
     */
    protected @NonNull ActorTracker getTracker() {
        return this.client.getActorTracker();
    }

    /**
     * Information about the target of a message.
     */
    protected static class MessageTargetInfo {
        /**
         * A targeted channel.
         */
        public static class ChannelInfo extends MessageTargetInfo {
            private final Channel channel;

            private ChannelInfo(Channel channel) {
                this.channel = channel;
            }

            /**
             * Gets the channel.
             *
             * @return channel
             */
            public @NonNull Channel getChannel() {
                return this.channel;
            }

            @Override
            public @NonNull String toString() {
                return new ToStringer(this).toString();
            }
        }

        /**
         * A channel targeted with a prefix.
         */
        public static class TargetedChannel extends MessageTargetInfo {
            private final Channel channel;
            private final ChannelUserMode prefix;

            private TargetedChannel(Channel channel, ChannelUserMode prefix) {
                this.channel = channel;
                this.prefix = prefix;
            }

            /**
             * Gets the channel.
             *
             * @return channel
             */
            public @NonNull Channel getChannel() {
                return this.channel;
            }

            /**
             * Gets the prefix.
             *
             * @return prefix
             */
            public @NonNull ChannelUserMode getPrefix() {
                return this.prefix;
            }

            @Override
            public @NonNull String toString() {
                return new ToStringer(this).toString();
            }
        }

        /**
         * A private message received.
         */
        public static class Private extends MessageTargetInfo {
            static final Private INSTANCE = new Private();

            private Private() {
            }

            @Override
            public @NonNull String toString() {
                return new ToStringer(this).toString();
            }
        }
    }

    /**
     * Gets the relevant info about a message target.
     *
     * @param target target in string form
     * @return target in specific object form
     */
    protected @NonNull MessageTargetInfo getTypeByTarget(@NonNull String target) {
        Optional<Channel> channel = this.getTracker().getChannel(target);
        Optional<ChannelUserMode> prefix = this.getClient().getServerInfo().getTargetedChannelInfo(target);
        if (prefix.isPresent()) {
            return new MessageTargetInfo.TargetedChannel(this.getTracker().getChannel(target.substring(1)).get(), prefix.get());
        } else if (channel.isPresent()) {
            return new MessageTargetInfo.ChannelInfo(channel.get());
        }
        return MessageTargetInfo.Private.INSTANCE;
    }
}
