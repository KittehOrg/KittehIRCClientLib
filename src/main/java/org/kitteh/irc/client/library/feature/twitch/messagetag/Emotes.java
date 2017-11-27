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
package org.kitteh.irc.client.library.feature.twitch.messagetag;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.ToStringer;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * I'm so emotional right now.
 */
public class Emotes extends MessageTagManager.DefaultMessageTag {
    /**
     * Name of this message tag.
     */
    public static final String NAME = "emotes";

    /**
     * Function to create this message tag.
     */
    public static final TriFunction<Client, String, String, Emotes> FUNCTION = (client, name, value) -> new Emotes(name, value);

    /**
     * One emote.
     */
    public class Emote {
        private final int id;
        private final int firstIndex;
        private final int lastIndex;

        private Emote(int id, int firstIndex, int lastIndex) {
            this.id = id;
            this.firstIndex = firstIndex;
            this.lastIndex = lastIndex;
        }

        /**
         * Gets the emote ID.
         *
         * @return emote id
         */
        public int getId() {
            return this.id;
        }

        /**
         * Gets the first index of the emote.
         *
         * @return emote starting index
         */
        public int getFirstIndex() {
            return this.firstIndex;
        }

        /**
         * Gets the last index of the emote
         *
         * @return emote ending index
         */
        public int getLastIndex() {
            return this.lastIndex;
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("id", this.id).add("firstIndex", this.firstIndex).add("lastIndex", this.lastIndex).toString();
        }
    }

    private final List<Emote> emotes;

    private Emotes(@Nonnull String name, @Nonnull String value) {
        super(name, value);
        if (value == null) {
            this.emotes = Collections.emptyList();
        } else {
            List<Emote> emotes = new ArrayList<>();
            String[] emotesSplit = value.split("/");
            for (String emoteInfo : emotesSplit) {
                String[] emoteAndIndices = emoteInfo.split(":");
                if (emoteAndIndices.length < 2) {
                    continue;
                }
                int emoteId;
                try {
                    emoteId = Integer.parseInt(emoteAndIndices[0]);
                } catch (NumberFormatException e) {
                    continue;
                }
                String[] indicesSplit = emoteAndIndices[1].split(",");
                for (String indices : indicesSplit) {
                    String[] split = indices.split("-");
                    if (split.length < 2) {
                        continue;
                    }
                    int firstIndex, lastIndex;
                    try {
                        firstIndex = Integer.parseInt(split[0]);
                        lastIndex = Integer.parseInt(split[1]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    emotes.add(new Emote(emoteId, firstIndex, lastIndex));
                }
            }
            this.emotes = Collections.unmodifiableList(emotes);
        }
    }

    /**
     * Gets emotes.
     *
     * @return list of emotes
     */
    @Nonnull
    public List<Emote> getEmotes() {
        return this.emotes;
    }

    @Nonnull
    @Override
    protected ToStringer toStringer() {
        return super.toStringer().add("emotes", this.emotes);
    }
}
