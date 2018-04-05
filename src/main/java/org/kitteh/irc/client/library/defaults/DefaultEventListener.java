/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.defaults;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.References;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.defaults.element.DefaultCapabilityState;
import org.kitteh.irc.client.library.defaults.element.mode.DefaultUserMode;
import org.kitteh.irc.client.library.defaults.listener.AbstractDefaultListenerBase;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.Server;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
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
import org.kitteh.irc.client.library.event.channel.ChannelCtcpEvent;
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
import org.kitteh.irc.client.library.event.channel.ChannelTargetedCtcpEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTargetedMessageEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTargetedNoticeEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTopicEvent;
import org.kitteh.irc.client.library.event.channel.ChannelUsersUpdatedEvent;
import org.kitteh.irc.client.library.event.channel.RequestedChannelJoinCompleteEvent;
import org.kitteh.irc.client.library.event.channel.UnexpectedChannelLeaveViaKickEvent;
import org.kitteh.irc.client.library.event.channel.UnexpectedChannelLeaveViaPartEvent;
import org.kitteh.irc.client.library.event.client.ClientAwayStatusChangeEvent;
import org.kitteh.irc.client.library.event.client.ClientNegotiationCompleteEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveMotdEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.client.NickRejectedEvent;
import org.kitteh.irc.client.library.event.helper.ClientEvent;
import org.kitteh.irc.client.library.event.helper.MonitoredNickStatusEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickListEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickListFullEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickOfflineEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickOnlineEvent;
import org.kitteh.irc.client.library.event.user.PrivateCtcpQueryEvent;
import org.kitteh.irc.client.library.event.user.PrivateCtcpReplyEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;
import org.kitteh.irc.client.library.event.user.PrivateNoticeEvent;
import org.kitteh.irc.client.library.event.user.ServerNoticeEvent;
import org.kitteh.irc.client.library.event.user.UserAccountStatusEvent;
import org.kitteh.irc.client.library.event.user.UserAwayMessageEvent;
import org.kitteh.irc.client.library.event.user.UserHostnameChangeEvent;
import org.kitteh.irc.client.library.event.user.UserModeEvent;
import org.kitteh.irc.client.library.event.user.UserNickChangeEvent;
import org.kitteh.irc.client.library.event.user.UserQuitEvent;
import org.kitteh.irc.client.library.event.user.UserUserStringChangeEvent;
import org.kitteh.irc.client.library.event.user.WallopsEvent;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;
import org.kitteh.irc.client.library.feature.twitch.TwitchListener;
import org.kitteh.irc.client.library.util.CtcpUtil;
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

/**
 * Default event listener.
 */
@SuppressWarnings("JavaDoc")
@net.engio.mbassy.listener.Listener(references = References.Strong)
public class DefaultEventListener extends AbstractDefaultListenerBase {
    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultEventListener(Client.WithManagement client) {
        super(client);
    }

    @NumericFilter(1)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void welcome(ClientReceiveNumericEvent event) {
        if (!event.getParameters().isEmpty()) {
            this.getClient().setCurrentNick(event.getParameters().get(0));
        } else {
            this.trackException(event, "Nickname missing; can't confirm");
        }
    }

    @NumericFilter(4)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void version(ClientReceiveNumericEvent event) {
        boolean isNotTwitch = this.getClient().getEventManager().getRegisteredEventListeners().stream().noneMatch(listener -> (listener instanceof TwitchListener));
        if (event.getParameters().size() > 1) {
            this.getClient().getServerInfo().setAddress(event.getParameters().get(1));
            if (event.getParameters().size() > 2) {
                this.getClient().getServerInfo().setVersion(event.getParameters().get(2));
                if (event.getParameters().size() > 3) {
                    List<UserMode> modes = new ArrayList<>(event.getParameters().get(3).length());
                    for (char mode : event.getParameters().get(3).toCharArray()) {
                        modes.add(new DefaultUserMode(this.getClient(), mode));
                    }
                    this.getClient().getServerInfo().setUserModes(modes);
                } else {
                    this.trackException(event, "Server user modes missing");
                }
            } else if (isNotTwitch) {
                this.trackException(event, "Server version and user modes missing");
            }
        } else {
            this.trackException(event, "Server address, version, and user modes missing");
        }
        if (isNotTwitch) {
            this.getClient().sendRawLineImmediately("WHOIS " + this.getClient().getNick());
        }
        this.fire(new ClientNegotiationCompleteEvent(this.getClient(), event.getActor(), this.getClient().getServerInfo()));
        this.getClient().startSending();
    }

