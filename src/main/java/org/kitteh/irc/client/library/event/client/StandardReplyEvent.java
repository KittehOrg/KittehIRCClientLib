package org.kitteh.irc.client.library.event.client;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ServerMessageEventBase;

import java.util.Collections;
import java.util.List;

/**
 * A standard reply. https://ircv3.net/specs/extensions/standard-replies
 */
public abstract class StandardReplyEvent extends ServerMessageEventBase {
    /**
     * Types of standard replies.
     */
    public enum Type {
        /**
         * A failure to process a command or an error about the current
         * session.
         */
        FAIL,
        /**
         * Informational.
         */
        NOTE,
        /**
         * Non-fatal feedback.
         */
        WARN
    }

    private final Type type;
    private final String command;
    private final String code;
    private final List<String> context;
    private final String description;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param sourceMessage source message
     * @param type the type
     * @param command the command
     * @param code the code
     * @param context the context
     * @param description the description
     */
    protected StandardReplyEvent(@NonNull Client client, @NonNull ServerMessage sourceMessage, @NonNull Type type, @NonNull String command, @NonNull String code, @NonNull List<String> context, @NonNull String description) {
        super(client, sourceMessage);
        this.type = type;
        this.command = command;
        this.code = code;
        this.context = context;
        this.description = description;
    }

    /**
     * Gets the type of standard reply this is.
     */
    public @NonNull Type getType() {
        return this.type;
    }

    /**
     * Gets the command this message is about, or "*" for no command.
     *
     * @return the command this is about
     */
    public @NonNull String getCommand() {
        return this.command;
    }

    /**
     * Gets the code for this message.
     *
     * @return code
     */
    public @NonNull String getCode() {
        return this.code;
    }

    /**
     * Gets the context, if any, for this message.
     *
     * @return a list of context, or empty if
     */
    public @NonNull List<String> getContext() {
        return Collections.unmodifiableList(this.context);
    }

    public @NonNull String getDescription() {
        return this.description;
    }
}
