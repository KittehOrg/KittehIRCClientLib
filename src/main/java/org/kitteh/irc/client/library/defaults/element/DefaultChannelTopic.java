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
package org.kitteh.irc.client.library.defaults.element;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.util.ToStringer;

import java.time.Instant;
import java.util.Optional;

/**
 * Default Channel.Topic implementation.
 */
public class DefaultChannelTopic implements Channel.Topic {
    private final Actor setter;
    private final Instant time;
    private final String topic;

    /**
     * Constructs the topic snapshot.
     *
     * @param time time of topic creation
     * @param topic topic
     * @param setter setter of topic if known
     */
    public DefaultChannelTopic(@Nullable Instant time, @Nullable String topic, @Nullable Actor setter) {
        this.time = time;
        this.topic = topic;
        this.setter = setter;
    }

    @Override
    public @NonNull Optional<Actor> getSetter() {
        return Optional.ofNullable(this.setter);
    }

    @Override
    public @NonNull Optional<Instant> getTime() {
        return Optional.ofNullable(this.time);
    }

    @Override
    public @NonNull Optional<String> getValue() {
        return Optional.ofNullable(this.topic);
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).add("topic", this.topic).add("setter", this.setter).add("time", this.time).toString();
    }
}
