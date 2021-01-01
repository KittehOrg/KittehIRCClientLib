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
package org.kitteh.irc.client.library.element.mode;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.command.ChannelModeCommand;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ClientLinked;
import org.kitteh.irc.client.library.util.Mask;

import java.time.Instant;
import java.util.Optional;

/**
 * Represents a type A mode information entry.
 */
public interface ModeInfo extends ClientLinked {
    /**
     * Gets the name of the party listed as creating the entry. This may be a
     * nickname, a service name, a server name, etc.
     *
     * @return name, if known, of who created this entry
     */
    @NonNull Optional<String> getCreator();

    /**
     * Gets the channel for which this entry exists.
     *
     * @return the channel
     */
    @NonNull Channel getChannel();

    /**
     * Gets the mask.
     *
     * @return the mask
     */
    @NonNull Mask getMask();

    /**
     * Gets the mode for which this info exists.
     *
     * @return the mode
     */
    @NonNull ChannelMode getMode();

    /**
     * Gets the time at which this entry was created.
     *
     * @return creation time, if known
     */
    @NonNull Optional<Instant> getCreationTime();

    /**
     * Attempts to remove this item from the channel.
     */
    default void remove() {
        new ChannelModeCommand(this.getClient(), this.getChannel().getName()).add(ModeStatus.Action.REMOVE, this.getMode(), this.getMask().asString()).execute();
    }
}
