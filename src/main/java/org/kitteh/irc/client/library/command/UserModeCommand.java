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
package org.kitteh.irc.client.library.command;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.mode.DefaultModeStatus;
import org.kitteh.irc.client.library.defaults.element.mode.DefaultModeStatusList;
import org.kitteh.irc.client.library.element.mode.ModeStatus;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.ArrayList;
import java.util.List;

/**
 * Commands a la User MODE, without modes added will just query self.
 */
public class UserModeCommand extends Command<UserModeCommand> {
    private final List<ModeStatus<UserMode>> changes = new ArrayList<>();

    /**
     * Constructs a MODE command for a given user.
     *
     * @param client the client on which this command is executing
     * @throws IllegalArgumentException if null parameters
     */
    public UserModeCommand(@NonNull Client client) {
        super(client);
    }

    /**
     * Adds a mode change without a parameter.
     *
     * @param action adding or removing
     * @param mode the mode to be changed
     * @return this ModeCommand
     * @throws IllegalArgumentException if mode invalid
     */
    public @NonNull UserModeCommand add(ModeStatus.Action action, @NonNull UserMode mode) {
        return this.addChange(action, mode, null);
    }

    /**
     * Adds a mode change.
     *
     * @param action adding or removing
     * @param mode the mode to be changed
     * @param parameter mode parameter
     * @return this ModeCommand
     * @throws IllegalArgumentException if mode invalid comes from a
     * different client or parameter is null
     */
    public @NonNull UserModeCommand add(ModeStatus.Action action, @NonNull UserMode mode, @NonNull String parameter) {
        return this.addChange(action, mode, Sanity.nullCheck(parameter, "Parameter"));
    }

    private synchronized @NonNull UserModeCommand addChange(ModeStatus.Action action, @NonNull UserMode mode, @Nullable String parameter) {
        Sanity.nullCheck(mode, "Mode");
        Sanity.truthiness(mode.getClient() == this.getClient(), "Mode comes from a different Client");
        if (parameter != null) {
            Sanity.safeMessageCheck(parameter, "Parameter");
            this.changes.add(new DefaultModeStatus<>(action, mode, parameter));
        } else {
            this.changes.add(new DefaultModeStatus<>(action, mode));
        }
        return this;
    }

    @Override
    public synchronized void execute() {
        if (this.changes.isEmpty()) {
            this.sendCommandLine("MODE " + this.getClient().getNick());
            return;
        }
        this.sendCommandLine("MODE " + this.getClient().getNick() + ' ' + DefaultModeStatusList.of(new ArrayList<>(this.changes)).getAsString());
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("changes", this.changes);
    }
}
