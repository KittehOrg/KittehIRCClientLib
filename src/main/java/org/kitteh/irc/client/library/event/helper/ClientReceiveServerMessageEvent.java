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
package org.kitteh.irc.client.library.event.helper;

import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.element.ServerMessage;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * An event for a raw server message from the server.
 */
public interface ClientReceiveServerMessageEvent extends ActorEvent<Actor> {
    /**
     * Gets the command sent.
     *
     * @return command, upper-case
     */
    @Nonnull
    String getCommand();

    /**
     * Gets the message tags.
     *
     * @return message tags
     */
    @Nonnull
    List<MessageTag> getMessageTags();

    /**
     * Gets the original message received by the server.
     *
     * @return unprocessed, original message
     */
    @Nonnull
    String getOriginalMessage();

    /**
     * Gets the subsequent parameters after the command.
     *
     * @return arguments
     */
    @Nonnull
    List<String> getParameters();

    /**
     * Gets the server message received.
     *
     * @return the server message
     */
    @Nonnull
    ServerMessage getServerMessage();
}
