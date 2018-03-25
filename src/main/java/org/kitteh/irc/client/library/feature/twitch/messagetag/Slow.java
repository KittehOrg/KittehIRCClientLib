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
package org.kitteh.irc.client.library.feature.twitch.messagetag;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;

/**
 * Message tag slow, indicating non-moderators have to wait between messages.
 */
public class Slow extends MessageTagManager.DefaultMessageTag {
    /**
     * Name of this message tag.
     */
    public static final String NAME = "slow";

    /**
     * Function to create this message tag.
     */
    @SuppressWarnings("ConstantConditions")
    public static final TriFunction<Client, String, String, Slow> FUNCTION = (client, name, value) -> new Slow(name, value, Integer.parseInt(value));

    private final int delay;

    private Slow(@Nonnull String name, @Nonnull String value, int delay) {
        super(name, value);
        this.delay = delay;
    }

    /**
     * Gets the delay between messages.
     *
     * @return time, in seconds, chatters have to wait
     */
    public int getDelay() {
        return this.delay;
    }
}
