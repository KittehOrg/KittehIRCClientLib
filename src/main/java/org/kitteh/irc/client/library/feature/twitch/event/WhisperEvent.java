package org.kitteh.irc.client.library.feature.twitch.event;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Fires when a whisper is received via twitch.
 */
public class WhisperEvent extends PrivateMessageEvent implements TwitchSingleMessageEvent {
    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param originalMessages original messages
     * @param sender who sent it
     * @param target who received it
     * @param message message sent
     */
    public WhisperEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull User sender, @Nonnull String target, @Nonnull String message) {
        super(client, originalMessages, sender, target, message);
    }

    @Override
    public void sendReply(@Nonnull String message) {
        this.getClient().sendMessage("#jtv", "/w " + this.getActor().getNick() + " " + message);
    }
}
