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
package org.kitteh.irc.client.library.feature.twitch.event;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.helper.ServerMessageEvent;

import java.util.Optional;

/**
 * Twitch events are single message, and this lets you get their tags.
 */
public interface TwitchSingleMessageEvent extends ServerMessageEvent {
    /**
     * Gets the message.
     *
     * @return message
     */
    default ServerMessage getOriginalMessage() {
        return this.getOriginalMessages().get(0);
    }

    /**
     * Gets the named tag if present
     *
     * @param name tag name
     * @return tag if present
     */
    @NonNull
    default Optional<MessageTag> getTag(@NonNull String name) {
        return this.getOriginalMessage().getTag(name);
    }

    /**
     * Gets the named message tag if present and if of the specified type.
     *
     * @param name message tag name
     * @param clazz message tag type
     * @param <Tag> message tag type
     * @return message tag if present
     */
    @NonNull
    default <Tag extends MessageTag> Optional<Tag> getTag(@NonNull String name, @NonNull Class<Tag> clazz) {
        return this.getOriginalMessage().getTag(name, clazz);
    }
}
