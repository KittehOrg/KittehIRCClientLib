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
package org.kitteh.irc.client.library.element.mode;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A list of mode statuses.
 */
public class ModeStatusList<ModeType extends Mode> {
    /**
     * Creates a list from a given string input such as "+o Kittens".
     *
     * @param client client for which this list exists
     * @param string string to parse
     * @return list
     */
    @Nonnull
    public static ModeStatusList<ChannelMode> fromChannel(@Nonnull Client client, @Nonnull String string) {
        Map<Character, ChannelMode> modes = new HashMap<>();
        client.getServerInfo().getChannelModes().forEach(mode -> modes.put(mode.getChar(), mode));
        client.getServerInfo().getChannelUserModes().forEach(mode -> modes.put(mode.getChar(), mode));
        return from(client, string, modes);
    }

    /**
     * Creates a list from a given string input such as "+iZ".
     *
     * @param client client for which this list exists
     * @param string string to parse
     * @return list
     */
    @Nonnull
    public static ModeStatusList<UserMode> fromUser(@Nonnull Client client, @Nonnull String string) {
        return from(client, string, client.getServerInfo().getUserModes().stream().collect(Collectors.toMap(UserMode::getChar, Function.identity())));
    }

    @Nonnull
    private static <ModeType extends Mode> ModeStatusList<ModeType> from(@Nonnull Client client, @Nonnull String string, @Nonnull Map<Character, ModeType> modes) {
        Sanity.nullCheck(client, "Client cannot be null");
        Sanity.safeMessageCheck(string, "String");
        List<ModeStatus<ModeType>> list = new ArrayList<>();
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
                        ModeType mode = modes.get(modeChar);
                        if (mode == null) {
                            throw new IllegalArgumentException("Contains non-registered mode: " + modeChar);
                        }
                        String target = null;
                        if ((mode instanceof ChannelMode) && ((mode instanceof ChannelUserMode) || (add ? ((ChannelMode) mode).getType().isParameterRequiredOnSetting() : ((ChannelMode) mode).getType().isParameterRequiredOnRemoval()))) {
                            target = args[++currentArg];
                        }
                        list.add((target == null) ? new ModeStatus<>(add, mode) : new ModeStatus<>(add, mode, target));
                }
            }
        }
        return ModeStatusList.of(list);
    }

    /**
     * Creates a list of the given statuses.
     *
     * @param statuses statuses
     * @return list
     */
    @Nonnull
    public static <ModeType extends Mode> ModeStatusList<ModeType> of(@Nonnull ModeStatus<ModeType>... statuses) {
        Sanity.nullCheck(statuses, "Statuses cannot be null");
        Sanity.truthiness((statuses.length <= 1) || (Arrays.stream(statuses).map(ModeStatus::getClient).distinct().count() == 1), "Statuses must all be from one client");
        return new ModeStatusList<>(Arrays.asList(statuses));
    }

    /**
     * Creates a list of the given statuses.
     *
     * @param statuses statuses
     * @return list
     */
    @Nonnull
    public static <ModeType extends Mode> ModeStatusList<ModeType> of(@Nonnull Collection<ModeStatus<ModeType>> statuses) {
        Sanity.nullCheck(statuses, "Statuses cannot be null");
        List<ModeStatus<ModeType>> list = new ArrayList<>(statuses);
        Sanity.truthiness((list.size() <= 1) || (list.stream().map(ModeStatus::getClient).distinct().count() == 1), "Statuses must all be from one client");
        return new ModeStatusList<>(list);
    }

    private final List<ModeStatus<ModeType>> statuses;

    private ModeStatusList(List<ModeStatus<ModeType>> statuses) {
        this.statuses = statuses;
    }

    /**
     * Gets if the given mode is present in the list.
     *
     * @param mode mode to check
     * @return true if present at least once
     */
    public boolean containsMode(@Nonnull ModeType mode) {
        Sanity.nullCheck(mode, "Mode cannot be null");
        return this.statuses.stream().filter(status -> status.getMode().equals(mode)).count() > 0;
    }

    /**
     * Gets all mode statuses of a given mode.
     *
     * @param mode mode to check
     * @return all matching modes or empty if none match
     */
    @Nonnull
    public List<ModeStatus<ModeType>> getStatusByMode(@Nonnull ModeType mode) {
        Sanity.nullCheck(mode, "Mode cannot be null");
        return Collections.unmodifiableList(this.statuses.stream().filter(status -> status.getMode().equals(mode)).collect(Collectors.toList()));
    }

    /**
     * Gets the list of statuses.
     *
     * @return status list
     */
    @Nonnull
    public List<ModeStatus<ModeType>> getStatuses() {
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
        for (ModeStatus<ModeType> change : this.statuses) {
            if ((add == null) || (add != change.isSetting())) {
                add = change.isSetting();
                modes.append(add ? '+' : '-');
            }
            modes.append(change.getMode().getChar());
            if (change.getParameter().isPresent()) {
                parameters.append(' ').append(change.getParameter().get());
            }
        }
        return modes.toString() + parameters;
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("list", this.statuses).toString();
    }
}
