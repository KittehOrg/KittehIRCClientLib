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
package org.kitteh.irc.client.library.element.mode;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.command.ChannelModeCommand;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ClientLinked;
import org.kitteh.irc.client.library.util.Mask;
import org.kitteh.irc.client.library.util.ToStringer;

import java.time.Instant;
import java.util.Optional;

/**
 * Represents a type A mode information entry.
 */
public interface ModeInfo extends ClientLinked {
    /**
     * A default ModeInfo implementation.
     */
    class DefaultModeInfo implements ModeInfo {
        private final Client client;
        private final Instant creationTime;
        private final String creator;
        private final Channel channel;
        private final Mask mask;
        private final ChannelMode mode;

        /**
         * Constructs the mode info.
         *
         * @param client the client
         * @param channel channel
         * @param mode mode
         * @param mask mask
         * @param creator creator, if known
         * @param creationTime creation time, if known
         */
        public DefaultModeInfo(@NonNull Client client, @NonNull Channel channel, @NonNull ChannelMode mode, @NonNull String mask, @Nullable String creator, @Nullable Instant creationTime) {
            this.client = client;
            this.creator = creator;
            this.channel = channel;
            this.mask = Mask.fromString(mask);
            this.creationTime = creationTime;
            this.mode = mode;
        }

        @NonNull
        @Override
        public Optional<String> getCreator() {
            return Optional.ofNullable(this.creator);
        }

        @NonNull
        @Override
        public Channel getChannel() {
            return this.channel;
        }

        @NonNull
        @Override
        public Client getClient() {
            return this.client;
        }

        @NonNull
        @Override
        public Mask getMask() {
            return this.mask;
        }

        @NonNull
        @Override
        public ChannelMode getMode() {
            return this.mode;
        }

        @NonNull
        @Override
        public Optional<Instant> getCreationTime() {
            return Optional.ofNullable(this.creationTime);
        }

        @NonNull
        @Override
        public String toString() {
            return new ToStringer(this).add("client", this.client).add("channel", this.channel).add("mode", this.mode).add("mask", this.mask).add("creator", this.creator).add("creationTime", this.creationTime).toString();
        }
    }

    /**
     * Gets the name of the party listed as creating the entry. This may be a
     * nickname, a service name, a server name, etc.
     *
     * @return name, if known, of who created this entry
     */
    @NonNull
    Optional<String> getCreator();

    /**
     * Gets the channel for which this entry exists.
     *
     * @return the channel
     */
    @NonNull
    Channel getChannel();

    /**
     * Gets the mask.
     *
     * @return the mask
     */
    @NonNull
    Mask getMask();

    /**
     * Gets the mode for which this info exists.
     *
     * @return the mode
     */
    @NonNull
    ChannelMode getMode();

    /**
     * Gets the time at which this entry was created.
     *
     * @return creation time, if known
     */
    @NonNull
    Optional<Instant> getCreationTime();

    /**
     * Attempts to remove this item from the channel.
     */
    default void remove() {
        new ChannelModeCommand(this.getClient(), this.getChannel().getName()).add(false, this.getMode(), this.getMask().asString()).execute();
    }
}
