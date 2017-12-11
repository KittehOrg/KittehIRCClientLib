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
package org.kitteh.irc.client.library.element.mode;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.command.ChannelModeCommand;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ClientLinked;
import org.kitteh.irc.client.library.util.ToStringer;
import org.kitteh.irc.client.library.util.mask.Mask;

import javax.annotation.Nonnull;
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
        private final Mask.AsString mask;
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
        public DefaultModeInfo(@Nonnull Client client, @Nonnull Channel channel, @Nonnull ChannelMode mode, @Nonnull Mask.AsString mask, @Nonnull String creator, @Nonnull Instant creationTime) {
            this.client = client;
            this.creator = creator;
            this.channel = channel;
            this.mask = mask;
            this.creationTime = creationTime;
            this.mode = mode;
        }

        @Nonnull
        @Override
        public Optional<String> getCreator() {
            return Optional.ofNullable(this.creator);
        }

        @Nonnull
        @Override
        public Channel getChannel() {
            return this.channel;
        }

        @Nonnull
        @Override
        public Client getClient() {
            return this.client;
        }

        @Nonnull
        @Override
        public Mask.AsString getMask() {
            return this.mask;
        }

        @Nonnull
        @Override
        public ChannelMode getMode() {
            return this.mode;
        }

        @Nonnull
        @Override
        public Optional<Instant> getCreationTime() {
            return Optional.ofNullable(this.creationTime);
        }

        @Nonnull
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
    @Nonnull
    Optional<String> getCreator();

    /**
     * Gets the channel for which this entry exists.
     *
     * @return the channel
     */
    @Nonnull
    Channel getChannel();

    /**
     * Gets the mask.
     *
     * @return the mask
     */
    @Nonnull
    Mask.AsString getMask();

    /**
     * Gets the mode for which this info exists.
     *
     * @return the mode
     */
    @Nonnull
    ChannelMode getMode();

    /**
     * Gets the time at which this entry was created.
     *
     * @return creation time, if known
     */
    @Nonnull
    Optional<Instant> getCreationTime();

    /**
     * Attempts to remove this item from the channel.
     */
    default void remove() {
        new ChannelModeCommand(this.getClient(), this.getChannel().getName()).add(false, this.getMode(), this.getMask().asString()).execute();
    }
}
