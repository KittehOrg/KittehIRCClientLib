/*
 * * Copyright (C) 2013-2025 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.defaults.element.mode;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ModeInfo;
import org.kitteh.irc.client.library.util.Mask;
import org.kitteh.irc.client.library.util.ToStringer;

import java.time.Instant;
import java.util.Optional;

/**
 * A default ModeInfo implementation.
 */
public class DefaultModeInfo implements ModeInfo {
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

    @Override
    public @NonNull Optional<String> getCreator() {
        return Optional.ofNullable(this.creator);
    }

    @Override
    public @NonNull Channel getChannel() {
        return this.channel;
    }

    @Override
    public @NonNull Client getClient() {
        return this.client;
    }

    @Override
    public @NonNull Mask getMask() {
        return this.mask;
    }

    @Override
    public @NonNull ChannelMode getMode() {
        return this.mode;
    }

    @Override
    public @NonNull Optional<Instant> getCreationTime() {
        return Optional.ofNullable(this.creationTime);
    }

    @Override
    public @NonNull
    String toString() {
        return new ToStringer(this).add("client", this.client).add("channel", this.channel).add("mode", this.mode).add("mask", this.mask).add("creator", this.creator).add("creationTime", this.creationTime).toString();
    }
}