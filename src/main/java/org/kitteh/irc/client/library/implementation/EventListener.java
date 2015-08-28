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
package org.kitteh.irc.client.library.implementation;

import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.References;
import org.kitteh.irc.client.library.CapabilityManager;
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.ChannelModeStatusList;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.abstractbase.CapabilityNegotiationResponseEventBase;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesAcknowledgedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesDeletedSupportedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesListEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesNewSupportedEvent;
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
import org.kitteh.irc.client.library.event.channel.RequestedChannelLeaveViaKickEvent;
import org.kitteh.irc.client.library.event.channel.RequestedChannelLeaveViaPartEvent;
import org.kitteh.irc.client.library.event.client.ClientConnectedEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveMOTDEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.client.NickRejectedEvent;
import org.kitteh.irc.client.library.event.client.RequestedChannelJoinCompleteEvent;
import org.kitteh.irc.client.library.event.helper.CapabilityNegotiationResponseEvent;
import org.kitteh.irc.client.library.event.helper.ClientReceiveServerMessageEvent;
import org.kitteh.irc.client.library.event.user.PrivateCTCPQueryEvent;
import org.kitteh.irc.client.library.event.user.PrivateCTCPReplyEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;
import org.kitteh.irc.client.library.event.user.PrivateNoticeEvent;
import org.kitteh.irc.client.library.event.user.UserNickChangeEvent;
import org.kitteh.irc.client.library.event.user.UserQuitEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.util.CommandFilter;
import org.kitteh.irc.client.library.util.NumericFilter;
import org.kitteh.irc.client.library.util.StringUtil;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@net.engio.mbassy.listener.Listener(references = References.Strong)
class EventListener {
    private final InternalClient client;

    EventListener(InternalClient client) {
        this.client = client;
    }

