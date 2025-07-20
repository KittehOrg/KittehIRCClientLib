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
package org.kitteh.irc.client.library.element.mode;

import org.jspecify.annotations.NonNull;
import org.kitteh.irc.client.library.Client;

import java.util.Optional;

/**
 * Describes a channel mode that a user can have, such as op.
 */
public interface ChannelUserMode extends ChannelMode {
    /**
     * Gets the nickname prefix character.
     *
     * @return the character displayed in front of a nickname
     */
    char getNickPrefix();

    @Override
    default @NonNull Type getType() {
        return Type.B_PARAMETER_ALWAYS;
    }

    /**
     * Gets a channel user mode by character for a given client.
     *
     * @param client client
     * @param mode   mode to get
     * @return the mode, if present
     */
    public static @NonNull Optional<ChannelUserMode> get(@NonNull Client client, char mode) {
        return client.getServerInfo().getChannelUserModes().stream().filter(m -> m.getChar() == mode).findFirst();
    }
}
