/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Sends a MONITOR request to the server.
 */
public class MonitorCommand extends Command {
    /**
     * Describes an action to take with the MONITOR command.
     */
    public enum Action {
        /**
         * Add specified target(s).
         */
        ADD_TARGET('+'),
        /**
         * Clear the entire target list.
         */
        CLEAR_ALL_TARGETS('C'),
        /**
         * List all targeted nicknames.
         */
        LIST_TARGETS('L'),
        /**
         * Remove specified target(s).
         */
        REMOVE_TARGET('-'),
        /**
         * Output status of all targets.
         */
        STATUS_OUTPUT_ALL('S');

        private final char character;

        Action(char character) {
            this.character = character;
        }

        /**
         * Gets the character for this action.
         *
         * @return the action's character
         */
        public char getCharacter() {
            return this.character;
        }
    }

    private Action action;
    private Set<String> targets;

    /**
     * Constructs the command.
     *
     * @param client the client
     * @throws IllegalArgumentException if client is null
     */
    public MonitorCommand(@NonNull Client client) {
        super(client);
    }

    /**
     * Sets the action for the command.
     *
     * @param action action to take
     * @return this command
     * @throws IllegalArgumentException for null action
     */
    public @NonNull MonitorCommand action(@NonNull Action action) {
        this.action = Sanity.nullCheck(action, "Action cannot be null");
        return this;
    }

    /**
     * Sets the target(s) for the command, assuming {@link Action#ADD_TARGET}
     * or {@link Action#REMOVE_TARGET}.
     *
     * @param targets targets to add/remove
     * @return this command
     * @throws IllegalArgumentException for \n, \r, \0, or comma in target or
     * null target
     */
    public @NonNull MonitorCommand target(@NonNull String... targets) {
        return this.target(Arrays.asList(Sanity.nullCheck(targets, "Targets cannot be null")));
    }

    /**
     * Sets the target(s) for the command, assuming {@link Action#ADD_TARGET}
     * or {@link Action#REMOVE_TARGET}.
     *
     * @param targets targets to add/remove
     * @return this command
     * @throws IllegalArgumentException for \n, \r, \0, or comma in target or
     * null target
     */
    public synchronized @NonNull MonitorCommand target(@NonNull Collection<String> targets) {
        Sanity.nullCheck(targets, "Targets cannot be null");
        Set<String> targetSet = new LinkedHashSet<>();
        for (String target : targets) {
            Sanity.safeMessageCheck(target, "target");
            Sanity.truthiness(target.indexOf(',') == -1, "Target cannot contain a comma");
            targetSet.add(target);
        }
        this.targets = targetSet;
        return this;
    }

    @Override
    public synchronized void execute() {
        if (this.action == null) {
            throw new IllegalStateException("Action not defined");
        }
        if ((this.action == Action.ADD_TARGET) || (this.action == Action.REMOVE_TARGET)) {
            if ((this.targets == null) || this.targets.isEmpty()) {
                throw new IllegalStateException("Target(s) not defined");
            }
            StringBuilder builder = new StringBuilder(200);
            for (String request : this.targets) {
                if ((builder.length() > 0) && ((request.length() + builder.length()) > 200)) {
                    this.monitorCommand(this.action, builder.toString());
                    builder.setLength(0);
                }
                if (builder.length() > 0) {
                    builder.append(',');
                }
                builder.append(request);
            }
            this.monitorCommand(this.action, builder.toString());
        } else {
            this.monitorCommand(this.action);
        }
    }

    private void monitorCommand(@NonNull Action action) {
        this.getClient().sendRawLine("MONITOR " + action.getCharacter());
    }

    private void monitorCommand(@NonNull Action action, @NonNull String targets) {
        this.getClient().sendRawLine("MONITOR " + action.getCharacter() + ' ' + targets);
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("action", this.action).add("targets", this.targets);
    }
}