    @NumericFilter(1)
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void welcome(ClientReceiveNumericEvent event) {
        if (!event.getArgs().isEmpty()) {
            this.client.setCurrentNick(event.getArgs().get(0));
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "Nickname unconfirmed.");
        }
    }

    @NumericFilter(4)
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void version(ClientReceiveNumericEvent event) {
        this.client.resetServerInfo();
        if (event.getArgs().size() > 1) {
            this.client.getServerInfo().setAddress(event.getArgs().get(1));
            if (event.getArgs().size() > 2) {
                this.client.getServerInfo().setVersion(event.getArgs().get(2));
            } else {
                throw new KittehServerMessageException(event.getOriginalMessage(), "Server version missing.");
            }
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "Server address and version missing.");
        }
        this.client.getEventManager().callEvent(new ClientConnectedEvent(this.client, event.getActor(), this.client.getServerInfo()));
        this.client.startSending();
    }

    @NumericFilter(5)
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void iSupport(ClientReceiveNumericEvent event) {
        for (int i = 1; i < event.getArgs().size(); i++) {
            ISupport.handle(event.getArgs().get(i), this.client);
        }
    }

    private final List<ServerMessage> whoMessages = new LinkedList<>();

    @NumericFilter(352) // WHO
    @NumericFilter(354) // WHOX
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void who(ClientReceiveNumericEvent event) {
        if (event.getArgs().size() < ((event.getNumeric() == 352) ? 8 : 9)) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "WHO response of incorrect length");
        }
        final ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs().get(1));
        if (channel != null) {
            final String ident = event.getArgs().get(2);
            final String host = event.getArgs().get(3);
            final String server = event.getArgs().get(4);
            final String nick = event.getArgs().get(5);
            final ActorProvider.IRCUser user = (ActorProvider.IRCUser) this.client.getActorProvider().getActor(nick + '!' + ident + '@' + host);
            user.setServer(server);
            final String status = event.getArgs().get(6);
            String realName;
            switch (event.getNumeric()) {
                case 352:
                    realName = event.getArgs().get(7);
                    break;
                case 354:
                default:
                    String account = event.getArgs().get(7);
                    user.setAccount("0".equals(account) ? null : account);
                    realName = event.getArgs().get(8);
                    break;
            }
            user.setRealName(realName);
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
            this.whoMessages.add(messageFromEvent(event));
        } // No else, server might send other WHO information about non-channels.
    }

    @NumericFilter(315) // WHO completed
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void whoComplete(ClientReceiveNumericEvent event) {
        if (event.getArgs().size() < 2) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "WHO response of incorrect length");
        }
        ActorProvider.IRCChannel whoChannel = this.client.getActorProvider().getChannel(event.getArgs().get(1));
        if (whoChannel != null) {
            whoChannel.setListReceived();
            this.whoMessages.add(messageFromEvent(event));
            this.client.getEventManager().callEvent(new ChannelUsersUpdatedEvent(this.client, this.whoMessages, whoChannel.snapshot()));
            this.whoMessages.clear();
        } // No else, server might send other WHO information about non-channels.
    }

    @NumericFilter(324)
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void channelMode(ClientReceiveNumericEvent event) {
        if (event.getArgs().size() < 3) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "Channel mode info message of incorrect length");
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs().get(1));
        if (channel != null) {
            ChannelModeStatusList statusList = ChannelModeStatusList.from(this.client, StringUtil.combineSplit(event.getArgs().toArray(new String[event.getArgs().size()]), 2));
            channel.updateChannelModes(statusList);
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "Channel mode info message sent for invalid channel name");
        }
    }

    @NumericFilter(332) // Topic
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void topic(ClientReceiveNumericEvent event) {
        if (event.getArgs().size() < 2) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "Topic message of incorrect length");
        }
        ActorProvider.IRCChannel topicChannel = this.client.getActorProvider().getChannel(event.getArgs().get(1));
        if (topicChannel != null) {
            topicChannel.setTopic(event.getArgs().get(2));
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "Topic message sent for invalid channel name");
        }
    }

    @NumericFilter(333) // Topic info
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void topicInfo(ClientReceiveNumericEvent event) {
        if (event.getArgs().size() < 4) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "Topic message of incorrect length");
        }
        ActorProvider.IRCChannel topicSetChannel = this.client.getActorProvider().getChannel(event.getArgs().get(1));
        if (topicSetChannel != null) {
            topicSetChannel.setTopic(Long.parseLong(event.getArgs().get(3)) * 1000, this.client.getActorProvider().getActor(event.getArgs().get(2)).snapshot());
            this.client.getEventManager().callEvent(new ChannelTopicEvent(this.client, listFromEvent(event), topicSetChannel.snapshot(), false));
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "Topic message sent for invalid channel name");
        }
    }

    private final List<ServerMessage> namesMessages = new LinkedList<>();

    @NumericFilter(353) // NAMES
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void names(ClientReceiveNumericEvent event) {
        if (event.getArgs().size() < 4) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "NAMES response of incorrect length");
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs().get(2));
        if (channel != null) {
            List<ChannelUserMode> channelUserModes = this.client.getServerInfo().getChannelUserModes();
            for (String combo : event.getArgs().get(3).split(" ")) {
                Set<ChannelUserMode> modes = new HashSet<>();
                for (int i = 0; i < combo.length(); i++) {
                    char c = combo.charAt(i);
                    Optional<ChannelUserMode> mode = channelUserModes.stream().filter(userMode -> userMode.getPrefix() == c).findFirst();
                    if (mode.isPresent()) {
                        modes.add(mode.get());
                    } else {
                        channel.trackNick(combo.substring(i), modes);
                        break;
                    }
                }
            }
            this.namesMessages.add(messageFromEvent(event));
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "NAMES response sent for invalid channel name");
        }
    }

    @NumericFilter(366) // End of NAMES
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void namesComplete(ClientReceiveNumericEvent event) {
        if (event.getArgs().size() < 2) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "NAMES response of incorrect length");
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs().get(1));
        if (channel != null) {
            this.namesMessages.add(messageFromEvent(event));
            this.client.getEventManager().callEvent(new ChannelNamesUpdatedEvent(this.client, this.namesMessages, channel.snapshot()));
            this.namesMessages.clear();
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "NAMES response sent for invalid channel name");
        }
    }

    private final List<String> motd = new LinkedList<>();
    private final List<ServerMessage> motdMessages = new LinkedList<>();

    @NumericFilter(375)
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void motdStart(ClientReceiveNumericEvent event) {
        this.motd.clear();
        this.motdMessages.clear();
    }

    @NumericFilter(372)
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void motdContent(ClientReceiveNumericEvent event) {
        if (event.getArgs().size() < 2) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "MOTD message of incorrect length");
        }
        this.motd.add(event.getArgs().get(1));
        this.motdMessages.add(messageFromEvent(event));
    }

    @NumericFilter(376)
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void motdEnd(ClientReceiveNumericEvent event) {
        this.motdMessages.add(messageFromEvent(event));
        this.client.getServerInfo().setMOTD(new ArrayList<>(this.motd));
        this.client.getEventManager().callEvent(new ClientReceiveMOTDEvent(this.client, this.motdMessages));
    }

    @NumericFilter(431) // No nick given
    @NumericFilter(432) // Erroneous nickname
    @NumericFilter(433) // Nick in use
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void nickInUse(ClientReceiveNumericEvent event) {
        NickRejectedEvent nickRejectedEvent = new NickRejectedEvent(this.client, listFromEvent(event), this.client.getRequestedNick(), this.client.getRequestedNick() + '`');
        this.client.getEventManager().callEvent(nickRejectedEvent);
        this.client.sendNickChange(nickRejectedEvent.getNewNick());
    }

    @NumericFilter(710) // Knock
    @Handler(filters = @Filter(NumericFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void knock(ClientReceiveNumericEvent event) {
        if (event.getArgs().size() < 3) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "KNOCK message of incorrect length");
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs().get(1));
        if (channel != null) {
            ActorProvider.IRCUser user = (ActorProvider.IRCUser) this.client.getActorProvider().getActor(event.getArgs().get(2));
            this.client.getEventManager().callEvent(new ChannelKnockEvent(this.client, listFromEvent(event), channel.snapshot(), user.snapshot()));
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "KNOCK message sent for invalid channel name");
        }
    }

    @CommandFilter("NOTICE")
    @CommandFilter("PRIVMSG")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void ctcp(ClientReceiveCommandEvent event) {
        if (event.getArgs().size() < 2) {
            return; // Nothing to do here, handle that issue in the individual methods
        }
        if (CTCPUtil.isCTCP(event.getArgs().get(1))) {
            final String ctcpMessage = CTCPUtil.fromCTCP(event.getArgs().get(1));
            final MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getArgs().get(0));
            User user = (User) event.getActor();
            switch (event.getCommand()) {
                case "NOTICE":
                    if (messageTargetInfo instanceof MessageTargetInfo.Private) {
                        this.client.getEventManager().callEvent(new PrivateCTCPReplyEvent(this.client, listFromEvent(event), user, ctcpMessage));
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
                        PrivateCTCPQueryEvent ctcpEvent = new PrivateCTCPQueryEvent(this.client, listFromEvent(event), user, ctcpMessage, Optional.ofNullable(reply));
                        this.client.getEventManager().callEvent(ctcpEvent);
                        if (ctcpEvent.getReply().isPresent()) {
                            this.client.sendRawLine("NOTICE " + user.getNick() + " :" + CTCPUtil.toCTCP(ctcpEvent.getReply().get()));
                        }
                    } else if (messageTargetInfo instanceof MessageTargetInfo.Channel) {
                        MessageTargetInfo.Channel channelInfo = (MessageTargetInfo.Channel) messageTargetInfo;
                        this.client.getEventManager().callEvent(new ChannelCTCPEvent(this.client, listFromEvent(event), user, channelInfo.getChannel().snapshot(), ctcpMessage));
                    } else if (messageTargetInfo instanceof MessageTargetInfo.TargetedChannel) {
                        MessageTargetInfo.TargetedChannel channelInfo = (MessageTargetInfo.TargetedChannel) messageTargetInfo;
                        this.client.getEventManager().callEvent(new ChannelTargetedCTCPEvent(this.client, listFromEvent(event), user, channelInfo.getChannel().snapshot(), channelInfo.getPrefix(), ctcpMessage));
                    }
                    break;
            }
        }
    }

    private final List<CapabilityState> capList = new LinkedList<>();
    private final List<ServerMessage> capListMessages = new LinkedList<>();
    private final List<CapabilityState> capLs = new LinkedList<>();
    private final List<ServerMessage> capLsMessages = new LinkedList<>();
    private static final int CAPABILITY_LIST_INDEX_DEFAULT = 2;

    @CommandFilter("CAP")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void cap(ClientReceiveCommandEvent event) {
        if (event.getArgs().size() < 3) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "CAP message of incorrect length");
        }
        CapabilityNegotiationResponseEventBase responseEvent = null;
        int capabilityListIndex;
        if (event.getArgs().get(CAPABILITY_LIST_INDEX_DEFAULT).equals("*")) {
            if (event.getArgs().size() < 4) {
                throw new KittehServerMessageException(event.getOriginalMessage(), "CAP message of incorrect length");
            }
            capabilityListIndex = CAPABILITY_LIST_INDEX_DEFAULT + 1;
        } else {
            capabilityListIndex = CAPABILITY_LIST_INDEX_DEFAULT;
        }
        List<CapabilityState> capabilityStateList = Arrays.stream(event.getArgs().get(capabilityListIndex).split(" ")).map(capability -> new IRCCapabilityManager.IRCCapabilityState(this.client, capability)).collect(Collectors.toCollection(ArrayList::new));
        switch (event.getArgs().get(1).toLowerCase()) {
            case "ack":
                this.client.getCapabilityManager().updateCapabilities(capabilityStateList);
                responseEvent = new CapabilitiesAcknowledgedEvent(this.client, listFromEvent(event), this.client.getCapabilityManager().isNegotiating(), capabilityStateList);
                this.client.getEventManager().callEvent(responseEvent);
                break;
            case "list":
                this.capListMessages.add(messageFromEvent(event));
                if (capabilityListIndex != CAPABILITY_LIST_INDEX_DEFAULT) {
                    this.capList.addAll(capabilityStateList);
                } else {
                    List<CapabilityState> states;
                    if (this.capList.isEmpty()) {
                        states = capabilityStateList;
                    } else {
                        states = this.capList;
                        states.addAll(capabilityStateList);
                    }
                    this.client.getCapabilityManager().setCapabilities(states);
                    this.client.getEventManager().callEvent(new CapabilitiesListEvent(this.client, this.capListMessages, states));
                    states.clear();
                }
                break;
            case "ls":
                this.capLsMessages.add(messageFromEvent(event));
                if (capabilityListIndex != CAPABILITY_LIST_INDEX_DEFAULT) {
                    this.capList.addAll(capabilityStateList);
                } else {
                    List<CapabilityState> states;
                    if (this.capLs.isEmpty()) {
                        states = capabilityStateList;
                    } else {
                        states = this.capLs;
                        states.addAll(capabilityStateList);
                    }
                    this.client.getCapabilityManager().setSupportedCapabilities(states);
                    responseEvent = new CapabilitiesSupportedListEvent(this.client, this.capLsMessages, this.client.getCapabilityManager().isNegotiating(), states);
                    this.capReq(responseEvent);
                    this.client.getEventManager().callEvent(responseEvent);
                    states.clear();
                }
                break;
            case "nak":
                this.client.getCapabilityManager().updateCapabilities(capabilityStateList);
                responseEvent = new CapabilitiesRejectedEvent(this.client, listFromEvent(event), this.client.getCapabilityManager().isNegotiating(), capabilityStateList);
                this.client.getEventManager().callEvent(responseEvent);
                break;
            case "new":
                List<CapabilityState> statesAdded = new LinkedList<>(this.client.getCapabilityManager().getSupportedCapabilities());
                statesAdded.addAll(capabilityStateList);
                this.client.getCapabilityManager().setSupportedCapabilities(statesAdded);
                responseEvent = new CapabilitiesNewSupportedEvent(this.client, listFromEvent(event), this.client.getCapabilityManager().isNegotiating(), capabilityStateList);
                this.capReq(responseEvent);
                this.client.getEventManager().callEvent(responseEvent);
                break;
            case "del":
                List<CapabilityState> statesRemaining = new LinkedList<>(this.client.getCapabilityManager().getSupportedCapabilities());
                statesRemaining.removeAll(capabilityStateList);
                this.client.getCapabilityManager().setSupportedCapabilities(statesRemaining);
                responseEvent = new CapabilitiesDeletedSupportedEvent(this.client, listFromEvent(event), this.client.getCapabilityManager().isNegotiating(), capabilityStateList);
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

    private void capReq(@Nullable CapabilityNegotiationResponseEvent responseEvent) {
        Set<String> capabilities = this.client.getCapabilityManager().getSupportedCapabilities().stream().map(CapabilityState::getName).collect(Collectors.toCollection(HashSet::new));
        capabilities.retainAll(CapabilityManager.Defaults.getAll());
        capabilities.removeAll(this.client.getCapabilityManager().getCapabilities().stream().map(CapabilityState::getName).collect(Collectors.toList()));
        if (!capabilities.isEmpty()) {
            if (responseEvent != null) {
                responseEvent.setEndingNegotiation(false);
            }
            CapabilityRequestCommand capabilityRequestCommand = new CapabilityRequestCommand(this.client);
            capabilities.forEach(capabilityRequestCommand::enable);
            capabilityRequestCommand.execute();
        }
    }

    @CommandFilter("ACCOUNT")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void account(ClientReceiveCommandEvent event) {
        if (event.getArgs().size() < 1) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "ACCOUNT message of incorrect length");
        }
        String account = event.getArgs().get(0);
        this.client.getActorProvider().setUserAccount(((User) event.getActor()).getNick(), "*".equals(account) ? null : account);
    }

    @CommandFilter("AWAY")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void away(ClientReceiveCommandEvent event) {
        this.client.getActorProvider().setUserAway(((User) event.getActor()).getNick(), event.getArgs().size() > 0);
    }

    @CommandFilter("NOTICE")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void notice(ClientReceiveCommandEvent event) {
        if (event.getArgs().size() < 2) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "NOTICE message of incorrect length");
        }
        if (!(event.getActor() instanceof User)) {
            return; // TODO handle this
        }
        User user = (User) event.getActor();
        MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getArgs().get(0));
        if (messageTargetInfo instanceof MessageTargetInfo.Private) {
            this.client.getEventManager().callEvent(new PrivateNoticeEvent(this.client, listFromEvent(event), user, event.getArgs().get(1)));
        } else if (messageTargetInfo instanceof MessageTargetInfo.Channel) {
            MessageTargetInfo.Channel channelInfo = (MessageTargetInfo.Channel) messageTargetInfo;
            this.client.getEventManager().callEvent(new ChannelNoticeEvent(this.client, listFromEvent(event), user, channelInfo.getChannel().snapshot(), event.getArgs().get(1)));
        } else if (messageTargetInfo instanceof MessageTargetInfo.TargetedChannel) {
            MessageTargetInfo.TargetedChannel channelInfo = (MessageTargetInfo.TargetedChannel) messageTargetInfo;
            this.client.getEventManager().callEvent(new ChannelTargetedNoticeEvent(this.client, listFromEvent(event), user, channelInfo.getChannel().snapshot(), channelInfo.getPrefix(), event.getArgs().get(1)));
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "NOTICE message to improper target");
        }
    }

    @CommandFilter("PRIVMSG")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void privmsg(ClientReceiveCommandEvent event) {
        if (event.getArgs().size() < 2) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "PRIVMSG message of incorrect length");
        }
        if (CTCPUtil.isCTCP(event.getArgs().get(1))) {
            return;
        }
        if (!(event.getActor() instanceof User)) {
            return; // TODO handle this
        }
        User user = (User) event.getActor();
        MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getArgs().get(0));
        if (messageTargetInfo instanceof MessageTargetInfo.Private) {
            this.client.getEventManager().callEvent(new PrivateMessageEvent(this.client, listFromEvent(event), user, event.getArgs().get(1)));
        } else if (messageTargetInfo instanceof MessageTargetInfo.Channel) {
            MessageTargetInfo.Channel channelInfo = (MessageTargetInfo.Channel) messageTargetInfo;
            this.client.getEventManager().callEvent(new ChannelMessageEvent(this.client, listFromEvent(event), user, channelInfo.getChannel().snapshot(), event.getArgs().get(1)));
        } else if (messageTargetInfo instanceof MessageTargetInfo.TargetedChannel) {
            MessageTargetInfo.TargetedChannel channelInfo = (MessageTargetInfo.TargetedChannel) messageTargetInfo;
            this.client.getEventManager().callEvent(new ChannelTargetedMessageEvent(this.client, listFromEvent(event), user, channelInfo.getChannel().snapshot(), channelInfo.getPrefix(), event.getArgs().get(1)));
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "PRIVMSG message to improper target");
        }
    }

    @CommandFilter("MODE")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void mode(ClientReceiveCommandEvent event) {
        if (event.getArgs().size() < 2) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "MODE message of incorrect length");
        }
        MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getArgs().get(0));
        if (messageTargetInfo instanceof MessageTargetInfo.Private) {
            // TODO listen to mode on self
        } else if (messageTargetInfo instanceof MessageTargetInfo.Channel) {
            ActorProvider.IRCChannel channel = ((MessageTargetInfo.Channel) messageTargetInfo).getChannel();
            ChannelModeStatusList statusList;
            try {
                statusList = ChannelModeStatusList.from(this.client, StringUtil.combineSplit(event.getArgs().toArray(new String[event.getArgs().size()]), 1));
            } catch (IllegalArgumentException e) {
                throw new KittehServerMessageException(event.getOriginalMessage(), e.getMessage());
            }
            this.client.getEventManager().callEvent(new ChannelModeEvent(this.client, listFromEvent(event), event.getActor(), channel.snapshot(), statusList));
            statusList.getStatuses().stream().filter(status -> (status.getMode() instanceof ChannelUserMode) && (status.getParameter().isPresent())).forEach(status -> {
                if (status.isSetting()) {
                    channel.trackUserModeAdd(status.getParameter().get(), (ChannelUserMode) status.getMode());
                } else {
                    channel.trackUserModeRemove(status.getParameter().get(), (ChannelUserMode) status.getMode());
                }
            });
            channel.updateChannelModes(statusList);
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "MODE message sent for invalid target");
        }
    }

    @CommandFilter("JOIN")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void join(ClientReceiveCommandEvent event) {
        if (event.getArgs().size() < 1) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "JOIN message of incorrect length");
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs().get(0));
        if (channel != null) {
            if (event.getActor() instanceof User) {
                ActorProvider.IRCUser user = (ActorProvider.IRCUser) this.client.getActorProvider().getActor(event.getActor().getName());
                channel.trackUser(user, new HashSet<>());
                ChannelJoinEvent joinEvent = null;
                if (user.getNick().equals(this.client.getNick())) {
                    this.client.getActorProvider().trackChannel(channel);
                    this.client.sendRawLine("MODE " + channel.getName());
                    this.client.sendRawLine("WHO " + channel.getName() + (this.client.getServerInfo().hasWhoXSupport() ? " %cuhsnfar" : ""));
                    if (this.client.getIntendedChannels().contains(channel.getName())) {
                        joinEvent = new RequestedChannelJoinCompleteEvent(this.client, listFromEvent(event), channel.snapshot(), user.snapshot());
                    }
                }
                if (event.getArgs().size() > 2) {
                    if (!"*".equals(event.getArgs().get(1))) {
                        user.setAccount(event.getArgs().get(1));
                    }
                    user.setRealName(event.getArgs().get(2));
                }
                if (joinEvent == null) {
                    joinEvent = new ChannelJoinEvent(this.client, listFromEvent(event), channel.snapshot(), user.snapshot());
                }
                this.client.getEventManager().callEvent(joinEvent);
            } else {
                throw new KittehServerMessageException(event.getOriginalMessage(), "JOIN message sent for non-user");
            }
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "JOIN message sent for invalid channel name");
        }
    }

    @CommandFilter("PART")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void part(ClientReceiveCommandEvent event) {
        if (event.getArgs().size() < 1) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "PART message of incorrect length");
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs().get(0));
        if (channel != null) {
            if (event.getActor() instanceof User) {
                User user = (User) event.getActor();
                boolean isSelf = user.getNick().equals(this.client.getNick());
                String partReason = (event.getArgs().size() > 1) ? event.getArgs().get(1) : "";
                ChannelPartEvent partEvent;
                if (isSelf && this.client.getIntendedChannels().contains(channel.getName())) {
                    partEvent = new RequestedChannelLeaveViaPartEvent(this.client, listFromEvent(event), channel.snapshot(), user, partReason);
                } else {
                    partEvent = new ChannelPartEvent(this.client, listFromEvent(event), channel.snapshot(), user, partReason);
                }
                this.client.getEventManager().callEvent(partEvent);
                channel.trackUserPart(user.getNick());
                if (isSelf) {
                    this.client.getActorProvider().unTrackChannel(channel);
                }
            } else {
                throw new KittehServerMessageException(event.getOriginalMessage(), "PART message sent for non-user");
            }
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "PART message sent for invalid channel name");
        }
    }

    @CommandFilter("QUIT")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void quit(ClientReceiveCommandEvent event) {
        if (event.getActor() instanceof User) {
            this.client.getEventManager().callEvent(new UserQuitEvent(this.client, listFromEvent(event), (User) event.getActor(), (event.getArgs().size() > 0) ? event.getArgs().get(0) : ""));
            this.client.getActorProvider().trackUserQuit(((User) event.getActor()).getNick());
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "QUIT message sent for non-user");
        }
    }

    @CommandFilter("KICK")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void kick(ClientReceiveCommandEvent event) {
        if (event.getArgs().size() < 2) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "KICK message of incorrect length");
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs().get(0));
        if (channel != null) {
            ActorProvider.IRCUser kickedUser = this.client.getActorProvider().getUser(event.getArgs().get(1));
            if (kickedUser != null) {
                boolean isSelf = event.getArgs().get(1).equals(this.client.getNick());
                ChannelKickEvent kickEvent;
                String kickReason = (event.getArgs().size() > 2) ? event.getArgs().get(2) : "";
                if (isSelf && this.client.getIntendedChannels().contains(channel.getName())) {
                    kickEvent = new RequestedChannelLeaveViaKickEvent(this.client, listFromEvent(event), channel.snapshot(), (User) event.getActor(), kickedUser.snapshot(), kickReason);
                } else {
                    kickEvent = new ChannelKickEvent(this.client, listFromEvent(event), channel.snapshot(), (User) event.getActor(), kickedUser.snapshot(), kickReason);
                }
                this.client.getEventManager().callEvent(kickEvent);
                channel.trackUserPart(event.getArgs().get(1));
                if (isSelf) {
                    this.client.getActorProvider().unTrackChannel(channel);
                }
            } else {
                throw new KittehServerMessageException(event.getOriginalMessage(), "KICK message sent for non-user");
            }
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "KICK message sent for invalid channel name");
        }
    }

    @CommandFilter("NICK")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void nick(ClientReceiveCommandEvent event) {
        if (event.getArgs().size() < 1) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "NICK message of incorrect length");
        }
        if (event.getActor() instanceof User) {
            boolean isSelf = ((User) event.getActor()).getNick().equals(this.client.getNick());
            ActorProvider.IRCUser user = this.client.getActorProvider().getUser(((User) event.getActor()).getNick());
            if (user == null) {
                if (isSelf) {
                    this.client.setCurrentNick(event.getArgs().get(0));
                    return; // Don't fail if NICK changes while not in a channel!
                }
                throw new KittehServerMessageException(event.getOriginalMessage(), "NICK message sent for user not in tracked channels");
            }
            User oldUser = user.snapshot();
            this.client.getActorProvider().trackUserNickChange(user.getNick(), event.getArgs().get(0));
            User newUser = user.snapshot();
            this.client.getEventManager().callEvent(new UserNickChangeEvent(this.client, listFromEvent(event), oldUser, newUser));
            if (isSelf) {
                this.client.setCurrentNick(event.getArgs().get(0));
            }
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "NICK message sent for non-user");
        }
    }

    @CommandFilter("INVITE")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void invite(ClientReceiveCommandEvent event) {
        if (event.getArgs().size() < 2) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "INVITE message of incorrect length");
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs().get(1));
        if (channel != null) {
            if (this.client.getNick().equalsIgnoreCase(event.getArgs().get(0)) && this.client.getIntendedChannels().contains(channel.getName())) {
                this.client.sendRawLine("JOIN " + channel.getName());
            }
            this.client.getEventManager().callEvent(new ChannelInviteEvent(this.client, listFromEvent(event), channel.snapshot(), event.getActor(), event.getArgs().get(0)));
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "INVITE message sent for invalid channel name");
        }
    }

    @CommandFilter("TOPIC")
    @Handler(filters = @Filter(CommandFilter.Filter.class), priority = Integer.MAX_VALUE - 1)
    public void topic(ClientReceiveCommandEvent event) {
        if (event.getArgs().size() < 2) {
            throw new KittehServerMessageException(event.getOriginalMessage(), "TOPIC message of incorrect length");
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getArgs().get(0));
        if (channel != null) {
            channel.setTopic(event.getArgs().get(1));
            channel.setTopic(System.currentTimeMillis(), event.getActor());
            this.client.getEventManager().callEvent(new ChannelTopicEvent(this.client, listFromEvent(event), channel.snapshot(), true));
        } else {
            throw new KittehServerMessageException(event.getOriginalMessage(), "TOPIC message sent for invalid channel name");
        }
    }

    private static class MessageTargetInfo {
        private static class Channel extends MessageTargetInfo {
            private final ActorProvider.IRCChannel channel;

            private Channel(ActorProvider.IRCChannel channel) {
                this.channel = channel;
            }

            @Nonnull
            ActorProvider.IRCChannel getChannel() {
                return this.channel;
            }

            @Nonnull
            @Override
            public String toString() {
                return new ToStringer(this).toString();
            }
        }

        private static class TargetedChannel extends MessageTargetInfo {
            private final ActorProvider.IRCChannel channel;
            private final ChannelUserMode prefix;

            private TargetedChannel(ActorProvider.IRCChannel channel, ChannelUserMode prefix) {
                this.channel = channel;
                this.prefix = prefix;
            }

            @Nonnull
            ActorProvider.IRCChannel getChannel() {
                return this.channel;
            }

            @Nonnull
            ChannelUserMode getPrefix() {
                return this.prefix;
            }

            @Nonnull
            @Override
            public String toString() {
                return new ToStringer(this).toString();
            }
        }

        private static class Private extends MessageTargetInfo {
            static final Private INSTANCE = new Private();

            private Private() {
            }

            @Nonnull
            @Override
            public String toString() {
                return new ToStringer(this).toString();
            }
        }

        static final MessageTargetInfo UNKNOWN = new MessageTargetInfo();
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).toString();
    }

    @Nonnull
    private MessageTargetInfo getTypeByTarget(@Nonnull String target) {
        if (this.client.getNick().equalsIgnoreCase(target)) {
            return MessageTargetInfo.Private.INSTANCE;
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(target);
        ChannelUserMode prefix = this.client.getServerInfo().getTargetedChannelInfo(target);
        if (channel != null) {
            if (prefix != null) {
                return new MessageTargetInfo.TargetedChannel(channel, prefix);
            } else {
                return new MessageTargetInfo.Channel(channel);
            }
        }
        return MessageTargetInfo.UNKNOWN;
    }

    @Nonnull
    private static List<ServerMessage> listFromEvent(@Nonnull ClientReceiveServerMessageEvent event) {
        return Collections.singletonList(messageFromEvent(event));
    }

    @Nonnull
    private static ServerMessage messageFromEvent(@Nonnull ClientReceiveServerMessageEvent event) {
        return new IRCServerMessage(event.getOriginalMessage(), event.getMessageTags());
    }
}