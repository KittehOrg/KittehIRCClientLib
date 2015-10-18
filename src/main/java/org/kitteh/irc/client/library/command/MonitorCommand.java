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
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
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
    public MonitorCommand(@Nonnull Client client) {
        super(client);
    }

    /**
     * Sets the action for the command.
     *
     * @param action action to take
     * @return this command
     */
    @Nonnull
    private MonitorCommand action(@Nonnull Action action) {
        this.action = action;
        return this;
    }

    /**
     * Sets the target(s) for the command, assuming {@link Action#ADD_TARGET}
     * or {@link Action#REMOVE_TARGET}.
     *
     * @param targets targets to add/remove
     * @return this command
     */
    @Nonnull
    public MonitorCommand target(@Nonnull String... targets) {
        Sanity.nullCheck(targets, "Targets cannot be null");
        HashSet<String> targetSet = new HashSet<>();
        for (String target : targets) {
            Sanity.safeMessageCheck(target, "target");
            targetSet.add(target);
        }
        this.targets = targetSet;
        return this;
    }

    /**
     * Sets the target(s) for the command, assuming {@link Action#ADD_TARGET}
     * or {@link Action#REMOVE_TARGET}.
     *
     * @param targets targets to add/remove
     * @return this command
     */
    @Nonnull
    public MonitorCommand target(@Nonnull Collection<String> targets) {
        Sanity.nullCheck(targets, "Targets cannot be null");
        HashSet<String> targetSet = new HashSet<>();
        for (String target : targets) {
            Sanity.safeMessageCheck(target, "target");
            targetSet.add(target);
        }
        this.targets = targetSet;
        return this;
    }

    @Override
    public void execute() {
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

    private void monitorCommand(@Nonnull Action action) {
        this.getClient().sendRawLine("MONITOR " + action.getCharacter());
    }

    private void monitorCommand(@Nonnull Action action, @Nonnull String targets) {
        this.getClient().sendRawLine("MONITOR " + action.getCharacter() + ' ' + targets);
    }
}
