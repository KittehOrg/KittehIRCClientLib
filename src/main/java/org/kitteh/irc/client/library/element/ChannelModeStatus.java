/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.element;

import org.kitteh.irc.client.library.Client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A particular status of a channel mode.
 */
public interface ChannelModeStatus {
    /**
     * Gets the client this status is for.
     *
     * @return client
     */
    @Nonnull
    default Client getClient() {
        return this.getMode().getClient();
    }

    /**
     * Gets the {@link ChannelMode} describing this mode status.
     *
     * @return the mode
     */
    @Nonnull
    ChannelMode getMode();

    /**
     * Gets the parameter for the mode status, if applicable.
     *
     * @return parameter or null if no parameter
     */
    @Nullable
    String getParameter();

    /**
     * Gets if this mode is being set.
     *
     * @return true for setting, false for removing
     */
    boolean isSetting();
}