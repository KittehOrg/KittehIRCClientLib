/*
 * * Copyright (C) 2013-2018 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.event.abstractbase;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.helper.ClientReceiveServerMessageEvent;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for events involving a server message. Use the helper
 * events if you want to listen to such an event.
 */
public class ClientReceiveServerMessageEventBase extends ActorEventBase<Actor> implements ClientReceiveServerMessageEvent {
    private final List<String> parameters;
    private final String command;
    private final ServerMessage message;

    /**
     * Constructs the event.
     *
     * @param client client
     * @param serverMessage server message
     * @param server server
     * @param command command
     * @param parameters parameters
     */
    public ClientReceiveServerMessageEventBase(@Nonnull Client client, @Nonnull ServerMessage serverMessage, @Nonnull Actor server, @Nonnull String command, @Nonnull List<String> parameters) {
        super(client, Collections.singletonList(Sanity.nullCheck(serverMessage, "Server message cannot be null")), server);
        this.parameters = Collections.unmodifiableList(new ArrayList<>(Sanity.nullCheck(parameters, "Parameters cannot be null")));
        this.message = serverMessage;
        this.command = Sanity.nullCheck(command, "Command cannot be null");
    }

    /**
     * Gets the subsequent arguments after the command.
     *
     * @return arguments
     */
    @Nonnull
    @Override
    public List<String> getParameters() {
        return this.parameters;
    }

    @Nonnull
    @Override
    public String getCommand() {
        return this.command;
    }

    /**
     * Gets the message tags.
     *
     * @return message tags
     */
    @Nonnull
    @Override
    public List<MessageTag> getMessageTags() {
        return this.message.getTags();
    }

    /**
     * Gets the original message received by the server.
     *
     * @return unprocessed, original message
     */
    @Nonnull
    @Override
    public String getOriginalMessage() {
        return this.message.getMessage();
    }

    @Nonnull
    @Override
    public ServerMessage getServerMessage() {
        return this.message;
    }

    @Override
    @Nonnull
    protected ToStringer toStringer() {
        return super.toStringer().add("message", this.message);
    }
}
