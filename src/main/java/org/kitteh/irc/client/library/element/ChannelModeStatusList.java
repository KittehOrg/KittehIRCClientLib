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
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A list of channel mode statuses.
 */
public class ChannelModeStatusList {
    /**
     * Creates a list from a given string input such as "+o Kittens".
     *
     * @param client client for which this list exists
     * @param string string to parse
     * @return list
     */
    public static ChannelModeStatusList from(@Nonnull Client client, @Nonnull String string) {
        Sanity.nullCheck(client, "Client cannot be null");
        Sanity.nullCheck(client, "String cannot be null");
        Sanity.safeMessageCheck(string, "string");
        Map<Character, ChannelMode> modes = new HashMap<>();
        client.getServerInfo().getChannelModes().forEach(mode -> modes.put(mode.getMode(), mode));
        client.getServerInfo().getChannelUserModes().forEach(mode -> modes.put(mode.getMode(), mode));
        List<ChannelModeStatus> list = new LinkedList<>();
        String[] args = string.split(" ");
        int currentArg = -1;
        while (++currentArg < args.length) {
            String changes = args[currentArg];
            if (!((changes.charAt(0) == '+') || (changes.charAt(0) == '-'))) {
                throw new IllegalArgumentException("Mode change does not start with + or -");
            }
            boolean add = true;
            for (char modeChar : changes.toCharArray()) {
                switch (modeChar) {
                    case '+':
                        add = true;
                        break;
                    case '-':
                        add = false;
                        break;
                    default:
                        ChannelMode mode = modes.get(modeChar);
                        if (mode == null) {
                            throw new IllegalArgumentException("Contains non-registered mode");
                        }
                        String target = null;
                        if ((mode instanceof ChannelUserMode) || (add ? mode.getType().isParameterRequiredOnSetting() : mode.getType().isParameterRequiredOnRemoval())) {
                            target = args[++currentArg];
                        }
                        list.add(new ChannelModeStatus(add, mode, target));
                }
            }
        }
        return ChannelModeStatusList.of(list);
    }

    /**
     * Creates a list of the given statuses.
     *
     * @param statuses statuses
     * @return list
     */
    public static ChannelModeStatusList of(@Nonnull ChannelModeStatus... statuses) {
        Sanity.nullCheck(statuses, "Statuses cannot be null");
        Sanity.truthiness(Arrays.stream(statuses).map(ChannelModeStatus::getClient).distinct().count() == 1, "Statuses must all be from one client");
        return new ChannelModeStatusList(Arrays.asList(statuses));
    }

    /**
     * Creates a list of the given statuses.
     *
     * @param statuses statuses
     * @return list
     */
    public static ChannelModeStatusList of(@Nonnull Collection<ChannelModeStatus> statuses) {
        Sanity.nullCheck(statuses, "Statuses cannot be null");
        List<ChannelModeStatus> list = new ArrayList<>(statuses);
        //Sanity.truthiness(statuses.stream().map(ChannelModeStatus::getClient).distinct().count() == 1, "Statuses must all be from one client");
        return new ChannelModeStatusList(list);
    }

    private final List<ChannelModeStatus> statuses;

    private ChannelModeStatusList(List<ChannelModeStatus> statuses) {
        this.statuses = statuses;
    }

    /**
     * Gets the list of statuses.
     *
     * @return status list
     */
    @Nonnull
    public List<ChannelModeStatus> getStatuses() {
        return Collections.unmodifiableList(this.statuses);
    }

    /**
     * Gets the statuses in a convenient String format.
     *
     * @return string of modes
     */
    @Nonnull
    public String getStatusString() {
        StringBuilder modes = new StringBuilder(this.statuses.size() * 2);
        StringBuilder parameters = new StringBuilder(100); // Golly, that's arbitrary.
        Boolean add = null;
        for (ChannelModeStatus change : this.statuses) {
            if ((add == null) || (add != change.isSetting())) {
                add = change.isSetting();
                modes.append(add ? '+' : '-');
            }
            modes.append(change.getMode().getMode());
            if (change.getParameter() != null) {
                parameters.append(' ').append(change.getParameter());
            }
        }
        return modes.toString() + parameters;
    }
}