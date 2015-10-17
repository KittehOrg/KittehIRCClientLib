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
