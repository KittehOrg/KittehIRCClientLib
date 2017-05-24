/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.element;

import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * Represents a message sent by the server.
 */
public interface ServerMessage {
    /**
     * Represents a message with a String command.
     */
    interface StringCommandServerMessage extends ServerMessage {
        /**
         * Gets this message's command.
         *
         * @return the command present in this message
         */
        @Nonnull
        String getCommand();
    }

    /**
     * Represents a message with a numeric command.
     */
    interface NumericCommandServerMessage extends ServerMessage {
        /**
         * Gets this message's command.
         *
         * @return the command present in this message
         */
        int getCommand();
    }

    /**
     * Gets the full content of the line sent by the server, minus linebreak
     * characters \r and \n.
     *
     * @return full message content
     */
    @Nonnull
    String getMessage();

    /**
     * Gets the processed message tags, if any, contained in the message.
     *
     * @return message tags or empty if none sent.
     */
    @Nonnull
    List<MessageTag> getTags();

    /**
     * Gets the named tag if present
     *
     * @param name tag name
     * @return tag if present
     */
    default Optional<MessageTag> getTag(@Nonnull String name) {
        Sanity.nullCheck(name, "Name cannot be null");
        return this.getTags().stream().filter(tag -> tag.getName().equals(name)).findAny();
    }

    /**
     * Gets the named message tag if present and if of the specified type.
     *
     * @param name message tag name
     * @param clazz message tag type
     * @param <Tag> message tag type
     * @return message tag if present
     */
    @Nonnull
    default <Tag extends MessageTag> Optional<Tag> getTag(@Nonnull String name, @Nonnull Class<Tag> clazz) {
        Sanity.nullCheck(name, "Name cannot be null");
        Sanity.nullCheck(clazz, "Class cannot be null");
        return this.getTags().stream()
                .filter(tag -> tag.getName().equals(name))
                .filter(clazz::isInstance)
                .map(tag -> (Tag) tag)
                .findAny();
    }
}
