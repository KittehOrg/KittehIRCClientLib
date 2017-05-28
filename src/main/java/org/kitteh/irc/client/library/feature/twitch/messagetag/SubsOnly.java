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
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Indicates chatting is for subscribers only.
 */
public class SubsOnly extends MessageTagManager.DefaultMessageTag {
    /**
     * Name of this message tag.
     */
    public static final String NAME = "subs-only";

    /**
     * Function to create this message tag.
     */
    public static final TriFunction<Client, String, Optional<String>, SubsOnly> FUNCTION = (client, name, value) -> new SubsOnly(name, value);

    /**
     * Known subs-only states.
     */
    public static final class KnownValues {
        private KnownValues() {
        }

        /**
         * Disabled.
         */
        public static final String DISABLED = "0";

        /**
         * Enabled.
         */
        public static final String ENABLED = "1";
    }

    private SubsOnly(@Nonnull String name, @Nonnull Optional<String> value) {
        super(name, value);
    }
}
