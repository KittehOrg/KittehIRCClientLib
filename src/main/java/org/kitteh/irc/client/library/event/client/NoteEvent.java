package org.kitteh.irc.client.library.event.client;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;

import java.util.List;

/**
 * A NOTE has been received.
 */
public class NoteEvent extends StandardReplyEvent {
    /**
     * Constructs the event.
     *
     * @param client the client
     * @param sourceMessage source message
     * @param command the command
     * @param code the code
     * @param context the context
     * @param description the description
     */
    public NoteEvent(@NonNull Client client, @NonNull ServerMessage sourceMessage, @NonNull String command, @NonNull String code, @NonNull List<String> context, @NonNull String description) {
        super(client, sourceMessage, Type.NOTE, command, code, context, description);
    }
}
