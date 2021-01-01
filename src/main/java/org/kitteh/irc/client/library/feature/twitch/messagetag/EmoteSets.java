/*
 * * Copyright (C) 2013-2021 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.feature.twitch.messagetag;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.ToStringer;
import org.kitteh.irc.client.library.util.TriFunction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Message tag for emote sets.
 */
public class EmoteSets extends MessageTagManager.DefaultMessageTag {
    /**
     * Name of this message tag.
     */
    public static final String NAME = "emote-sets";

    /**
     * Function to create this message tag.
     */
    @SuppressWarnings("ConstantConditions")
    public static final TriFunction<Client, String, String, EmoteSets> FUNCTION = (client, name, value) -> new EmoteSets(name, value);

    private final List<Integer> emoteSets;

    private EmoteSets(@NonNull String name, @Nullable String value) {
        super(name, value);
        this.emoteSets = (value == null) ? Collections.emptyList() : Collections.unmodifiableList(Arrays.stream(value.split(",")).map(Integer::valueOf).collect(Collectors.toList()));
    }

    /**
     * Gets the list of emote sets.
     *
     * @return list of integers, at least containing 0.
     */
    public @NonNull List<Integer> getEmoteSets() {
        return this.emoteSets;
    }

    @Override
    public @NonNull ToStringer toStringer() {
        return super.toStringer().add("emoteSets", this.emoteSets);
    }
}
