/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.References;
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.Server;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.WhoisData;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.ModeInfo;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.event.abstractbase.CapabilityNegotiationResponseEventBase;
import org.kitteh.irc.client.library.event.abstractbase.CapabilityNegotiationResponseEventWithRequestBase;
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
import org.kitteh.irc.client.library.event.channel.ChannelModeInfoListEvent;
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
import org.kitteh.irc.client.library.event.client.ClientAwayStatusChangeEvent;
import org.kitteh.irc.client.library.event.client.ClientConnectedEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveMOTDEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.client.NickRejectedEvent;
import org.kitteh.irc.client.library.event.client.RequestedChannelJoinCompleteEvent;
import org.kitteh.irc.client.library.event.helper.ClientEvent;
import org.kitteh.irc.client.library.event.helper.ClientReceiveServerMessageEvent;
import org.kitteh.irc.client.library.event.helper.MonitoredNickStatusEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickListEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickListFullEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickOfflineEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickOnlineEvent;
import org.kitteh.irc.client.library.event.user.PrivateCTCPQueryEvent;
import org.kitteh.irc.client.library.event.user.PrivateCTCPReplyEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;
import org.kitteh.irc.client.library.event.user.PrivateNoticeEvent;
import org.kitteh.irc.client.library.event.user.ServerNoticeEvent;
import org.kitteh.irc.client.library.event.user.UserHostnameChangeEvent;
import org.kitteh.irc.client.library.event.user.UserModeEvent;
import org.kitteh.irc.client.library.event.user.UserNickChangeEvent;
import org.kitteh.irc.client.library.event.user.UserQuitEvent;
import org.kitteh.irc.client.library.event.user.UserUserStringChangeEvent;
import org.kitteh.irc.client.library.event.user.WallopsEvent;
import org.kitteh.irc.client.library.event.user.WhoisEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;
import org.kitteh.irc.client.library.util.StringUtil;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
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
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void welcome(ClientReceiveNumericEvent event) {
        if (!event.getParameters().isEmpty()) {
            this.client.setCurrentNick(event.getParameters().get(0));
        } else {
            this.trackException(event, "Nickname unconfirmed.");
        }
    }

    @NumericFilter(4)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void version(ClientReceiveNumericEvent event) {
        this.client.resetServerInfo();
        if (event.getParameters().size() > 1) {
            this.client.getServerInfo().setAddress(event.getParameters().get(1));
            if (event.getParameters().size() > 2) {
                this.client.getServerInfo().setVersion(event.getParameters().get(2));
                if (event.getParameters().size() > 3) {
                    List<UserMode> modes = new ArrayList<>(event.getParameters().get(3).length());
                    for (char mode : event.getParameters().get(3).toCharArray()) {
                        modes.add(new ModeData.IRCUserMode(this.client, mode));
                    }
                    this.client.getServerInfo().setUserModes(modes);
                } else {
                    this.trackException(event, "Server user modes missing.");
                }
            } else {
                this.trackException(event, "Server version missing.");
            }
        } else {
            this.trackException(event, "Server address and version missing.");
        }
        this.client.sendRawLineImmediately("WHOIS " + this.client.getNick());
        this.fire(new ClientConnectedEvent(this.client, event.getActor(), this.client.getServerInfo()));
        this.client.startSending();
    }

    @NumericFilter(5)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void iSupport(ClientReceiveNumericEvent event) {
        for (int i = 1; i < event.getParameters().size(); i++) {
            this.client.getServerInfo().addISupportParameter(this.client.getISupportManager().getParameter(event.getParameters().get(i)));
        }
    }

    @NumericFilter(221) // UMODEIS
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void umode(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "UMODE response of incorrect length");
            return;
        }

        if (!this.client.getServerInfo().getCaseMapping().areEqualIgnoringCase(event.getParameters().get(0), this.client.getNick())) {
            this.trackException(event, "UMODE response for another user");
            return;
        }
        ModeStatusList<UserMode> modes;
        try {
            modes = ModeStatusList.fromUser(this.client, StringUtil.combineSplit(event.getParameters().toArray(new String[event.getParameters().size()]), 1));
        } catch (IllegalArgumentException e) {
            this.trackException(event, e.getMessage());
            return;
        }
        this.client.setUserModes(modes);
    }

    @NumericFilter(305) // UNAWAY
    @NumericFilter(306) // NOWAWAY
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void away(ClientReceiveNumericEvent event) {
        this.fire(new ClientAwayStatusChangeEvent(this.client, event.getOriginalMessages(), event.getNumeric() == 306));
    }

    private WhoisBuilder whoisBuilder;

    private WhoisBuilder getWhoisBuilder(String nick) {
        if ((this.whoisBuilder == null) || !this.client.getServerInfo().getCaseMapping().areEqualIgnoringCase(this.whoisBuilder.getNick(), nick)) {
            this.whoisBuilder = new WhoisBuilder(this.client, nick);
        }
        return this.whoisBuilder;
    }

    @NumericFilter(301) // WHOISAWAY
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisAway(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "WHOIS AWAY response of incorrect length");
            return;
        }
        this.getWhoisBuilder(event.getParameters().get(1)).setAway(event.getParameters().get((event.getParameters().size() == 3) ? 2 : 3));
    }

    @NumericFilter(311) // WHOISUSER
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisUser(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "WHOIS USER response of incorrect length");
            return;
        }
        WhoisBuilder whoisBuilder = this.getWhoisBuilder(event.getParameters().get(1));
        switch (event.getParameters().size()) {
            case 6:
                whoisBuilder.setRealName(event.getParameters().get(5));
            case 4:
                whoisBuilder.setHost(event.getParameters().get(3));
            case 3:
                whoisBuilder.setUserString(event.getParameters().get(2));
        }
    }

    @NumericFilter(312) // WHOISSERVER
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisServer(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "WHOIS SERVER response of incorrect length");
            return;
        }
        WhoisBuilder whoisBuilder = this.getWhoisBuilder(event.getParameters().get(1));
        whoisBuilder.setServer(event.getParameters().get(2));
        if (event.getParameters().size() > 3) {
            whoisBuilder.setServerDescription(event.getParameters().get(3));
        }
    }

    @NumericFilter(313) // WHOISOPERATOR
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisOperator(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "WHOIS OPERATOR response of incorrect length");
            return;
        }
        this.getWhoisBuilder(event.getParameters().get(1)).setOperatorInformation(event.getParameters().get(2));
    }

    @NumericFilter(317) // WHOISIDLE
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisIdle(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 4) {
            this.trackException(event, "WHOIS IDLE response of incorrect length");
            return;
        }
        WhoisBuilder whoisBuilder = this.getWhoisBuilder(event.getParameters().get(1));
        long idleTime;
        try {
            idleTime = Long.parseLong(event.getParameters().get(2));
        } catch (NumberFormatException e) {
            this.trackException(event, "WHOIS IDLE idle time not a number");
            return;
        }
        whoisBuilder.setIdleTime(idleTime);
        if (event.getParameters().size() > 4) {
            long signOnTime;
            try {
                signOnTime = Long.parseLong(event.getParameters().get(3));
            } catch (NumberFormatException e) {
                this.trackException(event, "WHOIS IDLE sign on time not a number");
                return;
            }
            whoisBuilder.setSignOnTime(signOnTime);
        }
    }

    @NumericFilter(330) // WHOISACCOUNT
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisAccount(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "WHOIS ACCOUNT response of incorrect length");
            return;
        }
        this.getWhoisBuilder(event.getParameters().get(1)).setAccount(event.getParameters().get(2));
    }

    @NumericFilter(319) // WHOISCHANNELS
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisChannels(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "WHOIS CHANNELS response of incorrect length");
            return;
        }
        this.getWhoisBuilder(event.getParameters().get(1)).addChannels(event.getParameters().get(2));
    }

    @NumericFilter(671) // WHOISSECURE
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisSecure(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "WHOIS SECURE response of incorrect length");
            return;
        }
        this.getWhoisBuilder(event.getParameters().get(1)).setSecure();
    }

    @NumericFilter(318) // ENDOFWHOIS
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisEnd(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "WHOIS END response of incorrect length");
            return;
        }
        WhoisData whois = this.getWhoisBuilder(event.getParameters().get(1)).build();
        if (this.client.getServerInfo().getCaseMapping().areEqualIgnoringCase(whois.getNick(), this.client.getNick()) && (this.client.getActorProvider().getUser(whois.getNick()) == null)) {
            this.client.getActorProvider().trackUser((ActorProvider.IRCUser) this.client.getActorProvider().getActor(whois.getName()));
        }
        this.fire(new WhoisEvent(this.client, whois));
        this.whoisBuilder = null;
    }

    private final List<ServerMessage> whoMessages = new ArrayList<>();

    @NumericFilter(352) // WHO
    @NumericFilter(354) // WHOX
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void who(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < ((event.getNumeric() == 352) ? 8 : 9)) {
            this.trackException(event, "WHO response of incorrect length");
            return;
        }
        final ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getParameters().get(1));
        if (channel != null) {
            final String ident = event.getParameters().get(2);
            final String host = event.getParameters().get(3);
            final String server = event.getParameters().get(4);
            final String nick = event.getParameters().get(5);
            final ActorProvider.IRCUser user = (ActorProvider.IRCUser) this.client.getActorProvider().getActor(nick + '!' + ident + '@' + host);
            user.setServer(server);
            final String status = event.getParameters().get(6);
            String realName;
            switch (event.getNumeric()) {
                case 352:
                    realName = event.getParameters().get(7);
                    break;
                case 354:
                default:
                    String account = event.getParameters().get(7);
                    user.setAccount("0".equals(account) ? null : account);
                    realName = event.getParameters().get(8);
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
                    if (mode.getNickPrefix() == prefix) {
                        modes.add(mode);
                        break;
                    }
                }
            }
            channel.trackUser(user, modes);
            this.whoMessages.add(event.getServerMessage());
        } // No else, server might send other WHO information about non-channels.
    }

    @NumericFilter(315) // WHO completed
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoComplete(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "WHO response of incorrect length");
            return;
        }
        ActorProvider.IRCChannel whoChannel = this.client.getActorProvider().getChannel(event.getParameters().get(1));
        if (whoChannel != null) {
            whoChannel.setListReceived();
            this.whoMessages.add(event.getServerMessage());
            this.fire(new ChannelUsersUpdatedEvent(this.client, this.whoMessages, whoChannel.snapshot()));
            this.whoMessages.clear();
        } // No else, server might send other WHO information about non-channels.
    }

    @NumericFilter(324)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void channelMode(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "Channel mode info message of incorrect length");
            return;
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getParameters().get(1));
        if (channel != null) {
            ModeStatusList<ChannelMode> statusList;
            try {
                statusList = ModeStatusList.fromChannel(this.client, StringUtil.combineSplit(event.getParameters().toArray(new String[event.getParameters().size()]), 2));
            } catch (IllegalArgumentException e) {
                this.trackException(event, e.getMessage());
                return;
            }
            channel.updateChannelModes(statusList);
        } else {
            this.trackException(event, "Channel mode info message sent for invalid channel name");
        }
    }

    @NumericFilter(332) // Topic
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void topic(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "Topic message of incorrect length");
            return;
        }
        ActorProvider.IRCChannel topicChannel = this.client.getActorProvider().getChannel(event.getParameters().get(1));
        if (topicChannel != null) {
            topicChannel.setTopic(event.getParameters().get(2));
        } else {
            this.trackException(event, "Topic message sent for invalid channel name");
        }
    }

    @NumericFilter(333) // Topic info
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void topicInfo(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 4) {
            this.trackException(event, "Topic message of incorrect length");
            return;
        }
        ActorProvider.IRCChannel topicSetChannel = this.client.getActorProvider().getChannel(event.getParameters().get(1));
        if (topicSetChannel != null) {
            topicSetChannel.setTopic(Long.parseLong(event.getParameters().get(3)) * 1000, this.client.getActorProvider().getActor(event.getParameters().get(2)).snapshot());
            this.fire(new ChannelTopicEvent(this.client, event.getOriginalMessages(), topicSetChannel.snapshot(), false));
        } else {
            this.trackException(event, "Topic message sent for invalid channel name");
        }
    }

    private final List<ServerMessage> namesMessages = new ArrayList<>();

    @NumericFilter(353) // NAMES
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void names(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 4) {
            this.trackException(event, "NAMES response of incorrect length");
            return;
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getParameters().get(2));
        if (channel != null) {
            List<ChannelUserMode> channelUserModes = this.client.getServerInfo().getChannelUserModes();
            for (String combo : event.getParameters().get(3).split(" ")) {
                Set<ChannelUserMode> modes = new HashSet<>();
                for (int i = 0; i < combo.length(); i++) {
                    char c = combo.charAt(i);
                    Optional<ChannelUserMode> mode = channelUserModes.stream().filter(userMode -> userMode.getNickPrefix() == c).findFirst();
                    if (mode.isPresent()) {
                        modes.add(mode.get());
                    } else {
                        channel.trackNick(combo.substring(i), modes);
                        break;
                    }
                }
            }
            this.namesMessages.add(event.getServerMessage());
        } else {
            this.trackException(event, "NAMES response sent for invalid channel name");
        }
    }

    @NumericFilter(366) // End of NAMES
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void namesComplete(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "NAMES response of incorrect length");
            return;
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getParameters().get(1));
        if (channel != null) {
            this.namesMessages.add(event.getServerMessage());
            this.fire(new ChannelNamesUpdatedEvent(this.client, this.namesMessages, channel.snapshot()));
            this.namesMessages.clear();
        } else {
            this.trackException(event, "NAMES response sent for invalid channel name");
        }
    }

    private final List<ServerMessage> banMessages = new ArrayList<>();
    private final List<ModeInfo> bans = new ArrayList<>();

    private final List<ServerMessage> inviteMessages = new ArrayList<>();
    private final List<ModeInfo> invites = new ArrayList<>();

    private final List<ServerMessage> exceptMessages = new ArrayList<>();
    private final List<ModeInfo> excepts = new ArrayList<>();

    private final List<ServerMessage> quietMessages = new ArrayList<>();
    private final List<ModeInfo> quiets = new ArrayList<>();

    @NumericFilter(367) // BANLIST
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void banList(ClientReceiveNumericEvent event) {
        this.modeInfoList(event, "BANLIST", 'b', this.banMessages, this.bans);
    }

    @NumericFilter(346) // INVITELIST
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void inviteList(ClientReceiveNumericEvent event) {
        this.modeInfoList(event, "INVITELIST", 'I', this.inviteMessages, this.invites);
    }

    @NumericFilter(348) // EXCEPTLIST
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void exceptList(ClientReceiveNumericEvent event) {
        this.modeInfoList(event, "EXCEPTLIST", 'e', this.exceptMessages, this.excepts);
    }

    @NumericFilter(344) // QUIETLIST
    @NumericFilter(728) // QUIETLIST
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void quietList(ClientReceiveNumericEvent event) {
        this.modeInfoList(event, "QUIETLIST", 'q', this.quietMessages, this.quiets, (event.getNumeric() == 344) ? 0 : 1);
    }

    private void modeInfoList(@Nonnull ClientReceiveNumericEvent event, @Nonnull String name, char mode, @Nonnull List<ServerMessage> messageList, @Nonnull List<ModeInfo> infoList) {
        this.modeInfoList(event, name, mode, messageList, infoList, 0);
    }

    private void modeInfoList(@Nonnull ClientReceiveNumericEvent event, @Nonnull String name, char mode, @Nonnull List<ServerMessage> messageList, @Nonnull List<ModeInfo> infoList, int offset) {
        if (event.getParameters().size() < (3 + offset)) {
            this.trackException(event, name + " response of incorrect length");
            return;
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getParameters().get(1));
        if (channel != null) {
            messageList.add(event.getServerMessage());
            String creator = (event.getParameters().size() > (3 + offset)) ? event.getParameters().get((3 + offset)) : null;
            Instant creationTime = null;
            if (event.getParameters().size() > (4 + offset)) {
                try {
                    creationTime = Instant.ofEpochSecond(Integer.parseInt(event.getParameters().get((4 + offset))));
                } catch (NumberFormatException | DateTimeException ignored) {
                }
            }
            Optional<ChannelMode> channelMode = this.client.getServerInfo().getChannelMode(mode);
            if (channelMode.isPresent()) {
                infoList.add(new ModeData.IRCModeInfo(this.client, channel.snapshot(), channelMode.get(), event.getParameters().get((2 + offset)), Optional.ofNullable(creator), Optional.ofNullable(creationTime)));
            } else {
                this.trackException(event, name + " can't list if there's no '" + mode + "' mode");
            }
        } else {
            this.trackException(event, name + " response sent for invalid channel name");
        }
    }

    @NumericFilter(368) // End of ban list
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void banListEnd(ClientReceiveNumericEvent event) {
        this.endModeInfoList(event, "BANLIST", 'b', this.banMessages, this.bans);
    }

    @NumericFilter(347) // End of invite list
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void inviteListEnd(ClientReceiveNumericEvent event) {
        this.endModeInfoList(event, "INVITELIST", 'I', this.inviteMessages, this.invites);
    }

    @NumericFilter(349) // End of except list
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void exceptListEnd(ClientReceiveNumericEvent event) {
        this.endModeInfoList(event, "EXCEPTLIST", 'e', this.exceptMessages, this.excepts);
    }

    @NumericFilter(345) // End of quiet list
    @NumericFilter(729) // End of quiet list
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void quietListEnd(ClientReceiveNumericEvent event) {
        this.endModeInfoList(event, "QUIETLIST", 'q', this.quietMessages, this.quiets);
    }

    private void endModeInfoList(@Nonnull ClientReceiveNumericEvent event, @Nonnull String name, char mode, @Nonnull List<ServerMessage> messageList, @Nonnull List<ModeInfo> infoList) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, name + " response of incorrect length");
            return;
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getParameters().get(1));
        if (channel != null) {
            messageList.add(event.getServerMessage());
            Optional<ChannelMode> channelMode = this.client.getServerInfo().getChannelMode(mode);
            if (channelMode.isPresent()) {
                List<ModeInfo> modeInfos = new ArrayList<>(infoList);
                this.fire(new ChannelModeInfoListEvent(this.client, messageList, channel.snapshot(), channelMode.get(), modeInfos));
                channel.setModeInfoList(mode, modeInfos);
            } else {
                this.trackException(event, name + " can't list if there's no '" + mode + "' mode");
            }
            infoList.clear();
            messageList.clear();
        } else {
            this.trackException(event, name + " response sent for invalid channel name");
        }
    }

    private final List<String> motd = new ArrayList<>();
    private final List<ServerMessage> motdMessages = new ArrayList<>();

    @NumericFilter(375)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void motdStart(ClientReceiveNumericEvent event) {
        this.motd.clear();
        this.motdMessages.clear();
    }

    @NumericFilter(372)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void motdContent(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "MOTD message of incorrect length");
            return;
        }
        this.motd.add(event.getParameters().get(1));
        this.motdMessages.add(event.getServerMessage());
    }

    @NumericFilter(376)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void motdEnd(ClientReceiveNumericEvent event) {
        this.motdMessages.add(event.getServerMessage());
        this.client.getServerInfo().setMOTD(new ArrayList<>(this.motd));
        this.fire(new ClientReceiveMOTDEvent(this.client, this.motdMessages));
    }

    @NumericFilter(431) // No nick given
    @NumericFilter(432) // Erroneous nickname
    @NumericFilter(433) // Nick in use
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void nickInUse(ClientReceiveNumericEvent event) {
        NickRejectedEvent nickRejectedEvent = new NickRejectedEvent(this.client, event.getOriginalMessages(), this.client.getRequestedNick(), this.client.getRequestedNick() + '`');
        this.fire(nickRejectedEvent);
        this.client.sendNickChange(nickRejectedEvent.getNewNick());
    }

    @NumericFilter(710) // Knock
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void knock(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "KNOCK message of incorrect length");
            return;
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getParameters().get(1));
        if (channel != null) {
            ActorProvider.IRCUser user = (ActorProvider.IRCUser) this.client.getActorProvider().getActor(event.getParameters().get(2));
            this.fire(new ChannelKnockEvent(this.client, event.getOriginalMessages(), channel.snapshot(), user.snapshot()));
        } else {
            this.trackException(event, "KNOCK message sent for invalid channel name");
        }
    }

    @NumericFilter(730) // Monitor online
    @NumericFilter(731) // Monitor offline
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void monitorOnline(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "MONITOR status message of incorrect length");
            return;
        }
        List<ServerMessage> originalMessages = event.getOriginalMessages();
        for (String nick : event.getParameters().get(1).split(",")) {
            MonitoredNickStatusEvent monitorEvent;
            if (event.getNumeric() == 730) {
                monitorEvent = new MonitoredNickOnlineEvent(this.client, originalMessages, nick);
            } else {
                monitorEvent = new MonitoredNickOfflineEvent(this.client, originalMessages, nick);
            }
            this.fire(monitorEvent);
        }
    }

    private final List<String> monitorList = new ArrayList<>();
    private final List<ServerMessage> monitorListMessages = new ArrayList<>();

    @NumericFilter(732) // Monitor list
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void monitorList(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "MONITOR list message of incorrect length");
            return;
        }
        Collections.addAll(this.monitorList, event.getParameters().get(1).split(","));
        this.monitorListMessages.add(event.getServerMessage());
    }

    @NumericFilter(733) // Monitor list end
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void monitorListEnd(ClientReceiveNumericEvent event) {
        this.fire(new MonitoredNickListEvent(this.client, this.monitorListMessages, this.monitorList));
        this.monitorList.clear();
        this.monitorListMessages.clear();
    }

    @NumericFilter(734) // Monitor list full
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void monitorListFull(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "MONITOR list full message of incorrect length");
            return;
        }
        int limit;
        try {
            limit = Integer.parseInt(event.getParameters().get(1));
        } catch (NumberFormatException e) {
            this.trackException(event, "MONITOR list full message using non-int limit");
            return;
        }
        this.fire(new MonitoredNickListFullEvent(this.client, event.getOriginalMessages(), limit, Arrays.stream(event.getParameters().get(2).split(",")).collect(Collectors.toList())));
    }

    private final List<CapabilityState> capList = new ArrayList<>();
    private final List<ServerMessage> capListMessages = new ArrayList<>();
    private final List<CapabilityState> capLs = new ArrayList<>();
    private final List<ServerMessage> capLsMessages = new ArrayList<>();
    private static final int CAPABILITY_LIST_INDEX_DEFAULT = 2;

    @CommandFilter("CAP")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void cap(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "CAP message of incorrect length");
            return;
        }
        CapabilityNegotiationResponseEventBase responseEvent = null;
        int capabilityListIndex;
        if ("*".equals(event.getParameters().get(CAPABILITY_LIST_INDEX_DEFAULT))) {
            if (event.getParameters().size() < 4) {
                this.trackException(event, "CAP message of incorrect length");
                return;
            }
            capabilityListIndex = CAPABILITY_LIST_INDEX_DEFAULT + 1;
        } else {
            capabilityListIndex = CAPABILITY_LIST_INDEX_DEFAULT;
        }
        List<CapabilityState> capabilityStateList = Arrays.stream(event.getParameters().get(capabilityListIndex).split(" ")).filter(string -> !string.isEmpty()).map(capability -> new ManagerCapability.IRCCapabilityState(this.client, capability)).collect(Collectors.toCollection(ArrayList::new));
        switch (event.getParameters().get(1).toLowerCase()) {
            case "ack":
                this.client.getCapabilityManager().updateCapabilities(capabilityStateList);
                responseEvent = new CapabilitiesAcknowledgedEvent(this.client, event.getOriginalMessages(), this.client.getCapabilityManager().isNegotiating(), capabilityStateList);
                this.fire(responseEvent);
                break;
            case "list":
                this.capListMessages.add(event.getServerMessage());
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
                    this.fire(new CapabilitiesListEvent(this.client, this.capListMessages, states));
                    states.clear();
                }
                break;
            case "ls":
                this.capLsMessages.add(event.getServerMessage());
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
                    this.fireAndCapReq((CapabilitiesSupportedListEvent) responseEvent);
                }
                break;
            case "nak":
                this.client.getCapabilityManager().updateCapabilities(capabilityStateList);
                responseEvent = new CapabilitiesRejectedEvent(this.client, event.getOriginalMessages(), this.client.getCapabilityManager().isNegotiating(), capabilityStateList);
                this.fire(responseEvent);
                break;
            case "new":
                List<CapabilityState> statesAdded = new ArrayList<>(this.client.getCapabilityManager().getSupportedCapabilities());
                statesAdded.addAll(capabilityStateList);
                this.client.getCapabilityManager().setSupportedCapabilities(statesAdded);
                responseEvent = new CapabilitiesNewSupportedEvent(this.client, event.getOriginalMessages(), this.client.getCapabilityManager().isNegotiating(), capabilityStateList);
                this.fireAndCapReq((CapabilitiesNewSupportedEvent) responseEvent);
                break;
            case "del":
                List<CapabilityState> statesRemaining = new ArrayList<>(this.client.getCapabilityManager().getSupportedCapabilities());
                statesRemaining.removeAll(capabilityStateList);
                this.client.getCapabilityManager().setSupportedCapabilities(statesRemaining);
                responseEvent = new CapabilitiesDeletedSupportedEvent(this.client, event.getOriginalMessages(), this.client.getCapabilityManager().isNegotiating(), capabilityStateList);
                this.fire(responseEvent);
                break;
        }
        if (responseEvent != null) {
            if (responseEvent.isNegotiating() && responseEvent.isEndingNegotiation()) {
                this.client.sendRawLineImmediately("CAP END");
                this.client.getCapabilityManager().endNegotiation();
            }
        }
    }

    private void fireAndCapReq(@Nonnull CapabilityNegotiationResponseEventWithRequestBase responseEvent) {
        Set<String> capabilities = this.client.getCapabilityManager().getSupportedCapabilities().stream().map(CapabilityState::getName).collect(Collectors.toCollection(HashSet::new));
        capabilities.retainAll(CapabilityManager.Defaults.getDefaults());
        capabilities.removeAll(this.client.getCapabilityManager().getCapabilities().stream().map(CapabilityState::getName).collect(Collectors.toList()));
        if (!capabilities.isEmpty()) {
            responseEvent.setEndingNegotiation(false);
            capabilities.forEach(responseEvent::addRequest);
        }
        this.fire(responseEvent);
        List<String> requests = responseEvent.getRequests();
        if (!requests.isEmpty()) {
            CapabilityRequestCommand capabilityRequestCommand = new CapabilityRequestCommand(this.client);
            requests.forEach(capabilityRequestCommand::enable);
            capabilityRequestCommand.execute();
        }
    }

    @CommandFilter("CHGHOST")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void chghost(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() != 2) {
            this.trackException(event, "Invalid number of parameters for CHGHOST message");
            return;
        }

        if (!(event.getActor() instanceof User)) {
            this.trackException(event, "Invalid actor for CHGHOST message");
            return;
        }

        User user = (User) event.getActor();
        ActorProvider.IRCUser ircUser = this.client.getActorProvider().getUser(user.getNick());

        if (ircUser == null) {
            this.trackException(event, "Null old user for nick.");
            return;
        }

        User oldUser = ircUser.snapshot();

        String newUserString = event.getParameters().get(0);
        String newHostString = event.getParameters().get(1);

        if (!user.getHost().equals(newHostString)) {
            this.client.getActorProvider().trackUserHostnameChange(user.getNick(), newHostString);
            user = ircUser.snapshot();
            this.fire(new UserHostnameChangeEvent(this.client, event.getOriginalMessages(), oldUser, user));
        }

        if (!user.getUserString().equals(newUserString)) {
            this.client.getActorProvider().trackUserUserStringChange(user.getNick(), newUserString);
            user = ircUser.snapshot();
            this.fire(new UserUserStringChangeEvent(this.client, event.getOriginalMessages(), oldUser, user));
        }
    }

    @CommandFilter("ACCOUNT")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void account(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 1) {
            this.trackException(event, "ACCOUNT message of incorrect length");
            return;
        }
        String account = event.getParameters().get(0);
        this.client.getActorProvider().setUserAccount(((User) event.getActor()).getNick(), "*".equals(account) ? null : account);
    }

    @CommandFilter("AWAY")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void away(ClientReceiveCommandEvent event) {
        this.client.getActorProvider().setUserAway(((User) event.getActor()).getNick(), !event.getParameters().isEmpty());
    }

    @CommandFilter("NOTICE")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void notice(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "NOTICE message of incorrect length");
            return;
        }
        String message = event.getParameters().get(1);
        if (!(event.getActor() instanceof User)) {
            if (event.getActor() instanceof Server) {
                if (CTCPUtil.isCTCP(message)) {
                    this.trackException(event, "Server sent a CTCP message and I panicked");
                    return;
                }
                this.fire(new ServerNoticeEvent(this.client, event.getOriginalMessages(), (Server) event.getActor(), message));
            } else {
                this.trackException(event, "Message from neither server nor user");
            }
            return;
        }
        if (CTCPUtil.isCTCP(message)) {
            this.ctcp(event);
            return;
        }
        User user = (User) event.getActor();
        MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().get(0));
        if (messageTargetInfo instanceof MessageTargetInfo.Private) {
            this.fire(new PrivateNoticeEvent(this.client, event.getOriginalMessages(), user, event.getParameters().get(0), message));
        } else if (messageTargetInfo instanceof MessageTargetInfo.Channel) {
            MessageTargetInfo.Channel channelInfo = (MessageTargetInfo.Channel) messageTargetInfo;
            this.fire(new ChannelNoticeEvent(this.client, event.getOriginalMessages(), user, channelInfo.getChannel().snapshot(), message));
        } else if (messageTargetInfo instanceof MessageTargetInfo.TargetedChannel) {
            MessageTargetInfo.TargetedChannel channelInfo = (MessageTargetInfo.TargetedChannel) messageTargetInfo;
            this.fire(new ChannelTargetedNoticeEvent(this.client, event.getOriginalMessages(), user, channelInfo.getChannel().snapshot(), channelInfo.getPrefix(), message));
        }
    }

    @CommandFilter("PRIVMSG")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void privmsg(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "PRIVMSG message of incorrect length");
            return;
        }
        if (!(event.getActor() instanceof User)) {
            this.trackException(event, "Message from something other than a user");
            return;
        }
        if (CTCPUtil.isCTCP(event.getParameters().get(1))) {
            this.ctcp(event);
            return;
        }
        User user = (User) event.getActor();
        MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().get(0));
        if (messageTargetInfo instanceof MessageTargetInfo.Private) {
            this.fire(new PrivateMessageEvent(this.client, event.getOriginalMessages(), user, event.getParameters().get(0), event.getParameters().get(1)));
        } else if (messageTargetInfo instanceof MessageTargetInfo.Channel) {
            MessageTargetInfo.Channel channelInfo = (MessageTargetInfo.Channel) messageTargetInfo;
            this.fire(new ChannelMessageEvent(this.client, event.getOriginalMessages(), user, channelInfo.getChannel().snapshot(), event.getParameters().get(1)));
        } else if (messageTargetInfo instanceof MessageTargetInfo.TargetedChannel) {
            MessageTargetInfo.TargetedChannel channelInfo = (MessageTargetInfo.TargetedChannel) messageTargetInfo;
            this.fire(new ChannelTargetedMessageEvent(this.client, event.getOriginalMessages(), user, channelInfo.getChannel().snapshot(), channelInfo.getPrefix(), event.getParameters().get(1)));
        }
    }

    public void ctcp(ClientReceiveCommandEvent event) {
        final String ctcpMessage = CTCPUtil.fromCTCP(event.getParameters().get(1));
        final MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().get(0));
        User user = (User) event.getActor();
        switch (event.getCommand()) {
            case "NOTICE":
                if (messageTargetInfo instanceof MessageTargetInfo.Private) {
                    this.fire(new PrivateCTCPReplyEvent(this.client, event.getOriginalMessages(), user, event.getParameters().get(0), ctcpMessage));
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
                    PrivateCTCPQueryEvent ctcpEvent = new PrivateCTCPQueryEvent(this.client, event.getOriginalMessages(), user, event.getParameters().get(0), ctcpMessage, reply);
                    this.fire(ctcpEvent);
                    Optional<String> replyMessage = ctcpEvent.getReply();
                    if (ctcpEvent.isToClient()) {
                        replyMessage.ifPresent(message -> this.client.sendRawLine("NOTICE " + user.getNick() + " :" + CTCPUtil.toCTCP(message)));
                    }
                } else if (messageTargetInfo instanceof MessageTargetInfo.Channel) {
                    MessageTargetInfo.Channel channelInfo = (MessageTargetInfo.Channel) messageTargetInfo;
                    this.fire(new ChannelCTCPEvent(this.client, event.getOriginalMessages(), user, channelInfo.getChannel().snapshot(), ctcpMessage));
                } else if (messageTargetInfo instanceof MessageTargetInfo.TargetedChannel) {
                    MessageTargetInfo.TargetedChannel channelInfo = (MessageTargetInfo.TargetedChannel) messageTargetInfo;
                    this.fire(new ChannelTargetedCTCPEvent(this.client, event.getOriginalMessages(), user, channelInfo.getChannel().snapshot(), channelInfo.getPrefix(), ctcpMessage));
                }
                break;
        }
    }

    @CommandFilter("MODE")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void mode(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "MODE message of incorrect length");
            return;
        }
        MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().get(0));
        if (messageTargetInfo instanceof MessageTargetInfo.Private) {
            ModeStatusList<UserMode> statusList;
            try {
                statusList = ModeStatusList.fromUser(this.client, StringUtil.combineSplit(event.getParameters().toArray(new String[event.getParameters().size()]), 1));
            } catch (IllegalArgumentException e) {
                this.trackException(event, e.getMessage());
                return;
            }
            this.fire(new UserModeEvent(this.client, event.getOriginalMessages(), event.getActor(), event.getParameters().get(0), statusList));
            this.client.updateUserModes(statusList);
        } else if (messageTargetInfo instanceof MessageTargetInfo.Channel) {
            ActorProvider.IRCChannel channel = ((MessageTargetInfo.Channel) messageTargetInfo).getChannel();
            ModeStatusList<ChannelMode> statusList;
            try {
                statusList = ModeStatusList.fromChannel(this.client, StringUtil.combineSplit(event.getParameters().toArray(new String[event.getParameters().size()]), 1));
            } catch (IllegalArgumentException e) {
                this.trackException(event, e.getMessage());
                return;
            }
            Channel channelSnapshot = channel.snapshot();
            this.fire(new ChannelModeEvent(this.client, event.getOriginalMessages(), event.getActor(), channelSnapshot, statusList));
            statusList.getStatuses().stream().filter(status -> status.getMode().getType() == ChannelMode.Type.A_MASK).forEach(status -> channel.trackModeInfo(status.isSetting(), new ModeData.IRCModeInfo(this.client, channelSnapshot, status.getMode(), status.getParameter().get(), Optional.of(event.getActor().getName()), Optional.of(Instant.now()))));
            channel.updateChannelModes(statusList);
        } else {
            this.trackException(event, "MODE message sent for invalid target");
        }
    }

    @CommandFilter("JOIN")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void join(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 1) {
            this.trackException(event, "JOIN message of incorrect length");
            return;
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getParameters().get(0));
        if (channel != null) {
            if (event.getActor() instanceof User) {
                ActorProvider.IRCUser user = (ActorProvider.IRCUser) this.client.getActorProvider().getActor(event.getActor().getName());
                channel.trackUser(user, new HashSet<>());
                ChannelJoinEvent joinEvent = null;
                if (user.getNick().equals(this.client.getNick())) {
                    this.client.getActorProvider().trackChannel(channel);
                    if (this.client.getConfig().getNotNull(Config.QUERY_CHANNEL_INFO)) {
                        this.client.sendRawLine("MODE " + channel.getName());
                        this.client.sendRawLine("WHO " + channel.getName() + (this.client.getServerInfo().hasWhoXSupport() ? " %cuhsnfar" : ""));
                    }
                    if (this.client.getIntendedChannels().contains(channel.getName())) {
                        joinEvent = new RequestedChannelJoinCompleteEvent(this.client, event.getOriginalMessages(), channel.snapshot(), user.snapshot());
                    }
                }
                if (event.getParameters().size() > 2) {
                    if (!"*".equals(event.getParameters().get(1))) {
                        user.setAccount(event.getParameters().get(1));
                    }
                    user.setRealName(event.getParameters().get(2));
                }
                if (joinEvent == null) {
                    joinEvent = new ChannelJoinEvent(this.client, event.getOriginalMessages(), channel.snapshot(), user.snapshot());
                }
                this.fire(joinEvent);
            } else {
                this.trackException(event, "JOIN message sent for non-user");
            }
        } else {
            this.trackException(event, "JOIN message sent for invalid channel name");
        }
    }

    @CommandFilter("PART")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void part(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 1) {
            this.trackException(event, "PART message of incorrect length");
            return;
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getParameters().get(0));
        if (channel != null) {
            if (event.getActor() instanceof User) {
                User user = (User) event.getActor();
                boolean isSelf = user.getNick().equals(this.client.getNick());
                String partReason = (event.getParameters().size() > 1) ? event.getParameters().get(1) : "";
                ChannelPartEvent partEvent;
                if (isSelf && this.client.getIntendedChannels().contains(channel.getName())) {
                    partEvent = new RequestedChannelLeaveViaPartEvent(this.client, event.getOriginalMessages(), channel.snapshot(), user, partReason);
                } else {
                    partEvent = new ChannelPartEvent(this.client, event.getOriginalMessages(), channel.snapshot(), user, partReason);
                }
                this.fire(partEvent);
                channel.trackUserPart(user.getNick());
                if (isSelf) {
                    this.client.getActorProvider().unTrackChannel(channel);
                }
            } else {
                this.trackException(event, "PART message sent for non-user");
            }
        } else {
            this.trackException(event, "PART message sent for invalid channel name");
        }
    }

    @CommandFilter("QUIT")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void quit(ClientReceiveCommandEvent event) {
        if (event.getActor() instanceof User) {
            this.fire(new UserQuitEvent(this.client, event.getOriginalMessages(), (User) event.getActor(), (event.getParameters().isEmpty()) ? "" : event.getParameters().get(0)));
            this.client.getActorProvider().trackUserQuit(((User) event.getActor()).getNick());
        } else {
            this.trackException(event, "QUIT message sent for non-user");
        }
    }

    @CommandFilter("KICK")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void kick(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "KICK message of incorrect length");
            return;
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getParameters().get(0));
        if (channel != null) {
            ActorProvider.IRCUser kickedUser = this.client.getActorProvider().getUser(event.getParameters().get(1));
            if (kickedUser != null) {
                boolean isSelf = event.getParameters().get(1).equals(this.client.getNick());
                ChannelKickEvent kickEvent;
                String kickReason = (event.getParameters().size() > 2) ? event.getParameters().get(2) : "";
                if (isSelf && this.client.getIntendedChannels().contains(channel.getName())) {
                    kickEvent = new RequestedChannelLeaveViaKickEvent(this.client, event.getOriginalMessages(), channel.snapshot(), (User) event.getActor(), kickedUser.snapshot(), kickReason);
                } else {
                    kickEvent = new ChannelKickEvent(this.client, event.getOriginalMessages(), channel.snapshot(), (User) event.getActor(), kickedUser.snapshot(), kickReason);
                }
                this.fire(kickEvent);
                channel.trackUserPart(event.getParameters().get(1));
                if (isSelf) {
                    this.client.getActorProvider().unTrackChannel(channel);
                }
            } else {
                this.trackException(event, "KICK message sent for non-user");
            }
        } else {
            this.trackException(event, "KICK message sent for invalid channel name");
        }
    }

    @CommandFilter("NICK")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void nick(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 1) {
            this.trackException(event, "NICK message of incorrect length");
            return;
        }
        if (event.getActor() instanceof User) {
            boolean isSelf = ((User) event.getActor()).getNick().equals(this.client.getNick());
            ActorProvider.IRCUser user = this.client.getActorProvider().getUser(((User) event.getActor()).getNick());
            if (user == null) {
                if (isSelf) {
                    this.client.setCurrentNick(event.getParameters().get(0));
                    return; // Don't fail if NICK changes while not in a channel!
                }
                this.trackException(event, "NICK message sent for user not in tracked channels");
                return;
            }
            User oldUser = user.snapshot();
            this.client.getActorProvider().trackUserNickChange(user.getNick(), event.getParameters().get(0));
            User newUser = user.snapshot();
            this.fire(new UserNickChangeEvent(this.client, event.getOriginalMessages(), oldUser, newUser));
            if (isSelf) {
                this.client.setCurrentNick(event.getParameters().get(0));
            }
        } else {
            this.trackException(event, "NICK message sent for non-user");
        }
    }

    @CommandFilter("INVITE")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void invite(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "INVITE message of incorrect length");
            return;
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getParameters().get(1));
        if (channel != null) {
            if (this.client.getNick().equalsIgnoreCase(event.getParameters().get(0)) && this.client.getIntendedChannels().contains(channel.getName())) {
                this.client.sendRawLine("JOIN " + channel.getName());
            }
            this.fire(new ChannelInviteEvent(this.client, event.getOriginalMessages(), channel.snapshot(), event.getActor(), event.getParameters().get(0)));
        } else {
            this.trackException(event, "INVITE message sent for invalid channel name");
        }
    }

    @CommandFilter("TOPIC")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void topic(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "TOPIC message of incorrect length");
            return;
        }
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(event.getParameters().get(0));
        if (channel != null) {
            channel.setTopic(event.getParameters().get(1));
            channel.setTopic(System.currentTimeMillis(), event.getActor());
            this.fire(new ChannelTopicEvent(this.client, event.getOriginalMessages(), channel.snapshot(), true));
        } else {
            this.trackException(event, "TOPIC message sent for invalid channel name");
        }
    }

    @CommandFilter("WALLOPS")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void wallops(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 1) {
            this.trackException(event, "WALLOPS message of incorrect length");
            return;
        }
        this.fire(new WallopsEvent(this.client, event.getOriginalMessages(), event.getActor(), event.getParameters().get(0)));
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
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).toString();
    }

    private void fire(ClientEvent event) {
        this.client.getEventManager().callEvent(event);
    }

    @Nonnull
    private MessageTargetInfo getTypeByTarget(@Nonnull String target) {
        ActorProvider.IRCChannel channel = this.client.getActorProvider().getChannel(target);
        ChannelUserMode prefix = this.client.getServerInfo().getTargetedChannelInfo(target);
        if (prefix != null) {
            return new MessageTargetInfo.TargetedChannel(this.client.getActorProvider().getChannel(target.substring(1)), prefix);
        } else if (channel != null) {
            return new MessageTargetInfo.Channel(channel);
        }
        return MessageTargetInfo.Private.INSTANCE;
    }

    private void trackException(ClientReceiveServerMessageEvent event, String reason) {
        this.client.getExceptionListener().queue(new KittehServerMessageException(event.getServerMessage(), reason));
    }
}