    @NumericFilter(5)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void iSupport(ClientReceiveNumericEvent event) {
        for (int i = 1; i < event.getParameters().size(); i++) {
            this.getClient().getServerInfo().addISupportParameter(this.getClient().getISupportManager().createParameter(event.getParameters().get(i)));
        }
    }

    @NumericFilter(221) // UMODEIS
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void umode(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "UMODE response too short");
            return;
        }

        if (!this.getClient().getServerInfo().getCaseMapping().areEqualIgnoringCase(event.getParameters().get(0), this.getClient().getNick())) {
            this.trackException(event, "UMODE response for another user");
            return;
        }
        ModeStatusList<UserMode> modes;
        try {
            modes = ModeStatusList.fromUser(this.getClient(), StringUtil.combineSplit(event.getParameters().toArray(new String[event.getParameters().size()]), 1));
        } catch (IllegalArgumentException e) {
            this.trackException(event, e.getMessage());
            return;
        }
        this.getClient().setUserModes(modes);
    }

    @NumericFilter(305) // UNAWAY
    @NumericFilter(306) // NOWAWAY
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void away(ClientReceiveNumericEvent event) {
        this.fire(new ClientAwayStatusChangeEvent(this.getClient(), event.getOriginalMessages(), event.getNumeric() == 306));
    }

    @NumericFilter(324)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void channelMode(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "Channel mode info message too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getTrackedChannel(event.getParameters().get(1));
        if (channel.isPresent()) {
            ModeStatusList<ChannelMode> statusList;
            try {
                statusList = ModeStatusList.fromChannel(this.getClient(), StringUtil.combineSplit(event.getParameters().toArray(new String[event.getParameters().size()]), 2));
            } catch (IllegalArgumentException e) {
                this.trackException(event, e.getMessage());
                return;
            }
            this.getTracker().updateChannelModes(channel.get().getName(), statusList);
        } else {
            this.trackException(event, "Channel mode info message sent for invalid channel name");
        }
    }

    @NumericFilter(332) // Topic
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void topic(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "Topic message too short");
            return;
        }
        Optional<Channel> topicChannel = this.getTracker().getTrackedChannel(event.getParameters().get(1));
        if (topicChannel.isPresent()) {
            this.getTracker().setChannelTopic(topicChannel.get().getName(), event.getParameters().get(2));
        } else {
            this.trackException(event, "Topic message sent for invalid channel name");
        }
    }

    @NumericFilter(333) // Topic info
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void topicInfo(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 4) {
            this.trackException(event, "Topic message too short");
            return;
        }
        Optional<Channel> topicSetChannel = this.getTracker().getTrackedChannel(event.getParameters().get(1));
        if (topicSetChannel.isPresent()) {
            this.getTracker().setChannelTopicInfo(topicSetChannel.get().getName(), Long.parseLong(event.getParameters().get(3)) * 1000, this.getTracker().getActor(event.getParameters().get(2)));
            this.fire(new ChannelTopicEvent(this.getClient(), event.getOriginalMessages(), topicSetChannel.get(), false));
        } else {
            this.trackException(event, "Topic message sent for invalid channel name");
        }
    }

    private final List<ServerMessage> namesMessages = new ArrayList<>();

    @NumericFilter(353) // NAMES
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void names(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 4) {
            this.trackException(event, "NAMES response too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getTrackedChannel(event.getParameters().get(2));
        if (channel.isPresent()) {
            List<ChannelUserMode> channelUserModes = this.getClient().getServerInfo().getChannelUserModes();
            for (String combo : event.getParameters().get(3).split(" ")) {
                Set<ChannelUserMode> modes = new HashSet<>();
                for (int i = 0; i < combo.length(); i++) {
                    char c = combo.charAt(i);
                    Optional<ChannelUserMode> mode = channelUserModes.stream().filter(userMode -> userMode.getNickPrefix() == c).findFirst();
                    if (mode.isPresent()) {
                        modes.add(mode.get());
                    } else {
                        this.getTracker().trackChannelNick(channel.get().getName(), combo.substring(i), modes);
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
            this.trackException(event, "NAMES response too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getTrackedChannel(event.getParameters().get(1));
        if (channel.isPresent()) {
            this.namesMessages.add(event.getServerMessage());
            this.fire(new ChannelNamesUpdatedEvent(this.getClient(), this.namesMessages, channel.get()));
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
            this.trackException(event, name + " response too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getTrackedChannel(event.getParameters().get(1));
        if (channel.isPresent()) {
            messageList.add(event.getServerMessage());
            String creator = (event.getParameters().size() > (3 + offset)) ? event.getParameters().get((3 + offset)) : null;
            Instant creationTime = null;
            if (event.getParameters().size() > (4 + offset)) {
                try {
                    creationTime = Instant.ofEpochSecond(Integer.parseInt(event.getParameters().get((4 + offset))));
                } catch (NumberFormatException | DateTimeException ignored) {
                }
            }
            Optional<ChannelMode> channelMode = this.getClient().getServerInfo().getChannelMode(mode);
            if (channelMode.isPresent()) {
                infoList.add(new ModeInfo.DefaultModeInfo(this.getClient(), channel.get(), channelMode.get(), event.getParameters().get((2 + offset)), creator, creationTime));
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
            this.trackException(event, name + " response too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getTrackedChannel(event.getParameters().get(1));
        if (channel.isPresent()) {
            messageList.add(event.getServerMessage());
            Optional<ChannelMode> channelMode = this.getClient().getServerInfo().getChannelMode(mode);
            if (channelMode.isPresent()) {
                List<ModeInfo> modeInfos = new ArrayList<>(infoList);
                this.fire(new ChannelModeInfoListEvent(this.getClient(), messageList, channel.get(), channelMode.get(), modeInfos));
                this.getTracker().setChannelModeInfoList(channel.get().getName(), mode, modeInfos);
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
            this.trackException(event, "MOTD message too short");
            return;
        }
        this.motd.add(event.getParameters().get(1));
        this.motdMessages.add(event.getServerMessage());
    }

    @NumericFilter(376)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void motdEnd(ClientReceiveNumericEvent event) {
        this.motdMessages.add(event.getServerMessage());
        this.getClient().getServerInfo().setMotd(new ArrayList<>(this.motd));
        this.fire(new ClientReceiveMotdEvent(this.getClient(), this.motdMessages));
    }

    @NumericFilter(431) // No nick given
    @NumericFilter(432) // Erroneous nickname
    @NumericFilter(433) // Nick in use
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void nickInUse(ClientReceiveNumericEvent event) {
        NickRejectedEvent nickRejectedEvent = new NickRejectedEvent(this.getClient(), event.getOriginalMessages(), this.getClient().getRequestedNick(), this.getClient().getRequestedNick() + '`');
        this.fire(nickRejectedEvent);
        this.getClient().sendNickChange(nickRejectedEvent.getNewNick());
    }

    @NumericFilter(710) // Knock
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void knock(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "KNOCK message too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getTrackedChannel(event.getParameters().get(1));
        if (channel.isPresent()) {
            User user = (User) this.getTracker().getActor(event.getParameters().get(2));
            this.fire(new ChannelKnockEvent(this.getClient(), event.getOriginalMessages(), channel.get(), user));
        } else {
            this.trackException(event, "KNOCK message sent for invalid channel name");
        }
    }

    @NumericFilter(730) // Monitor online
    @NumericFilter(731) // Monitor offline
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void monitorOnline(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "MONITOR status message too short");
            return;
        }
        List<ServerMessage> originalMessages = event.getOriginalMessages();
        for (String nick : event.getParameters().get(1).split(",")) {
            MonitoredNickStatusEvent monitorEvent;
            if (event.getNumeric() == 730) {
                monitorEvent = new MonitoredNickOnlineEvent(this.getClient(), originalMessages, nick);
            } else {
                monitorEvent = new MonitoredNickOfflineEvent(this.getClient(), originalMessages, nick);
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
            this.trackException(event, "MONITOR list message too short");
            return;
        }
        Collections.addAll(this.monitorList, event.getParameters().get(1).split(","));
        this.monitorListMessages.add(event.getServerMessage());
    }

    @NumericFilter(733) // Monitor list end
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void monitorListEnd(ClientReceiveNumericEvent event) {
        this.fire(new MonitoredNickListEvent(this.getClient(), this.monitorListMessages, this.monitorList));
        this.monitorList.clear();
        this.monitorListMessages.clear();
    }

    @NumericFilter(734) // Monitor list full
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void monitorListFull(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "MONITOR list full message too short");
            return;
        }
        int limit;
        try {
            limit = Integer.parseInt(event.getParameters().get(1));
        } catch (NumberFormatException e) {
            this.trackException(event, "MONITOR list full message using non-int limit");
            return;
        }
        this.fire(new MonitoredNickListFullEvent(this.getClient(), event.getOriginalMessages(), limit, Arrays.stream(event.getParameters().get(2).split(",")).collect(Collectors.toList())));
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
            this.trackException(event, "CAP message too short");
            return;
        }
        CapabilityNegotiationResponseEventBase responseEvent = null;
        int capabilityListIndex;
        if ("*".equals(event.getParameters().get(CAPABILITY_LIST_INDEX_DEFAULT))) {
            if (event.getParameters().size() < 4) {
                this.trackException(event, "CAP message too short");
                return;
            }
            capabilityListIndex = CAPABILITY_LIST_INDEX_DEFAULT + 1;
        } else {
            capabilityListIndex = CAPABILITY_LIST_INDEX_DEFAULT;
        }
        List<CapabilityState> capabilityStateList = Arrays.stream(event.getParameters().get(capabilityListIndex).split(" ")).filter(string -> !string.isEmpty()).map(capability -> new DefaultCapabilityState(this.getClient(), capability)).collect(Collectors.toCollection(ArrayList::new));
        switch (event.getParameters().get(1).toLowerCase()) {
            case "ack":
                this.getClient().getCapabilityManager().updateCapabilities(capabilityStateList);
                responseEvent = new CapabilitiesAcknowledgedEvent(this.getClient(), event.getOriginalMessages(), this.getClient().getCapabilityManager().isNegotiating(), capabilityStateList);
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
                    this.getClient().getCapabilityManager().setCapabilities(states);
                    this.fire(new CapabilitiesListEvent(this.getClient(), this.capListMessages, states));
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
                    this.getClient().getCapabilityManager().setSupportedCapabilities(states);
                    responseEvent = new CapabilitiesSupportedListEvent(this.getClient(), this.capLsMessages, this.getClient().getCapabilityManager().isNegotiating(), states);
                    this.fireAndCapReq((CapabilitiesSupportedListEvent) responseEvent);
                }
                break;
            case "nak":
                this.getClient().getCapabilityManager().updateCapabilities(capabilityStateList);
                responseEvent = new CapabilitiesRejectedEvent(this.getClient(), event.getOriginalMessages(), this.getClient().getCapabilityManager().isNegotiating(), capabilityStateList);
                this.fire(responseEvent);
                break;
            case "new":
                List<CapabilityState> statesAdded = new ArrayList<>(this.getClient().getCapabilityManager().getSupportedCapabilities());
                statesAdded.addAll(capabilityStateList);
                this.getClient().getCapabilityManager().setSupportedCapabilities(statesAdded);
                responseEvent = new CapabilitiesNewSupportedEvent(this.getClient(), event.getOriginalMessages(), this.getClient().getCapabilityManager().isNegotiating(), capabilityStateList);
                this.fireAndCapReq((CapabilitiesNewSupportedEvent) responseEvent);
                break;
            case "del":
                List<CapabilityState> statesRemaining = new ArrayList<>(this.getClient().getCapabilityManager().getSupportedCapabilities());
                statesRemaining.removeAll(capabilityStateList);
                this.getClient().getCapabilityManager().setSupportedCapabilities(statesRemaining);
                responseEvent = new CapabilitiesDeletedSupportedEvent(this.getClient(), event.getOriginalMessages(), this.getClient().getCapabilityManager().isNegotiating(), capabilityStateList);
                this.fire(responseEvent);
                break;
        }
        if (responseEvent != null) {
            if (responseEvent.isNegotiating() && responseEvent.isEndingNegotiation()) {
                this.getClient().sendRawLineImmediately("CAP END");
                this.getClient().getCapabilityManager().endNegotiation();
            }
        }
    }

    private void fireAndCapReq(@Nonnull CapabilityNegotiationResponseEventWithRequestBase responseEvent) {
        Set<String> capabilities = this.getClient().getCapabilityManager().getSupportedCapabilities().stream().map(CapabilityState::getName).collect(Collectors.toCollection(HashSet::new));
        capabilities.retainAll(CapabilityManager.Defaults.getDefaults());
        capabilities.removeAll(this.getClient().getCapabilityManager().getCapabilities().stream().map(CapabilityState::getName).collect(Collectors.toList()));
        if (!capabilities.isEmpty()) {
            responseEvent.setEndingNegotiation(false);
            capabilities.forEach(responseEvent::addRequest);
        }
        this.fire(responseEvent);
        List<String> requests = responseEvent.getRequests();
        if (!requests.isEmpty()) {
            CapabilityRequestCommand capabilityRequestCommand = new CapabilityRequestCommand(this.getClient());
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
        Optional<User> optUser = this.getTracker().getTrackedUser(user.getNick());

        if (!optUser.isPresent()) {
            this.trackException(event, "Null old user for nick");
            return;
        }

        User oldUser = optUser.get();

        String newUserString = event.getParameters().get(0);
        String newHostString = event.getParameters().get(1);

        if (!user.getHost().equals(newHostString)) {
            this.getTracker().trackUserHostnameChange(user.getNick(), newHostString);
            this.fire(new UserHostnameChangeEvent(this.getClient(), event.getOriginalMessages(), oldUser, this.getTracker().getTrackedUser(user.getNick()).get()));
        }

        if (!user.getUserString().equals(newUserString)) {
            this.getTracker().trackUserUserStringChange(user.getNick(), newUserString);
            this.fire(new UserUserStringChangeEvent(this.getClient(), event.getOriginalMessages(), oldUser, this.getTracker().getTrackedUser(user.getNick()).get()));
        }
    }

    @CommandFilter("ACCOUNT")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void account(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 1) {
            this.trackException(event, "ACCOUNT message too short");
            return;
        }
        if (!(event.getActor() instanceof User)) {
            this.trackException(event, "ACCOUNT message from something other than a user");
            return;
        }
        String accountParameter = event.getParameters().get(0);
        String accountName = "*".equals(accountParameter) ? null : accountParameter;
        this.fire(new UserAccountStatusEvent(this.getClient(), event.getOriginalMessages(), (User) event.getActor(), accountName));
        this.getTracker().setUserAccount(((User) event.getActor()).getNick(), accountName);
    }

    @CommandFilter("AWAY")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void away(ClientReceiveCommandEvent event) {
        if (!(event.getActor() instanceof User)) {
            this.trackException(event, "AWAY message from something other than a user");
            return;
        }
        String awayMessage = event.getParameters().isEmpty() ? null : StringUtil.combineSplit(event.getParameters().toArray(new String[event.getParameters().size()]), 0);
        this.fire(new UserAwayMessageEvent(this.getClient(), event.getOriginalMessages(), (User) event.getActor(), awayMessage));
        this.getTracker().setUserAway(((User) event.getActor()).getNick(), awayMessage);
    }

    @CommandFilter("NOTICE")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void notice(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "NOTICE message too short");
            return;
        }
        String message = event.getParameters().get(1);
        if (!(event.getActor() instanceof User)) {
            if (event.getActor() instanceof Server) {
                if (CtcpUtil.isCtcp(message)) {
                    this.trackException(event, "Server sent a CTCP message and I panicked");
                    return;
                }
                this.fire(new ServerNoticeEvent(this.getClient(), event.getOriginalMessages(), (Server) event.getActor(), message));
            } else {
                this.trackException(event, "Message from neither server nor user");
            }
            return;
        }
        if (CtcpUtil.isCtcp(message)) {
            this.ctcp(event);
            return;
        }
        User user = (User) event.getActor();
        MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().get(0));
        if (messageTargetInfo instanceof MessageTargetInfo.Private) {
            this.fire(new PrivateNoticeEvent(this.getClient(), event.getOriginalMessages(), user, event.getParameters().get(0), message));
        } else if (messageTargetInfo instanceof MessageTargetInfo.ChannelInfo) {
            MessageTargetInfo.ChannelInfo channelInfo = (MessageTargetInfo.ChannelInfo) messageTargetInfo;
            this.fire(new ChannelNoticeEvent(this.getClient(), event.getOriginalMessages(), user, channelInfo.getChannel(), message));
        } else if (messageTargetInfo instanceof MessageTargetInfo.TargetedChannel) {
            MessageTargetInfo.TargetedChannel channelInfo = (MessageTargetInfo.TargetedChannel) messageTargetInfo;
            this.fire(new ChannelTargetedNoticeEvent(this.getClient(), event.getOriginalMessages(), user, channelInfo.getChannel(), channelInfo.getPrefix(), message));
        }
    }

    @CommandFilter("PRIVMSG")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void privmsg(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "PRIVMSG message too short");
            return;
        }
        if (!(event.getActor() instanceof User)) {
            this.trackException(event, "Message from something other than a user");
            return;
        }
        if (CtcpUtil.isCtcp(event.getParameters().get(1))) {
            this.ctcp(event);
            return;
        }
        User user = (User) event.getActor();
        MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().get(0));
        if (messageTargetInfo instanceof MessageTargetInfo.Private) {
            this.fire(new PrivateMessageEvent(this.getClient(), event.getOriginalMessages(), user, event.getParameters().get(0), event.getParameters().get(1)));
        } else if (messageTargetInfo instanceof MessageTargetInfo.ChannelInfo) {
            MessageTargetInfo.ChannelInfo channelInfo = (MessageTargetInfo.ChannelInfo) messageTargetInfo;
            this.fire(new ChannelMessageEvent(this.getClient(), event.getOriginalMessages(), user, channelInfo.getChannel(), event.getParameters().get(1)));
        } else if (messageTargetInfo instanceof MessageTargetInfo.TargetedChannel) {
            MessageTargetInfo.TargetedChannel channelInfo = (MessageTargetInfo.TargetedChannel) messageTargetInfo;
            this.fire(new ChannelTargetedMessageEvent(this.getClient(), event.getOriginalMessages(), user, channelInfo.getChannel(), channelInfo.getPrefix(), event.getParameters().get(1)));
        }
    }

    public void ctcp(ClientReceiveCommandEvent event) {
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

    @CommandFilter("MODE")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void mode(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "MODE message too short");
            return;
        }
        MessageTargetInfo messageTargetInfo = this.getTypeByTarget(event.getParameters().get(0));
        if (messageTargetInfo instanceof MessageTargetInfo.Private) {
            ModeStatusList<UserMode> statusList;
            try {
                statusList = ModeStatusList.fromUser(this.getClient(), StringUtil.combineSplit(event.getParameters().toArray(new String[event.getParameters().size()]), 1));
            } catch (IllegalArgumentException e) {
                this.trackException(event, e.getMessage());
                return;
            }
            this.fire(new UserModeEvent(this.getClient(), event.getOriginalMessages(), event.getActor(), event.getParameters().get(0), statusList));
            this.getClient().updateUserModes(statusList);
        } else if (messageTargetInfo instanceof MessageTargetInfo.ChannelInfo) {
            Channel channel = ((MessageTargetInfo.ChannelInfo) messageTargetInfo).getChannel();
            ModeStatusList<ChannelMode> statusList;
            try {
                statusList = ModeStatusList.fromChannel(this.getClient(), StringUtil.combineSplit(event.getParameters().toArray(new String[event.getParameters().size()]), 1));
            } catch (IllegalArgumentException e) {
                this.trackException(event, e.getMessage());
                return;
            }
            this.fire(new ChannelModeEvent(this.getClient(), event.getOriginalMessages(), event.getActor(), channel, statusList));
            statusList.getStatuses().stream()
                    .filter(status -> status.getMode().getType() == ChannelMode.Type.A_MASK)
                    .forEach(status -> this.getTracker().trackChannelModeInfo(channel.getName(), status.isSetting(),
                            new ModeInfo.DefaultModeInfo(this.getClient(), channel, status.getMode(), status.getParameter().get(), event.getActor().getName(), Instant.now())
                    ));
            this.getTracker().updateChannelModes(channel.getName(), statusList);
        } else {
            this.trackException(event, "MODE message sent for invalid target");
        }
    }

    @CommandFilter("JOIN")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void join(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 1) {
            this.trackException(event, "JOIN message too short");
            return;
        }
        String channelName = event.getParameters().get(0);
        if (this.getClient().getServerInfo().isValidChannel(channelName)) {
            if (event.getActor() instanceof User) {
                this.getTracker().trackChannel(channelName);
                Channel channel = this.getTracker().getTrackedChannel(channelName).get();
                User user = (User) event.getActor();
                this.getTracker().trackChannelUser(channelName, user, new HashSet<>());
                ChannelJoinEvent joinEvent = null;
                if (user.getNick().equals(this.getClient().getNick())) {
                    if (this.getClient().getActorTracker().shouldQueryChannelInformation()) {
                        this.getClient().sendRawLine("MODE " + channelName);
                        this.getClient().sendRawLine("WHO " + channelName + (this.getClient().getServerInfo().hasWhoXSupport() ? " %cuhsnfar" : ""));
                    }
                    if (this.getClient().getIntendedChannels().contains(channelName)) {
                        joinEvent = new RequestedChannelJoinCompleteEvent(this.getClient(), event.getOriginalMessages(), channel, user);
                    }
                }
                if (event.getParameters().size() > 2) {
                    if (!"*".equals(event.getParameters().get(1))) {
                        this.getTracker().setUserAccount(user.getNick(), event.getParameters().get(1));
                    }
                    this.getTracker().setUserRealName(user.getNick(), event.getParameters().get(2));
                }
                if (joinEvent == null) {
                    joinEvent = new ChannelJoinEvent(this.getClient(), event.getOriginalMessages(), channel, user);
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
            this.trackException(event, "PART message too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getTrackedChannel(event.getParameters().get(0));
        if (channel.isPresent()) {
            if (event.getActor() instanceof User) {
                User user = (User) event.getActor();
                boolean isSelf = user.getNick().equals(this.getClient().getNick());
                String partReason = (event.getParameters().size() > 1) ? event.getParameters().get(1) : "";
                ChannelPartEvent partEvent;
                if (isSelf && this.getClient().getIntendedChannels().contains(channel.get().getName())) {
                    partEvent = new UnexpectedChannelLeaveViaPartEvent(this.getClient(), event.getOriginalMessages(), channel.get(), user, partReason);
                } else {
                    partEvent = new ChannelPartEvent(this.getClient(), event.getOriginalMessages(), channel.get(), user, partReason);
                }
                this.fire(partEvent);
                this.getTracker().trackUserPart(channel.get().getName(), user.getNick());
                if (isSelf) {
                    this.getTracker().unTrackChannel(channel.get().getName());
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
            this.fire(new UserQuitEvent(this.getClient(), event.getOriginalMessages(), (User) event.getActor(), (event.getParameters().isEmpty()) ? "" : event.getParameters().get(0)));
            this.getTracker().trackUserQuit(((User) event.getActor()).getNick());
        } else {
            this.trackException(event, "QUIT message sent for non-user");
        }
    }

    @CommandFilter("KICK")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void kick(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "KICK message too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getTrackedChannel(event.getParameters().get(0));
        if (channel.isPresent()) {
            Optional<User> kickedUser = this.getTracker().getTrackedUser(event.getParameters().get(1));
            if (kickedUser.isPresent()) {
                boolean isSelf = event.getParameters().get(1).equals(this.getClient().getNick());
                ClientEvent kickEvent;
                String kickReason = (event.getParameters().size() > 2) ? event.getParameters().get(2) : "";
                if (isSelf && this.getClient().getIntendedChannels().contains(channel.get().getName())) {
                    kickEvent = new UnexpectedChannelLeaveViaKickEvent(this.getClient(), event.getOriginalMessages(), channel.get(), event.getActor(), kickedUser.get(), kickReason);
                } else {
                    kickEvent = new ChannelKickEvent(this.getClient(), event.getOriginalMessages(), channel.get(), event.getActor(), kickedUser.get(), kickReason);
                }
                this.fire(kickEvent);
                this.getTracker().trackUserPart(channel.get().getName(), event.getParameters().get(1));
                if (isSelf) {
                    this.getTracker().unTrackChannel(channel.get().getName());
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
            this.trackException(event, "NICK message too short");
            return;
        }
        if (event.getActor() instanceof User) {
            boolean isSelf = ((User) event.getActor()).getNick().equals(this.getClient().getNick());
            Optional<User> user = this.getTracker().getTrackedUser(((User) event.getActor()).getNick());
            if (!user.isPresent()) {
                if (isSelf) {
                    this.getClient().setCurrentNick(event.getParameters().get(0));
                    return; // Don't fail if NICK changes while not in a channel!
                }
                this.trackException(event, "NICK message sent for user not in tracked channels");
                return;
            }
            User oldUser = user.get();
            this.getTracker().trackUserNickChange(user.get().getNick(), event.getParameters().get(0));
            User newUser = user.get();
            this.fire(new UserNickChangeEvent(this.getClient(), event.getOriginalMessages(), oldUser, newUser));
            if (isSelf) {
                this.getClient().setCurrentNick(event.getParameters().get(0));
            }
        } else {
            this.trackException(event, "NICK message sent for non-user");
        }
    }

    @CommandFilter("INVITE")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void invite(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "INVITE message too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getTrackedChannel(event.getParameters().get(1));
        if (channel.isPresent()) {
            if (this.getClient().getNick().equalsIgnoreCase(event.getParameters().get(0)) && this.getClient().getIntendedChannels().contains(channel.get().getName())) {
                this.getClient().sendRawLine("JOIN " + channel.get().getName());
            }
            this.fire(new ChannelInviteEvent(this.getClient(), event.getOriginalMessages(), channel.get(), event.getActor(), event.getParameters().get(0)));
        } else {
            this.trackException(event, "INVITE message sent for invalid channel name");
        }
    }

    @CommandFilter("TOPIC")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void topic(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "TOPIC message too short");
            return;
        }
        Optional<Channel> channel = this.getTracker().getTrackedChannel(event.getParameters().get(0));
        if (channel.isPresent()) {
            this.getTracker().setChannelTopic(channel.get().getName(), event.getParameters().get(1));
            this.getTracker().setChannelTopicInfo(channel.get().getName(), System.currentTimeMillis(), event.getActor());
            this.fire(new ChannelTopicEvent(this.getClient(), event.getOriginalMessages(), channel.get(), true));
        } else {
            this.trackException(event, "TOPIC message sent for invalid channel name");
        }
    }

    @CommandFilter("WALLOPS")
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void wallops(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 1) {
            this.trackException(event, "WALLOPS message too short");
            return;
        }
        this.fire(new WallopsEvent(this.getClient(), event.getOriginalMessages(), event.getActor(), event.getParameters().get(0)));
    }

    protected static class MessageTargetInfo {
        protected static class ChannelInfo extends MessageTargetInfo {
            private final Channel channel;

            protected ChannelInfo(Channel channel) {
                this.channel = channel;
            }

            @Nonnull
            protected Channel getChannel() {
                return this.channel;
            }

            @Nonnull
            @Override
            public String toString() {
                return new ToStringer(this).toString();
            }
        }

        protected static class TargetedChannel extends MessageTargetInfo {
            private final Channel channel;
            private final ChannelUserMode prefix;

            protected TargetedChannel(Channel channel, ChannelUserMode prefix) {
                this.channel = channel;
                this.prefix = prefix;
            }

            @Nonnull
            protected Channel getChannel() {
                return this.channel;
            }

            @Nonnull
            protected ChannelUserMode getPrefix() {
                return this.prefix;
            }

            @Nonnull
            @Override
            public String toString() {
                return new ToStringer(this).toString();
            }
        }

        protected static class Private extends MessageTargetInfo {
            static final Private INSTANCE = new Private();

            protected Private() {
            }

            @Nonnull
            @Override
            public String toString() {
                return new ToStringer(this).toString();
            }
        }
    }

    @Nonnull
    protected MessageTargetInfo getTypeByTarget(@Nonnull String target) {
        Optional<Channel> channel = this.getTracker().getTrackedChannel(target);
        Optional<ChannelUserMode> prefix = this.getClient().getServerInfo().getTargetedChannelInfo(target);
        if (prefix.isPresent()) {
            return new MessageTargetInfo.TargetedChannel(this.getTracker().getTrackedChannel(target.substring(1)).get(), prefix.get());
        } else if (channel.isPresent()) {
            return new MessageTargetInfo.ChannelInfo(channel.get());
        }
        return MessageTargetInfo.Private.INSTANCE;
    }
}
