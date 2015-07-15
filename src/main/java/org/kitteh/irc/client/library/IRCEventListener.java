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
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.ChannelUserMode;
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
import org.kitteh.irc.client.library.util.CommandFilter;
import org.kitteh.irc.client.library.util.NumericFilter;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@net.engio.mbassy.listener.Listener(references = References.Strong)
class IRCEventListener {
    private final InternalClient client;

    IRCEventListener(InternalClient client) {
        this.client = client;
    }

    @NumericFilter(1)
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void welcome(ClientReceiveNumericEvent event) {
        this.client.setCurrentNick(event.getArgs()[0]);
    }

    @NumericFilter(4)
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void version(ClientReceiveNumericEvent event) {
        try {
            this.client.getAuthManager().authenticate();
        } catch (IllegalStateException | UnsupportedOperationException ignored) {
        }
        this.client.resetServerInfo();
        this.client.getServerInfo().setServerAddress(event.getArgs()[1]);
        this.client.getServerInfo().setServerVersion(event.getArgs()[2]);
        this.client.getEventManager().callEvent(new ClientConnectedEvent(this.client, event.getServer(), this.client.getServerInfo()));
        this.client.startSending();
    }

    @NumericFilter(5) // WHO completed
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void iSupport(ClientReceiveNumericEvent event) {
        for (int i = 1; i < event.getArgs().length; i++) {
            IRCISupport.handle(event.getArgs()[i], this.client);
        }
    }

