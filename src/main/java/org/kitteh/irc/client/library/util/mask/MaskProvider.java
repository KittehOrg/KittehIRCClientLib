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
package org.kitteh.irc.client.library.util.mask;

import javax.annotation.Nonnull;

/**
 * A mask provider.
 */
public interface MaskProvider {
    /**
     * A default implementation of a mask provider.
     */
    class DefaultMaskProvider implements MaskProvider {
        @Nonnull
        @Override
        public Mask get(@Nonnull final String string) {
            return this.getAsString(string);
        }

        @Nonnull
        @Override
        public Mask.AsString getAsString(@Nonnull final String string) {
            return NameMask.fromString(string);
        }
    }

    /**
     * Resolves a mask from a string.
     *
     * @param string the string
     * @return the mask
     */
    @Nonnull
    Mask get(@Nonnull final String string);

    /**
     * Resolves a mask from a string.
     *
     * @param string the string
     * @return the mask
     */
    @Nonnull
    Mask.AsString getAsString(@Nonnull final String string);
}
