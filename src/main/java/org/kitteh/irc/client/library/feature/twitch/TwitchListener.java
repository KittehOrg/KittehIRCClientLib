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
package org.kitteh.irc.client.library.feature.twitch;

import net.engio.mbassy.listener.Handler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;
import org.kitteh.irc.client.library.feature.twitch.event.ClearChatEvent;
import org.kitteh.irc.client.library.feature.twitch.event.GlobalUserStateEvent;
import org.kitteh.irc.client.library.feature.twitch.event.RoomStateEvent;
import org.kitteh.irc.client.library.feature.twitch.event.UserNoticeEvent;
import org.kitteh.irc.client.library.feature.twitch.event.UserStateEvent;
import org.kitteh.irc.client.library.feature.twitch.event.WhisperEvent;
import org.kitteh.irc.client.library.util.Sanity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Helpful things.
 */
public class TwitchListener {
    private final Client client;

    /**
     * Creates a new TwitchListener.
     *
     * @param client the client for which it will be registered
     */
    public TwitchListener(@NonNull Client client) {
        this.client = Sanity.nullCheck(client, "Client cannot be null");
    }

    @Handler
    public void capList(@NonNull CapabilitiesSupportedListEvent event) {
        List<String> already = this.client.getCapabilityManager().getCapabilities().stream().map(CapabilityState::getName).collect(Collectors.toList());
        if (!already.contains(TwitchSupport.CAPABILITY_COMMANDS)) {
            event.addRequest(TwitchSupport.CAPABILITY_COMMANDS);
        }
        if (!already.contains(TwitchSupport.CAPABILITY_MEMBERSHIP)) {
            event.addRequest(TwitchSupport.CAPABILITY_MEMBERSHIP);
        }
        if (!already.contains(TwitchSupport.CAPABILITY_TAGS)) {
            event.addRequest(TwitchSupport.CAPABILITY_TAGS);
        }
    }

    @CommandFilter("CLEARCHAT")
    @Handler(priority = Integer.MAX_VALUE - 2)
    public void clearChat(ClientReceiveCommandEvent event) {
        this.client.getEventManager().callEvent(new ClearChatEvent(this.client, event.getOriginalMessages(), this.getChannel(event)));
    }

    @CommandFilter("GLOBALUSERSTATE")
    @Handler(priority = Integer.MAX_VALUE - 2)
    public void globalUserState(ClientReceiveCommandEvent event) {
        this.client.getEventManager().callEvent(new GlobalUserStateEvent(this.client, event.getOriginalMessages()));
    }

    @CommandFilter("ROOMSTATE")
    @Handler(priority = Integer.MAX_VALUE - 2)
    public void roomState(ClientReceiveCommandEvent event) {
        this.client.getEventManager().callEvent(new RoomStateEvent(this.client, event.getOriginalMessages(), this.getChannel(event)));
    }

    @CommandFilter("USERNOTICE")
    @Handler(priority = Integer.MAX_VALUE - 2)
    public void userNotice(ClientReceiveCommandEvent event) {
        String message = null;
        if (event.getParameters().size() > 1) {
            message = event.getParameters().get(1);
        }
        this.client.getEventManager().callEvent(new UserNoticeEvent(this.client, event.getOriginalMessages(), this.getChannel(event), message));
    }

    @CommandFilter("USERSTATE")
    @Handler(priority = Integer.MAX_VALUE - 2)
    public void userState(ClientReceiveCommandEvent event) {
        this.client.getEventManager().callEvent(new UserStateEvent(this.client, event.getOriginalMessages(), this.getChannel(event)));
    }

    @CommandFilter("WHISPER")
    @Handler(priority = Integer.MAX_VALUE - 2)
    public void whisper(ClientReceiveCommandEvent event) {
        if (event.getParameters().size() < 2) {
            this.client.getExceptionListener().queue(new KittehServerMessageException(event.getOriginalMessages(), "WHISPER didn't contain enough parameters"));
            return;
        }

        if (!(event.getActor() instanceof org.kitteh.irc.client.library.element.User)) {
            this.client.getExceptionListener().queue(new KittehServerMessageException(event.getOriginalMessages(), "Received WHISPER from non-user"));
            return;
        }

        final String target = event.getParameters().get(0);
        final String message = event.getParameters().get(1);
        final org.kitteh.irc.client.library.element.User sender = (org.kitteh.irc.client.library.element.User) event.getActor();
        this.client.getEventManager().callEvent(new WhisperEvent(this.client, event.getOriginalMessages(), sender, target, message));
    }

    private @NonNull Channel getChannel(ClientReceiveCommandEvent event) {
        Optional<Channel> channel = this.client.getChannel(event.getParameters().get(0));
        if (!channel.isPresent()) {
            throw new KittehServerMessageException(event.getServerMessage(), "Invalid channel name");
        }
        return channel.get();
    }
}
