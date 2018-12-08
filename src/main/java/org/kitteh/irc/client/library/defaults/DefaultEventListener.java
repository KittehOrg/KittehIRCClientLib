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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.mode.DefaultUserMode;
import org.kitteh.irc.client.library.defaults.listener.AbstractDefaultListenerBase;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.ModeInfo;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.event.channel.ChannelKnockEvent;
import org.kitteh.irc.client.library.event.channel.ChannelModeInfoListEvent;
import org.kitteh.irc.client.library.event.channel.ChannelNamesUpdatedEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTopicEvent;
import org.kitteh.irc.client.library.event.client.ClientAwayStatusChangeEvent;
import org.kitteh.irc.client.library.event.client.ClientNegotiationCompleteEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveMotdEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.client.NickRejectedEvent;
import org.kitteh.irc.client.library.event.helper.MonitoredNickStatusEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickListEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickListFullEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickOfflineEvent;
import org.kitteh.irc.client.library.event.user.MonitoredNickOnlineEvent;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;
import org.kitteh.irc.client.library.feature.twitch.TwitchListener;
import org.kitteh.irc.client.library.util.StringUtil;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private void modeInfoList(@NonNull ClientReceiveNumericEvent event, @NonNull String name, char mode, @NonNull List<ServerMessage> messageList, @NonNull List<ModeInfo> infoList) {
        this.modeInfoList(event, name, mode, messageList, infoList, 0);
    }

    private void modeInfoList(@NonNull ClientReceiveNumericEvent event, @NonNull String name, char mode, @NonNull List<ServerMessage> messageList, @NonNull List<ModeInfo> infoList, int offset) {
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

    private void endModeInfoList(@NonNull ClientReceiveNumericEvent event, @NonNull String name, char mode, @NonNull List<ServerMessage> messageList, @NonNull List<ModeInfo> infoList) {
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
}
