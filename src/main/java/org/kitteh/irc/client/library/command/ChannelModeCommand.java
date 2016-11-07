/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.ModeStatus;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Commands a la MODE.
 */
public class ChannelModeCommand extends ChannelCommand {
    private static final int PARAMETER_MODES_PER_LINE = 3;

    private final List<ModeStatus<ChannelMode>> changes = new ArrayList<>();

    /**
     * Constructs a MODE command for a given channel.
     *
     * @param client the client on which this command is executing
     * @param channel channel targeted
     * @throws IllegalArgumentException if null parameters
     */
    public ChannelModeCommand(@Nonnull Client client, @Nonnull String channel) {
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
    public ChannelModeCommand add(boolean add, @Nonnull ChannelMode mode) {
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
    public ChannelModeCommand add(boolean add, @Nonnull ChannelMode mode, @Nonnull String parameter) {
        Sanity.nullCheck(parameter, "Parameter cannot be null");
        return this.addChange(add, mode, parameter);
    }

    /**
     * Adds a mode change.
     *
     * @param add true if adding, false if removing
     * @param mode the mode to be changed
     * @param parameter user whose nick will be sent
     * @return this ModeCommand
     * @throws IllegalArgumentException if mode invalid or either mode or
     * user comes from a different client or parameter is null
     */
    @Nonnull
    public ChannelModeCommand add(boolean add, @Nonnull ChannelUserMode mode, @Nonnull User parameter) {
        Sanity.nullCheck(parameter, "User cannot be null");
        Sanity.truthiness(parameter.getClient() == this.getClient(), "User comes from a different Client");
        return this.addChange(add, mode, parameter.getNick());
    }

    @Nonnull
    private synchronized ChannelModeCommand addChange(boolean add, @Nonnull ChannelMode mode, @Nullable String parameter) {
        Sanity.nullCheck(mode, "Mode cannot be null");
        Sanity.truthiness(mode.getClient() == this.getClient(), "Mode comes from a different Client");
        if (parameter != null) {
            Sanity.safeMessageCheck(parameter, "Parameter");
            this.changes.add(new ModeStatus<>(add, mode, parameter));
        } else {
            this.changes.add(new ModeStatus<>(add, mode));
        }
        return this;
    }

    @Override
    public synchronized void execute() {
        if (this.changes.isEmpty()) {
            this.getClient().sendRawLine("MODE " + this.getChannel());
            return;
        }
        int parameterModesPerLine = -1;
        Optional<ISupportParameter.Modes> modes = this.getClient().getServerInfo().getISupportParameter("MODES", ISupportParameter.Modes.class);
        if (modes.isPresent()) {
            parameterModesPerLine = modes.get().getInteger();
        }
        if (parameterModesPerLine < 1) {
            parameterModesPerLine = PARAMETER_MODES_PER_LINE;
        }
        List<ModeStatus<ChannelMode>> queue = new ArrayList<>();
        int currentParamModes = 0;
        for (ModeStatus<ChannelMode> modeChange : this.changes) {
            if (modeChange.getParameter().isPresent()) {
                if (++currentParamModes > parameterModesPerLine) {
                    this.send(queue);
                    currentParamModes = 0;
                }
            }
            queue.add(modeChange);
        }
        if (!queue.isEmpty()) {
            this.send(queue);
        }
    }

    private void send(@Nonnull List<ModeStatus<ChannelMode>> queue) {
        this.getClient().sendRawLine("MODE " + this.getChannel() + ' ' + ModeStatusList.of(new ArrayList<>(queue)).getStatusString());
        queue.clear();
    }

    @Nonnull
    @Override
    protected ToStringer toStringer() {
        return super.toStringer().add("changes", this.changes);
    }
}
