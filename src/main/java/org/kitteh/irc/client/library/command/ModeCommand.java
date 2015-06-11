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

import org.kitteh.irc.client.library.ChannelModeType;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

/**
 * Commands a la MODE.
 */
public class ModeCommand extends ChannelCommand {
    private class ModeChange {
        private final boolean add;
        private final char mode;
        private final String parameter;

        private ModeChange(boolean add, char mode, @Nullable String parameter) {
            this.add = add;
            this.mode = mode;
            this.parameter = parameter;
        }

        private boolean getAdd() {
            return this.add;
        }

        private char getMode() {
            return this.mode;
        }

        @Nullable
        private String getParameter() {
            return this.parameter;
        }
    }

    private static final Pattern MASK_PATTERN = Pattern.compile("([^!@]+)!([^!@]+)@([^!@]+)");

    private final List<ModeChange> changes = new ArrayList<>();

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
     * Adds a mode change for a mode not requiring a parameter.
     *
     * @param add true if adding, false if removing
     * @param mode the mode to be changed
     * @return this ModeCommand
     * @throws IllegalArgumentException if mode invalid or requires parameter
     */
    @Nonnull
    public ModeCommand addModeChange(boolean add, char mode) {
        return this.addModeChange(add, mode, (String) null);
    }

    /**
     * Adds a mode change.
     *
     * @param add true if adding, false if removing
     * @param mode the mode to be changed
     * @param parameter mode parameter or null if one is not needed
     * @return this ModeCommand
     * @throws IllegalArgumentException if mode invalid or mode requires a
     * parameter but one was not provided or mode requires no parameter but
     * one was provided or the mode comes from a different client
     */
    @Nonnull
    public ModeCommand addModeChange(boolean add, @Nonnull ChannelUserMode mode, @Nullable String parameter) {
        Sanity.nullCheck(mode, "Mode cannot be null");
        Sanity.truthiness(mode.getClient() == this.getClient(), "Mode comes from a different Client");
        return this.addModeChange(add, mode.getMode(), parameter);
    }

    /**
     * Adds a mode change.
     *
     * @param add true if adding, false if removing
     * @param mode the mode to be changed
     * @param parameter user whose nick will be sent or null if not needed
     * @return this ModeCommand
     * @throws IllegalArgumentException if mode invalid or mode requires a
     * parameter but one was not provided or mode requires no parameter but
     * one was provided or the mode comes from a different client
     */
    @Nonnull
    public ModeCommand addModeChange(boolean add, char mode, @Nonnull User parameter) {
        Sanity.nullCheck(parameter, "User cannot be null");
        return this.addModeChange(add, mode, parameter.getNick());
    }

    /**
     * Adds a mode change.
     *
     * @param add true if adding, false if removing
     * @param mode the mode to be changed
     * @param parameter user whose nick will be sent or null if not needed
     * @return this ModeCommand
     * @throws IllegalArgumentException if mode invalid or mode requires a
     * parameter but one was not provided or mode requires no parameter but
     * one was provided or the mode comes from a different client
     */
    @Nonnull
    public ModeCommand addModeChange(boolean add, @Nonnull ChannelUserMode mode, @Nonnull User parameter) {
        Sanity.nullCheck(parameter, "User cannot be null");
        return this.addModeChange(add, mode, parameter.getNick());
    }

    /**
     * Adds a mode change for a mode not requiring a parameter.
     *
     * @param add true if adding, false if removing
     * @param mode the mode to be changed
     * @param parameter mode parameter or null if one is not needed
     * @return this ModeCommand
     * @throws IllegalArgumentException if mode invalid or requires parameter
     */
    @Nonnull
    public synchronized ModeCommand addModeChange(boolean add, char mode, @Nullable String parameter) {
        ChannelModeType channelModeType = this.getChannelModeType(mode);
        if (parameter != null) {
            Sanity.safeMessageCheck(parameter);
        }
        if (add ? channelModeType.isParameterRequiredOnSetting() : channelModeType.isParameterRequiredOnRemoval()) {
            Sanity.truthiness(parameter != null, "Provided mode '" + mode + "' without parameter when one is required.");
        } else {
            Sanity.truthiness(parameter == null, "Provided mode '" + mode + "' with parameter when one is not required.");
        }
        if (channelModeType == ChannelModeType.A_MASK) {
            Sanity.truthiness(parameter != null && MASK_PATTERN.matcher(parameter).matches(), "Provided mode `" + mode + "' requires a mask parameter.");
        }
        this.changes.add(new ModeChange(add, mode, parameter));
        return this;
    }

    @Override
    public synchronized void execute() {
        Queue<ModeChange> queue = new LinkedList<>();
        for (ModeChange modeChange : this.changes) {
            queue.offer(modeChange);
            if (queue.size() == 3) {
                this.send(queue);
            }
        }
        if (!queue.isEmpty()) {
            this.send(queue);
        }
    }

    @Nonnull
    private ChannelModeType getChannelModeType(char mode) {
        ChannelModeType type = this.getClient().getServerInfo().getChannelModes().get(mode);
        if (type != null) {
            return type;
        }
        for (ChannelUserMode prefix : this.getClient().getServerInfo().getChannelUserModes()) {
            if (prefix.getMode() == mode) {
                return ChannelModeType.B_PARAMETER_ALWAYS;
            }
        }
        throw new IllegalArgumentException("Invalid mode '" + mode + "'");
    }

    private void send(@Nonnull Queue<ModeChange> queue) {
        StringBuilder modes = new StringBuilder();
        StringBuilder parameters = new StringBuilder();
        ModeChange change;
        Boolean add = null;
        while ((change = queue.poll()) != null) {
            if (add == null || add != change.getAdd()) {
                add = change.getAdd();
                modes.append(add ? '+' : '-');
            }
            modes.append(change.getMode());
            if (change.getParameter() != null) {
                parameters.append(' ').append(change.getParameter());
            }
        }
        this.getClient().sendRawLine("MODE " + this.getChannel() + " " + modes.toString() + parameters.toString());
    }
}