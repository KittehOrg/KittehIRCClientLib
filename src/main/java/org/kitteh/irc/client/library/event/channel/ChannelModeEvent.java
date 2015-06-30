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
package org.kitteh.irc.client.library.event.channel;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.abstractbase.ActorChannelEventBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Channel a la mode.
 */
public class ChannelModeEvent extends ActorChannelEventBase<Actor> {
    private final boolean setting;
    private final char mode;
    private final String arg;
    private final ChannelUserMode channelUserMode;

    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param actor the mode setter
     * @param channel the channel in which the mode is being set
     * @param setting if the mode is being set or unset
     * @param mode the mode being set or unset
     * @param channelUserMode prefix mode if such
     * @param arg the argument presented for the mode
     */
    public ChannelModeEvent(@Nonnull Client client, @Nonnull Actor actor, @Nonnull Channel channel, boolean setting, char mode, @Nullable ChannelUserMode channelUserMode, @Nullable String arg) {
        super(client, actor, channel);
        this.setting = setting;
        this.mode = mode;
        this.channelUserMode = channelUserMode;
        this.arg = arg;
    }

    /**
     * Gets the argument for the mode.
     *
     * @return the mode argument, or null if no argument
     */
    @Nullable
    public String getArgument() {
        return this.arg;
    }

    /**
     * Gets the mode.
     *
     * @return the mode
     */
    public char getMode() {
        return this.mode;
    }

    /**
     * Gets information on the mode if it sets a prefix on the user.
     *
     * @return the channel mode information
     * @see #isPrefix()
     * @see #getPrefixedUser()
     */
    @Nullable
    public ChannelUserMode getPrefix() {
        return this.channelUserMode;
    }

    /**
     * Gets the user whose prefix has been changed, if this mode change
     * involves a user prefix.
     *
     * @return user changing prefix
     * @see #isPrefix()
     * @see #getPrefix()
     */
    @Nullable
    public User getPrefixedUser() {
        return (this.isPrefix() && (this.arg != null)) ? this.getChannel().getUser(this.arg) : null;
    }

    /**
     * Gets if the mode set sets a user's prefix. If true, you can acquire
     * additional information via {@link #getPrefix()}.
     *
     * @return true if this mode sets a prefix
     * @see #getPrefix()
     * @see #getPrefixedUser()
     */
    public boolean isPrefix() {
        return this.channelUserMode != null;
    }

    /**
     * Gets if the mode is being set or unset.
     *
     * @return true if set, false if unset
     */
    public boolean isSetting() {
        return this.setting;
    }
}