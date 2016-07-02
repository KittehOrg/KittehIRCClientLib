/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.element.mode;

import org.kitteh.irc.client.library.Client;

import javax.annotation.Nonnull;

/**
 * A channel mode.
 */
public interface ChannelMode {
    /**
     * Channel mode types.
     */
    enum Type {
        /**
         * Always has parameter, which is a mask.
         */
        A_MASK(true, true),
        /**
         * Always has parameter.
         */
        B_PARAMETER_ALWAYS(true, true),
        /**
         * Has parameter when setting.
         */
        C_PARAMETER_ON_SET(true, false),
        /**
         * Never has parameters.
         */
        D_PARAMETER_NEVER(false, false);

        private final boolean parameterRequiredOnRemoval;
        private final boolean parameterRequiredOnSetting;

        Type(boolean parameterRequiredOnSetting, boolean parameterRequiredOnRemoval) {
            this.parameterRequiredOnRemoval = parameterRequiredOnRemoval;
            this.parameterRequiredOnSetting = parameterRequiredOnSetting;
        }

        /**
         * Gets if a parameter is required when removing the mode.
         *
         * @return true if a parameter is required on removal
         */
        public boolean isParameterRequiredOnRemoval() {
            return this.parameterRequiredOnRemoval;
        }

        /**
         * Gets if a parameter is required when adding the mode.
         *
         * @return true if a parameter is required on addition
         */
        public boolean isParameterRequiredOnSetting() {
            return this.parameterRequiredOnSetting;
        }
    }

    /**
     * Gets the mode character.
     *
     * @return mode character
     */
    char getChar();

    /**
     * Gets the client this mode is for.
     *
     * @return client
     */
    @Nonnull
    Client getClient();

    /**
     * Gets the type this mode is.
     *
     * @return type
     */
    @Nonnull
    Type getType();
}
