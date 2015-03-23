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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

/**
 * TODO javaducks
 */
public class ModeCommand extends Command {
    private class ModeChange {
        private Boolean add;
        private char mode;
        private String parameter;

        private ModeChange(boolean add, char mode, String parameter) {
            this.add = add;
            this.mode = mode;
            this.parameter = parameter;
        }

        private Boolean getAdd() {
            return this.add;
        }

        private char getMode() {
            return this.mode;
        }

        private String getParameter() {
            return this.parameter;
        }
    }

    private static final Pattern MASK_PATTERN = Pattern.compile("([^!@]+)!([^!@]+)@([^!@]+)");

    private final List<ModeChange> changes = new ArrayList<>();
    private final String channel;

    public ModeCommand(Client client, Channel channel) {
        super(client);
        Sanity.nullCheck(channel, "Channel cannot be null");
        this.channel = channel.getName();
    }

    public ModeCommand(Client client, String channel) {
        super(client);
        Sanity.nullCheck(channel, "Channel cannot be null");
        Sanity.nullCheck(client.getChannel(channel), "Invalid channel name '" + channel + "'");
        this.channel = channel;
    }

    /**
     * TODO javaducks
     *
     * @param add quack
     * @param mode quack
     */
    public void addModeChange(boolean add, char mode) {
        this.addModeChange(add, mode, null);
    }

    public void addModeChange(boolean add, ChannelUserMode mode, String parameter) {
        this.addModeChange(add, mode.getMode(), parameter);
    }

    public void addModeChange(boolean add, ChannelUserMode mode, User parameter) {
        this.addModeChange(add, mode.getMode(), parameter.getNick());
    }

    /**
     * TODO javaducks
     *
     * @param add quack
     * @param mode QUACK
     * @param parameter QUACK!
     */
    public synchronized void addModeChange(boolean add, char mode, String parameter) {
        ChannelModeType channelModeType = this.getChannelModeType(mode);
        Sanity.safeMessageCheck(parameter);
        boolean paramRequired = add ? channelModeType.isParameterRequiredOnSetting() : channelModeType.isParameterRequiredOnRemoval();
        Sanity.truthiness(paramRequired && parameter == null, "Provided mode '" + mode + "' without parameter when one is required.");
        Sanity.truthiness(!paramRequired && parameter != null, "Provided mode '" + mode + "' with parameter when one is not required.");
        if (channelModeType == ChannelModeType.A_MASK) {
            Sanity.truthiness(MASK_PATTERN.matcher(parameter).matches(), "Provided mode `" + mode + "' requires a mask parameter.");
        }
        this.changes.add(new ModeChange(add, mode, parameter));
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
    }

    private void send(Queue<ModeChange> queue) {
        StringBuilder modes = new StringBuilder();
        StringBuilder parameters = new StringBuilder();
        ModeChange change;
        Boolean add = null;
        while ((change = queue.poll()) != null) {
            if (add != change.getAdd()) {
                add = change.getAdd();
                modes.append(add ? '+' : '-');
            }
            modes.append(change.getMode());
            if (change.getParameter() != null) {
                parameters.append(' ').append(change.getParameter());
            }
        }
        this.getClient().sendRawLine("MODE " + this.channel + " " + modes.toString() + parameters.toString());
    }

    private ChannelModeType getChannelModeType(char mode) {
        ChannelModeType type = this.getClient().getServerInfo().getChannelModes().get(mode);
        if (type != null) {
            return type;
        }
        for (ChannelUserMode prefix : this.getClient().getServerInfo().getChannelUserModes()) {
            if (prefix.getPrefix() == mode) {
                return ChannelModeType.B_PARAMETER_ALWAYS;
            }
        }
        throw new IllegalArgumentException("Invalid mode '" + mode + "'");
    }
}