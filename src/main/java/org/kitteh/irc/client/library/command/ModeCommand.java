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
package org.kitteh.irc.client.library.command;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ChannelMode;
import org.kitteh.irc.client.library.element.ChannelModeStatus;
import org.kitteh.irc.client.library.element.ChannelModeStatusList;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Commands a la MODE.
 */
public class ModeCommand extends ChannelCommand {
    private static final int MODES_PER_LINE = 3;

    private final List<ChannelModeStatus> changes = new ArrayList<>();

    /**
     * Constructs a MODE command for a given channel.
     *
     * @param client the client on which this command is executing
     * @param channel channel targeted
     * @throws IllegalArgumentException if null parameters or Channel is from
     * another Client
     */
    public ModeCommand(@Nonnull Client client, @Nonnull Channel channel) {
        super(client, channel);
    }

    /**
     * Constructs a MODE command for a given channel.
     *
     * @param client the client on which this command is executing
     * @param channel channel targeted
     * @throws IllegalArgumentException if null parameters or Channel is from
     * another Client
     */
    public ModeCommand(@Nonnull Client client, @Nonnull String channel) {
        super(client, channel);
    }

    /**
     * Adds a mode change without a parameter.
     *
     * @param add true if adding, false if removing
     * @param mode the mode to be changed
     * @return this ModeCommand
     * @throws IllegalArgumentException if mode invalid
     */
    @Nonnull
    public ModeCommand add(boolean add, @Nonnull ChannelMode mode) {
        return this.addChange(add, mode, null);
    }

    /**
     * Adds a mode change.
     *
     * @param add true if adding, false if removing
     * @param mode the mode to be changed
     * @param parameter mode parameter
     * @return this ModeCommand
     * @throws IllegalArgumentException if mode invalid comes from a
     * different client or parameter is null
     */
    @Nonnull
    public ModeCommand add(boolean add, @Nonnull ChannelMode mode, @Nonnull String parameter) {
        Sanity.nullCheck(parameter, "Parameter cannot be null");
        return this.addChange(add, mode, parameter);
    }

    /**
     * Adds a mode change.
     *
     * @param add true if adding, false if removing
     * @param mode the mode to be changed
     * @param parameter user whose nick will be sent or null if not needed
     * @return this ModeCommand
     * @throws IllegalArgumentException if mode invalid or either mode or
     * user comes from a different client or parameter is null
     */
    @Nonnull
    public ModeCommand add(boolean add, @Nonnull ChannelUserMode mode, @Nonnull User parameter) {
        Sanity.nullCheck(parameter, "User cannot be null");
        Sanity.truthiness(parameter.getClient() == this.getClient(), "User comes from a different Client");
        return this.addChange(add, mode, parameter.getNick());
    }

    @Nonnull
    private ModeCommand addChange(boolean add, @Nonnull ChannelMode mode, @Nullable String parameter) {
        Sanity.nullCheck(mode, "Mode cannot be null");
        Sanity.truthiness(mode.getClient() == this.getClient(), "Mode comes from a different Client");
        if (parameter != null) {
            Sanity.safeMessageCheck(parameter);
            this.changes.add(new ChannelModeStatus(add, mode, parameter));
        } else {
            this.changes.add(new ChannelModeStatus(add, mode));
        }
        return this;
    }

    @Override
    public synchronized void execute() {
        List<ChannelModeStatus> queue = new LinkedList<>();
        for (ChannelModeStatus modeChange : this.changes) {
            queue.add(modeChange);
            if (queue.size() == MODES_PER_LINE) {
                this.send(queue);
            }
        }
        if (!queue.isEmpty()) {
            this.send(queue);
        }
    }

    private void send(@Nonnull List<ChannelModeStatus> queue) {
        this.getClient().sendRawLine("MODE " + this.getChannel() + ' ' + ChannelModeStatusList.of(new ArrayList<>(queue)).getStatusString());
        queue.clear();
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("client", this.getClient()).add("modes", this.changes).toString();
    }
}