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
package org.kitteh.irc.client.library.defaults.element.messagetag;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
import java.time.Instant;

public class DefaultMessageTagTime extends MessageTagManager.DefaultMessageTag implements MessageTag.Time {
    @SuppressWarnings("ConstantConditions")
    public static final TriFunction<Client, String, String, DefaultMessageTagTime> FUNCTION = (client, name, value) -> new DefaultMessageTagTime(name, value, Instant.parse(value));

    private final Instant time;

    private DefaultMessageTagTime(@Nonnull String name, @Nonnull String value, @Nonnull Instant time) {
        super(name, value);
        this.time = time;
    }

    @Nonnull
    @Override
    public Instant getTime() {
        return this.time;
    }
}
