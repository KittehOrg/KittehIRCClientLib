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
package org.kitteh.irc.client.library.defaults.element.mode;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.Mode;
import org.kitteh.irc.client.library.element.mode.ModeStatus;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A default list of mode statuses.
 *
 * @param <ModeType> type of modes being listed
 */
public class DefaultModeStatusList<ModeType extends Mode> implements ModeStatusList<ModeType> {
    /**
     * Creates a list from a given string input such as "+o Kittens".
     *
     * @param client client for which this list exists
     * @param string string to parse
     * @return list
     */
    public static @NonNull DefaultModeStatusList<ChannelMode> fromChannel(@NonNull Client client, @NonNull String string) {
        Map<Character, ChannelMode> modes = new HashMap<>();
        client.getServerInfo().getChannelModes().forEach(mode -> modes.put(mode.getChar(), mode));
        client.getServerInfo().getChannelUserModes().forEach(mode -> modes.put(mode.getChar(), mode));
        return DefaultModeStatusList.from(string, modes);
    }

    /**
     * Creates a list from a given string input such as "+iZ".
     *
     * @param client client for which this list exists
     * @param string string to parse
     * @return list
     */
    public static @NonNull DefaultModeStatusList<UserMode> fromUser(@NonNull Client client, @NonNull String string) {
        return DefaultModeStatusList.from(string, client.getServerInfo().getUserModes().stream().collect(Collectors.toMap(UserMode::getChar, Function.identity())));
    }

    private static <ModeType extends Mode> @NonNull DefaultModeStatusList<ModeType> from(@NonNull String string, @NonNull Map<Character, ModeType> modes) {
        Sanity.safeMessageCheck(string, "String");
        List<ModeStatus<ModeType>> list = new ArrayList<>();
        String[] args = string.split(" ");
        int currentArg = -1;
        while (++currentArg < args.length) {
            String changes = args[currentArg];
            if (!((changes.charAt(0) == '+') || (changes.charAt(0) == '-'))) {
                throw new IllegalArgumentException("Mode change does not start with + or -");
            }
            ModeStatus.Action action = null; // Immediately changed because of lines immediately above and the switch below.
            for (char modeChar : changes.toCharArray()) {
                switch (modeChar) {
                    case '+':
                        action = ModeStatus.Action.ADD;
                        break;
                    case '-':
                        action = ModeStatus.Action.REMOVE;
                        break;
                    default:
                        ModeType mode = modes.get(modeChar);
                        if (mode == null) {
                            throw new IllegalArgumentException("Contains non-registered mode: " + modeChar);
                        }
                        String target = null;
                        if ((mode instanceof ChannelMode) && ((mode instanceof ChannelUserMode) || ((action == ModeStatus.Action.ADD) ? ((ChannelMode) mode).getType().isParameterRequiredOnSetting() : ((ChannelMode) mode).getType().isParameterRequiredOnRemoval()))) {
                            target = args[++currentArg];
                        }
                        list.add((target == null) ? new DefaultModeStatus<>(action, mode) : new DefaultModeStatus<>(action, mode, target));
                }
            }
        }
        return DefaultModeStatusList.of(list);
    }

    /**
     * Creates a list of the given statuses.
     *
     * @param statuses statuses
     * @param <ModeType> type of modes being listed
     * @return list
     */
    public static @NonNull <ModeType extends Mode> DefaultModeStatusList<ModeType> of(@NonNull ModeStatus<ModeType>... statuses) {
        Sanity.nullCheck(statuses, "Statuses");
        Sanity.truthiness((statuses.length <= 1) || (Arrays.stream(statuses).map(ModeStatus::getClient).distinct().count() == 1), "Statuses must all be from one client");
        return new DefaultModeStatusList<>(Arrays.asList(statuses));
    }

    /**
     * Creates a list of the given statuses.
     *
     * @param statuses statuses
     * @param <ModeType> type of modes being listed
     * @return list
     */
    public static @NonNull <ModeType extends Mode> DefaultModeStatusList<ModeType> of(@NonNull Collection<ModeStatus<ModeType>> statuses) {
        Sanity.nullCheck(statuses, "Statuses");
        List<ModeStatus<ModeType>> list = new ArrayList<>(statuses);
        Sanity.truthiness((list.size() <= 1) || (list.stream().map(ModeStatus::getClient).distinct().count() == 1), "Statuses must all be from one client");
        return new DefaultModeStatusList<>(list);
    }

    private final List<ModeStatus<ModeType>> statuses;

    private DefaultModeStatusList(List<ModeStatus<ModeType>> statuses) {
        this.statuses = statuses;
    }

    @Override
    public boolean contains(@NonNull ModeType mode) {
        Sanity.nullCheck(mode, "Mode");
        return this.statuses.stream().anyMatch(status -> status.getMode().equals(mode));
    }

    @Override
    public boolean containsMode(char mode) {
        return this.statuses.stream().anyMatch(status -> status.getMode().getChar() == mode);
    }

    @Override
    public @NonNull List<ModeStatus<ModeType>> getByMode(@NonNull ModeType mode) {
        Sanity.nullCheck(mode, "Mode");
        return Collections.unmodifiableList(this.statuses.stream().filter(status -> status.getMode().equals(mode)).collect(Collectors.toList()));
    }

    @Override
    public @NonNull List<ModeStatus<ModeType>> getByMode(char mode) {
        return Collections.unmodifiableList(this.statuses.stream().filter(status -> status.getMode().getChar() == mode).collect(Collectors.toList()));
    }

    @Override
    public @NonNull List<ModeStatus<ModeType>> getAll() {
        return Collections.unmodifiableList(this.statuses);
    }

    @Override
    public @NonNull String getAsString() {
        StringBuilder modes = new StringBuilder(this.statuses.size() * 2);
        StringBuilder parameters = new StringBuilder(100); // Golly, that's arbitrary.
        ModeStatus.Action action = null;
        for (ModeStatus<ModeType> change : this.statuses) {
            if ((action == null) || (action != change.getAction())) {
                action = change.getAction();
                modes.append(action.getChar());
            }
            modes.append(change.getMode().getChar());
            Optional<String> parameter = change.getParameter();
            parameter.ifPresent(s -> parameters.append(' ').append(s));
        }
        return modes.toString() + parameters;
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).add("list", this.statuses).toString();
    }
}
