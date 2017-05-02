/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
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
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ChannelEventBase;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.helper.ChannelEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * Helpful things.
 */
public class Twitch {
    /**
     * Capability to receive JOIN, MODE, NAMES, and PART.
     */
    public static final String CAPABILITY_MEMBERSHIP = "twitch.tv/membership";

    /**
     * Capability to receive tags.
     */
    public static final String CAPABILITY_TAGS = "twitch.tv/tags";

    /**
     * An event listener that will request the membership capability, which
     * will let the client receive JOIN, MODE, NAMES and PART messages.
     */
    public static class MembershipListener {
        /**
         * Adds the membership ability
         *
         * @param client client gaining this ability
         */
        public static void add(@Nonnull Client client) {
            client.getEventManager().registerEventListener(new MembershipListener());
        }

        private MembershipListener() {
        }

        @Handler
        public void capList(@Nonnull CapabilitiesSupportedListEvent event) {
            event.addRequest(CAPABILITY_MEMBERSHIP);
            // TODO less aggressive
        }
    }

    /**
     * An event listener that will request the tags capability, which will
     * let the client receive tags and tag-related messages, as well as adds
     * the appropriate tags to the {@link MessageTagManager}.
     */
    public static class TagListener {
        private final Client client;

        private TagListener(@Nonnull Client client) {
            this.client = client;
        }

        /**
         * Adds support for tags.
         *
         * @param client client gaining this ability
         */
        public static void add(@Nonnull Client client) {
            client.getEventManager().registerEventListener(new TagListener(client));
            client.getMessageTagManager().registerTagCreator("ban-duration", CAPABILITY_TAGS, BanDuration.FUNCTION);
            client.getMessageTagManager().registerTagCreator("ban-reason", CAPABILITY_TAGS, BanReason.FUNCTION);
        }

        @Handler
        public void capList(@Nonnull CapabilitiesSupportedListEvent event) {
            event.addRequest(CAPABILITY_TAGS);
        }

        @CommandFilter("CLEARCHAT")
        @Handler(priority = Integer.MAX_VALUE - 2)
        public void clearchat(ClientReceiveCommandEvent event) {
            Optional<MessageTag> reasonTag = event.getMessageTags().stream().filter(tag -> tag instanceof BanReason).findAny();
            if (!reasonTag.isPresent() || !reasonTag.get().getValue().isPresent()) {
                throw new KittehServerMessageException(event.getServerMessage(), "No ban reason present in ban");
            }
            String reason = reasonTag.get().getValue().get();
            Optional<Channel> channel = this.client.getChannel(event.getParameters().get(0));
            if (!channel.isPresent()) {
                throw new KittehServerMessageException(event.getServerMessage(), "Invalid channel name");
            }
            Optional<MessageTag> durationTag = event.getMessageTags().stream().filter(tag -> tag instanceof BanDuration).findAny();
            OptionalInt duration = durationTag
                    .map(Stream::of)
                    .orElseGet(Stream::empty)
                    .mapToInt(tag -> ((BanDuration) tag).getDuration())
                    .findFirst();
            this.client.getEventManager().callEvent(new ClearchatEvent(this.client, event.getOriginalMessages(), channel.get(), reason, duration));
        }
    }

    /**
     * Message tag for ban duration.
     */
    public static class BanDuration extends MessageTagManager.DefaultMessageTag {
        private static final TriFunction<Client, String, Optional<String>, BanDuration> FUNCTION = (client, name, value) -> new BanDuration(name, value, Integer.parseInt(value.get()));

        private final int duration;

        /**
         * Constructs ban reason message tag.
         *
         * @param name tag name
         * @param value tag value or {@link Optional#EMPTY}
         * @param duration duration, in seconds, of the ban
         */
        public BanDuration(@Nonnull String name, @Nonnull Optional<String> value, int duration) {
            super(name, value);
            this.duration = duration;
        }

        /**
         * Gets the ban duration.
         *
         * @return time, in seconds, the ban will last
         */
        public int getDuration() {
            return this.duration;
        }
    }

    /**
     * Message tag for ban reason.
     */
    public static class BanReason extends MessageTagManager.DefaultMessageTag {
        private static final TriFunction<Client, String, Optional<String>, BanReason> FUNCTION = (client, name, value) -> new BanReason(name, value);

        /**
         * Constructs ban reason message tag.
         *
         * @param name tag name
         * @param value tag value or {@link Optional#EMPTY}
         */
        public BanReason(@Nonnull String name, @Nonnull Optional<String> value) {
            super(name, value);
        }
    }

    /**
     * An event for when Twitch sends a CLEARCHAT message meaning a ban has
     * happened.
     */
    public static class ClearchatEvent extends ChannelEventBase implements ChannelEvent {
        private final String banReason;
        private final OptionalInt duration;

        /**
         * Constructs the event.
         *
         * @param client the client
         * @param originalMessages original messages
         * @param channel the channel
         */
        ClearchatEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull Channel channel, @Nonnull String banReason, @Nonnull OptionalInt duration) {
            super(client, originalMessages, channel);
            this.banReason = banReason;
            this.duration = duration;
        }

        /**
         * Gets the ban duration.
         *
         * @return ban duration in seconds or {@link Optional#EMPTY} if
         * permanent
         */
        public OptionalInt getBanDuration() {
            return this.duration;
        }

        /**
         * Gets the ban reason.
         *
         * @return ban reason
         */
        public String getBanReason() {
            return this.banReason;
        }
    }
}
