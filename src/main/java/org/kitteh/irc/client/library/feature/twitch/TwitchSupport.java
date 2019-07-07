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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.listener.DefaultVersionListener;
import org.kitteh.irc.client.library.feature.twitch.messagetag.*;
import org.kitteh.irc.client.library.util.Sanity;

/**
 * A class for introducing Twitch support to a KICL client.
 */
public final class TwitchSupport {
    /**
     * Capability to receive commands.
     */
    public static final String CAPABILITY_COMMANDS = "twitch.tv/commands";

    /**
     * Capability to receive JOIN, MODE, NAMES, and PART.
     */
    public static final String CAPABILITY_MEMBERSHIP = "twitch.tv/membership";

    /**
     * Capability to receive tags.
     */
    public static final String CAPABILITY_TAGS = "twitch.tv/tags";

    /**
     * Adds support for Twitch to the client. Registers all the Twitch tags,
     * switches out an event listener for connection information (Twitch does
     * it differently), disables querying channel info (missing on Twitch),
     * and replaces the message sending queue supplier with a Twitch-targeted
     * queue for regular users.
     *
     * @param client unconnected client
     * @return the same client
     * @see TwitchListener
     * @see TwitchDelaySender
     */
    public static Client addSupport(@NonNull Client client) {
        return addSupport(client, false);
    }

    /**
     * Adds support for Twitch to the client. Registers all the Twitch tags,
     * switches out an event listener for connection information (Twitch does
     * it differently), disables querying channel info (missing on Twitch),
     * and sets the message sending queue supplier to be a Twitch-targeted
     * queue for regular users or mod/op depending on the second parameter.
     * This method must be called prior to connecting.
     *
     * @param client unconnected client
     * @param alwaysModOrOp if the client will be a mod/op in EVERY channel
     * @return the same client
     * @see TwitchListener
     * @see TwitchDelaySender
     */
    public static Client addSupport(@NonNull Client client, boolean alwaysModOrOp) {
        Sanity.truthiness(!((Client.WithManagement) client).isConnectionAlive(), "Client already connected!");

        client.getEventManager().getRegisteredEventListeners()
                .stream()
                .filter(l -> l.getClass().equals(DefaultVersionListener.class))
                .forEach(client.getEventManager()::unregisterEventListener);
        client.getEventManager().registerEventListener(new TwitchListener(client));
        client.getEventManager().registerEventListener(new TwitchVersionListener((Client.WithManagement) client));

        client.setMessageSendingQueueSupplier(TwitchDelaySender.getSupplier(alwaysModOrOp));

        ((Client.WithManagement) client).getActorTracker().setQueryChannelInformation(false);

        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, Badges.NAME, Badges.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, BanDuration.NAME, BanDuration.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, BanReason.NAME, BanReason.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, Bits.NAME, Bits.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, BroadcasterLang.NAME, BroadcasterLang.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, Color.NAME, Color.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, DisplayName.NAME, DisplayName.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, Emotes.NAME, Emotes.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, EmoteSets.NAME, EmoteSets.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, Id.NAME, Id.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, MsgId.NAME, MsgId.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, MsgParamMonths.NAME, MsgParamMonths.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, MsgParamSubPlan.NAME, MsgParamSubPlan.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, MsgParamSubPlanName.NAME, MsgParamSubPlanName.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, Mod.NAME, Mod.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, R9k.NAME, R9k.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, RoomId.NAME, RoomId.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, Slow.NAME, Slow.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, SubsOnly.NAME, SubsOnly.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, Subscriber.NAME, Subscriber.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, SystemMsg.NAME, SystemMsg.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, Turbo.NAME, Turbo.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, User.NAME, User.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, UserId.NAME, UserId.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, UserType.NAME, UserType.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, Login.NAME, Login.FUNCTION);
        client.getMessageTagManager().registerTagCreator(CAPABILITY_TAGS, MsgParamCumulativeMonths.NAME, MsgParamCumulativeMonths.FUNCTION);

        return client;
    }

    /**
     * Checks if the given client has had Twitch support added. Checks by
     * looking for the twitch-modified event listener added in {@link
     * #addSupport(Client, boolean)}.
     *
     * @param client client to check
     * @return true if the client has Twitch support added
     */
    public static boolean hasSupport(@NonNull Client client) {
        return client.getEventManager().getRegisteredEventListeners()
                .stream()
                .anyMatch(l -> l.getClass().equals(TwitchVersionListener.class));
    }

    private TwitchSupport() {
    }
}
