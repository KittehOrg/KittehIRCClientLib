/*
 * * Copyright (C) 2013-2021 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.feature.EventListenerSupplier;

import java.util.function.Function;

/**
 * Default event listeners registered by the client.
 */
public enum DefaultListeners implements EventListenerSupplier {
    /**
     * ACCOUNT handling.
     *
     * @see DefaultAccountListener
     */
    ACCOUNT(DefaultAccountListener::new),
    /**
     * ACK handling.
     *
     * @see DefaultAckListener
     */
    ACK(DefaultAckListener::new),
    /**
     * AWAY handling.
     *
     * @see DefaultAwayListener
     */
    AWAY(DefaultAwayListener::new),
    /**
     * Ban list handling.
     *
     * @see DefaultBanListListener
     */
    BANLIST(DefaultBanListListener::new),
    /**
     * CAP handling.
     *
     * @see DefaultCapListener
     */
    CAP(DefaultCapListener::new),
    /**
     * CHGHOST handling.
     *
     * @see DefaultChgHostListener
     */
    CHGHOST(DefaultChgHostListener::new),
    /**
     * Except list handling.
     *
     * @see DefaultExceptListListener
     */
    EXCEPTLIST(DefaultExceptListListener::new),
    /**
     * INVITE handling.
     *
     * @see DefaultInviteListener
     */
    INVITE(DefaultInviteListener::new),
    /**
     * Invite list handling.
     *
     * @see DefaultInviteListListener
     */
    INVITELIST(DefaultInviteListListener::new),
    /**
     * ISUPPORT handling.
     *
     * @see DefaultISupportListener
     */
    ISUPPORT(DefaultISupportListener::new),
    /**
     * JOIN handling.
     *
     * @see DefaultJoinListener
     */
    JOIN(DefaultJoinListener::new),
    /**
     * KICK handling.
     *
     * @see DefaultKickListener
     */
    KICK(DefaultKickListener::new),
    /**
     * KNOCK handling.
     *
     * @see DefaultKnockListener
     */
    KNOCK(DefaultKnockListener::new),
    /**
     * MODE handling.
     *
     * @see DefaultModeListener
     */
    MODE(DefaultModeListener::new),
    /**
     * MONITOR handling.
     *
     * @see DefaultMonitorListener
     */
    MONITOR(DefaultMonitorListener::new),
    /**
     * MOTD handling.
     *
     * @see DefaultMotdListener
     */
    MOTD(DefaultMotdListener::new),
    /**
     * NAMES handling.
     *
     * @see DefaultNamesListener
     */
    NAMES(DefaultNamesListener::new),
    /**
     * NICK handling.
     *
     * @see DefaultNickListener
     */
    NICK(DefaultNickListener::new),
    /**
     * Nick rejection handler.
     *
     * @see DefaultNickRejectedListener
     */
    NICK_REJECTED(DefaultNickRejectedListener::new),
    /**
     * NOTICE handling.
     *
     * @see DefaultNoticeListener
     */
    NOTICE(DefaultNoticeListener::new),
    /**
     * PART handling.
     *
     * @see DefaultPartListener
     */
    PART(DefaultPartListener::new),
    /**
     * PRIVMSG handling.
     *
     * @see DefaultPrivmsgListener
     */
    PRIVMSG(DefaultPrivmsgListener::new),
    /**
     * Quiet list handling.
     *
     * @see DefaultQuietListListener
     */
    QUIETLIST(DefaultQuietListListener::new),
    /**
     * QUIT handling.
     *
     * @see DefaultQuitListener
     */
    QUIT(DefaultQuitListener::new),
    /**
     * TOPIC handling.
     *
     * @see DefaultTopicListener
     */
    TOPIC(DefaultTopicListener::new),
    /**
     * UMODE handling.
     *
     * @see DefaultUserModeListener
     */
    USERMODE(DefaultUserModeListener::new),
    /**
     * Version handling.
     *
     * @see DefaultVersionListener
     */
    VERSION(DefaultVersionListener::new),
    /**
     * WALLOPS handling.
     *
     * @see DefaultWallopsListener
     */
    WALLOPS(DefaultWallopsListener::new),
    /**
     * Welcome handling.
     *
     * @see DefaultWelcomeListener
     */
    WELCOME(DefaultWelcomeListener::new),
    /**
     * WHO handling.
     *
     * @see DefaultWhoListener
     */
    WHO(DefaultWhoListener::new),
    /**
     * WHOIS handling.
     *
     * @see DefaultWhoisListener
     */
    WHOIS(DefaultWhoisListener::new);

    private final Function<Client.WithManagement, Object> function;

    DefaultListeners(@NonNull Function<Client.WithManagement, Object> function) {
        this.function = function;
    }

    @Override
    public Function<Client.WithManagement, Object> getConstructingFunction() {
        return this.function;
    }
}
