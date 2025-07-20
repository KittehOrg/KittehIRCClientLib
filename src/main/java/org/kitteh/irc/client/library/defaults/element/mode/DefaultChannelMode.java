/*
 * * Copyright (C) 2013-2025 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.defaults.element.mode;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.Objects;

/**
 * Default implementation of {@link ChannelMode}.
 */
public class DefaultChannelMode extends DefaultModeBase implements ChannelMode {
    private final Type type;

    /**
     * Constructs this object.
     *
     * @param client client for which this exists
     * @param mode mode
     * @param type mode type
     */
    public DefaultChannelMode(@NonNull Client client, char mode, @NonNull Type type) {
        super(client, mode);
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultChannelMode)) {
            return false;
        }
        DefaultChannelMode other = (DefaultChannelMode) o;
        return (other.getType() == this.getType()) && (other.getClient().equals(this.getClient())) && (other.getChar() == this.getChar());
    }

    @Override
    public @NonNull Type getType() {
        return this.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getClient(), this.getChar(), this.type);
    }

    @Override
    public @NonNull ToStringer toStringer() {
        return super.toStringer().add("type", this.type);
    }
}