    @NumericFilter(352) // WHO
    @NumericFilter(354) // WHOX
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void who(ClientReceiveNumericEvent event) {
        if (this.client.getServerInfo().isValidChannel(event.getArgs()[1])) {
            final String channelName = event.getArgs()[1];
            final String ident = event.getArgs()[2];
            final String host = event.getArgs()[3];
            final String server = event.getArgs()[4];
            final String nick = event.getArgs()[5];
            final ActorProvider.IRCUser user = (ActorProvider.IRCUser) this.client.getActorProvider().getActor(nick + '!' + ident + '@' + host);
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
            final ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(channelName);
            final Set<ChannelUserMode> modes = new HashSet<>();
            for (char prefix : status.substring(1).toCharArray()) {
                if (prefix == 'G') {
                    user.setAway(true);
                    continue;
                }
                for (ChannelUserMode mode : this.client.getServerInfo().getChannelUserModes()) {
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
        ActorProvider.IRCChannel whoChannel = this.client.getActorProvider().getChannel(event.getArgs()[1]);
        if (whoChannel != null) {
            whoChannel.setListReceived();
            this.client.getEventManager().callEvent(new ChannelUsersUpdatedEvent(this.client, whoChannel.snapshot()));
        }
    }

    @NumericFilter(332) // Topic
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void topic(ClientReceiveNumericEvent event) {
        ActorProvider.IRCChannel topicChannel = this.client.getActorProvider().getChannel(event.getArgs()[1]);
        if (topicChannel != null) {
            topicChannel.setTopic(event.getArgs()[2]);
        }
    }

    @NumericFilter(333) // Topic info
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void topicInfo(ClientReceiveNumericEvent event) {
        ActorProvider.IRCChannel topicSetChannel = this.client.getActorProvider().getChannel(event.getArgs()[1]);
        if (topicSetChannel != null) {
            topicSetChannel.setTopic(Long.parseLong(event.getArgs()[3]) * 1000, this.client.getActorProvider().getActor(event.getArgs()[2]).snapshot());
            this.client.getEventManager().callEvent(new ChannelTopicEvent(this.client, topicSetChannel.snapshot(), false));
        }
    }

    @NumericFilter(353) // NAMES
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void names(ClientReceiveNumericEvent event) {
        if (this.client.getServerInfo().isValidChannel(event.getArgs()[2])) {
            ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs()[2]);
            List<ChannelUserMode> channelUserModes = this.client.getServerInfo().getChannelUserModes();
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
        if (this.client.getServerInfo().isValidChannel(event.getArgs()[1])) {
            ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs()[1]);
            this.client.getEventManager().callEvent(new ChannelNamesUpdatedEvent(this.client, channel.snapshot()));
        }
    }

    @NumericFilter(431) // No nick given
    @NumericFilter(432) // Erroneous nickname
    @NumericFilter(433) // Nick in use
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void nickInUse(ClientReceiveNumericEvent event) {
        NickRejectedEvent nickRejectedEvent = new NickRejectedEvent(this.client, this.client.getRequestedNick(), this.client.getRequestedNick() + '`');
        this.client.getEventManager().callEvent(nickRejectedEvent);
        this.client.sendNickChange(nickRejectedEvent.getNewNick());
    }

    @NumericFilter(710) // Knock
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void knock(ClientReceiveNumericEvent event) {
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs()[1]);
        ActorProvider.IRCUser user = (ActorProvider.IRCUser) this.client.getActorProvider().getActor(event.getArgs()[2]);
        this.client.getEventManager().callEvent(new ChannelKnockEvent(this.client, channel.snapshot(), user.snapshot()));
    }

    @CommandFilter("NOTICE")
    @CommandFilter("PRIVMSG")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void ctcp(ClientReceiveCommandEvent event) {
        if (CTCPUtil.isCTCP(event.getArgs()[1])) {
            final String ctcpMessage = CTCPUtil.fromCTCP(event.getArgs()[1]);
            final MessageTarget messageTarget = MessageTarget.getTypeByTarget(this.client, event.getArgs()[0]);
            User user = (User) event.getActor();
            switch (event.getCommand()) {
                case "NOTICE":
                    if (messageTarget == MessageTarget.PRIVATE) {
                        this.client.getEventManager().callEvent(new PrivateCTCPReplyEvent(this.client, user, ctcpMessage));
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
                            PrivateCTCPQueryEvent ctcpEvent = new PrivateCTCPQueryEvent(this.client, user, ctcpMessage, reply);
                            this.client.getEventManager().callEvent(ctcpEvent);
                            String eventReply = ctcpEvent.getReply();
                            if (eventReply != null) {
                                this.client.sendRawLine("NOTICE " + user.getNick() + " :" + CTCPUtil.toCTCP(eventReply));
                            }
                            break;
                        case CHANNEL:
                            this.client.getEventManager().callEvent(new ChannelCTCPEvent(this.client, user, this.client.getActorProvider().getChannel(event.getArgs()[0]).snapshot(), ctcpMessage));
                            break;
                        case CHANNEL_TARGETED:
                            this.client.getEventManager().callEvent(new ChannelTargetedCTCPEvent(this.client, user, this.client.getActorProvider().getChannel(event.getArgs()[0].substring(1)).snapshot(), this.client.getServerInfo().getTargetedChannelInfo(event.getArgs()[0]), ctcpMessage));
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
        List<CapabilityState> capabilityStateList = Arrays.stream(event.getArgs()[2].split(" ")).map(IRCCapabilityManager.IRCCapabilityState::new).collect(Collectors.toList());
        switch (event.getArgs()[1].toLowerCase()) {
            case "ack":
                this.client.getCapabilityManager().updateCapabilities(capabilityStateList);
                responseEvent = new CapabilitiesAcknowledgedEvent(this.client, this.client.getCapabilityManager().isNegotiating(), capabilityStateList);
                this.client.getEventManager().callEvent(responseEvent);
                break;
            case "list":
                this.client.getCapabilityManager().setCapabilities(capabilityStateList);
                this.client.getEventManager().callEvent(new CapabilitiesListEvent(this.client, capabilityStateList));
                break;
            case "ls":
                this.client.getCapabilityManager().setSupportedCapabilities(capabilityStateList);
                responseEvent = new CapabilitiesSupportedListEvent(this.client, this.client.getCapabilityManager().isNegotiating(), capabilityStateList);
                Set<String> capabilities = capabilityStateList.stream().map(CapabilityState::getCapabilityName).collect(Collectors.toCollection(HashSet::new));
                capabilities.retainAll(Arrays.asList("account-notify", "away-notify", "extended-join", "multi-prefix"));
                if (!capabilities.isEmpty()) {
                    CapabilityRequestCommand capabilityRequestCommand = new CapabilityRequestCommand(this.client);
                    capabilities.forEach(capabilityRequestCommand::requestEnable);
                    capabilityRequestCommand.execute();
                }
                this.client.getEventManager().callEvent(responseEvent);
                break;
            case "nak":
                this.client.getCapabilityManager().updateCapabilities(capabilityStateList);
                responseEvent = new CapabilitiesRejectedEvent(this.client, this.client.getCapabilityManager().isNegotiating(), capabilityStateList);
                this.client.getEventManager().callEvent(responseEvent);
                break;
        }
        if (responseEvent != null) {
            if (responseEvent.isNegotiating() && responseEvent.isEndingNegotiation()) {
                this.client.sendRawLineImmediately("CAP END");
                this.client.getCapabilityManager().endNegotiation();
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
        this.client.getActorProvider().trackUserAccount(((User) event.getActor()).getNick(), account);
    }

    @CommandFilter("AWAY")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void away(ClientReceiveCommandEvent event) {
        this.client.getActorProvider().trackUserAway(((User) event.getActor()).getNick(), event.getArgs().length > 0);
    }

    @CommandFilter("NOTICE")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void notice(ClientReceiveCommandEvent event) {
        switch (MessageTarget.getTypeByTarget(this.client, event.getArgs()[0])) {
            case CHANNEL:
                this.client.getEventManager().callEvent(new ChannelNoticeEvent(this.client, (User) event.getActor(), this.client.getActorProvider().getChannel(event.getArgs()[0]).snapshot(), event.getArgs()[1]));
                break;
            case CHANNEL_TARGETED:
                this.client.getEventManager().callEvent(new ChannelTargetedNoticeEvent(this.client, (User) event.getActor(), this.client.getActorProvider().getChannel(event.getArgs()[0].substring(1)).snapshot(), this.client.getServerInfo().getTargetedChannelInfo(event.getArgs()[0]), event.getArgs()[1]));
                break;
            case PRIVATE:
                this.client.getEventManager().callEvent(new PrivateNoticeEvent(this.client, (User) event.getActor(), event.getArgs()[1]));
                break;
        }
    }

    @CommandFilter("PRIVMSG")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void privmsg(ClientReceiveCommandEvent event) {
        if (CTCPUtil.isCTCP(event.getArgs()[1])) {
            return;
        }
        switch (MessageTarget.getTypeByTarget(this.client, event.getArgs()[0])) {
            case CHANNEL:
                this.client.getEventManager().callEvent(new ChannelMessageEvent(this.client, (User) event.getActor(), this.client.getActorProvider().getChannel(event.getArgs()[0]).snapshot(), event.getArgs()[1]));
                break;
            case CHANNEL_TARGETED:
                this.client.getEventManager().callEvent(new ChannelTargetedMessageEvent(this.client, (User) event.getActor(), this.client.getActorProvider().getChannel(event.getArgs()[0].substring(1)).snapshot(), this.client.getServerInfo().getTargetedChannelInfo(event.getArgs()[0]), event.getArgs()[1]));
                break;
            case PRIVATE:
                this.client.getEventManager().callEvent(new PrivateMessageEvent(this.client, (User) event.getActor(), event.getArgs()[1]));
                break;
        }
    }

    @CommandFilter("MODE")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void mode(ClientReceiveCommandEvent event) {
        if (MessageTarget.getTypeByTarget(this.client, event.getArgs()[0]) == MessageTarget.CHANNEL) {
            ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs()[0]);
            List<ChannelUserMode> channelUserModes = this.client.getServerInfo().getChannelUserModes();
            Map<Character, ChannelModeType> channelModes = this.client.getServerInfo().getChannelModes();
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
                            this.client.getEventManager().callEvent(new ChannelModeEvent(this.client, event.getActor(), channel.snapshot(), add, modeChar, prefixMode, target));
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
            ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs()[0]);
            ActorProvider.IRCUser user = (ActorProvider.IRCUser) this.client.getActorProvider().getActor(event.getActor().getName());
            channel.trackUser(user, null);
            if (user.getNick().equals(this.client.getNick())) {
                this.client.getActorProvider().channelTrack(channel);
                this.client.sendRawLine("WHO " + channel.getName() + (this.client.getServerInfo().hasWhoXSupport() ? " %cuhsnfar" : ""));
            }
            if (event.getArgs().length > 2) {
                if (!"*".equals(event.getArgs()[1])) {
                    user.setAccount(event.getArgs()[1]);
                }
                user.setRealName(event.getArgs()[2]);
            }
            this.client.getEventManager().callEvent(new ChannelJoinEvent(this.client, channel.snapshot(), user.snapshot()));
        }
    }

    @CommandFilter("PART")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void part(ClientReceiveCommandEvent event) {
        if (event.getActor() instanceof User) { // Just in case
            ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs()[0]);
            ActorProvider.IRCUser user = this.client.getActorProvider().getUser((((User) event.getActor()).getNick()));
            this.client.getEventManager().callEvent(new ChannelPartEvent(this.client, channel.snapshot(), user.snapshot(), (event.getArgs().length > 1) ? event.getArgs()[1] : ""));
            channel.trackUserPart(user.getNick());
            if (user.getNick().equals(this.client.getNick())) {
                this.client.getActorProvider().channelUntrack(channel);
            }
        }
    }

    @CommandFilter("QUIT")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void quit(ClientReceiveCommandEvent event) {
        if (event.getActor() instanceof User) { // Just in case
            this.client.getEventManager().callEvent(new UserQuitEvent(this.client, (User) event.getActor(), (event.getArgs().length > 0) ? event.getArgs()[0] : ""));
            this.client.getActorProvider().trackUserQuit(((User) event.getActor()).getNick());
        }
    }

    @CommandFilter("KICK")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void kick(ClientReceiveCommandEvent event) {
        ActorProvider.IRCChannel kickedChannel = this.client.getActorProvider().getChannel(event.getArgs()[0]);
        ActorProvider.IRCUser kickedUser = this.client.getActorProvider().getUser(event.getArgs()[1]);
        this.client.getEventManager().callEvent(new ChannelKickEvent(this.client, kickedChannel.snapshot(), (User) event.getActor(), kickedUser.snapshot(), (event.getArgs().length > 2) ? event.getArgs()[2] : ""));
        kickedChannel.trackUserPart(event.getArgs()[1]);
        if (event.getArgs()[1].equals(this.client.getNick())) {
            this.client.getActorProvider().channelUntrack(kickedChannel);
        }
    }

    @CommandFilter("NICK")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void nick(ClientReceiveCommandEvent event) {
        if (event.getActor() instanceof User) {
            ActorProvider.IRCUser user = this.client.getActorProvider().getUser(((User) event.getActor()).getNick());
            User oldUser = user.snapshot();
            if (user.getNick().equals(this.client.getNick())) {
                this.client.setCurrentNick(event.getArgs()[0]);
            }
            this.client.getActorProvider().trackUserNick(user.getNick(), event.getArgs()[0]);
            User newUser = user.snapshot();
            this.client.getEventManager().callEvent(new UserNickChangeEvent(this.client, oldUser, newUser));
        }
    }

    @CommandFilter("INVITE")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void invite(ClientReceiveCommandEvent event) {
        ActorProvider.IRCChannel invitedChannel = this.client.getActorProvider().getChannel(event.getArgs()[1]);
        if ((MessageTarget.getTypeByTarget(this.client, event.getArgs()[0]) == MessageTarget.PRIVATE) && this.client.getIntendedChannels().contains(invitedChannel.getName())) {
            this.client.sendRawLine("JOIN " + invitedChannel.getName());
        }
        this.client.getEventManager().callEvent(new ChannelInviteEvent(this.client, invitedChannel.snapshot(), event.getActor(), event.getArgs()[0]));
    }

    @CommandFilter("TOPIC")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void topic(ClientReceiveCommandEvent event) {
        ActorProvider.IRCChannel topicChannel = this.client.getActorProvider().getChannel(event.getArgs()[0]);
        topicChannel.setTopic(event.getArgs()[1]);
        topicChannel.setTopic(System.currentTimeMillis(), event.getActor());
        this.client.getEventManager().callEvent(new ChannelTopicEvent(this.client, topicChannel.snapshot(), true));
    }

    private enum MessageTarget {
        CHANNEL,
        CHANNEL_TARGETED,
        PRIVATE,
        UNKNOWN;

        static MessageTarget getTypeByTarget(@Nonnull InternalClient client, @Nonnull String target) {
            if (client.getNick().equalsIgnoreCase(target)) {
                return MessageTarget.PRIVATE;
            }
            if (client.getServerInfo().isTargetedChannel(target)) {
                return MessageTarget.CHANNEL_TARGETED;
            }
            if (client.getServerInfo().isValidChannel(target)) {
                return MessageTarget.CHANNEL;
            }
            return MessageTarget.UNKNOWN;
        }
    }
}