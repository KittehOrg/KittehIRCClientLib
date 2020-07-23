/*
 * * Copyright (C) 2013-2020 Matt Baxter https://kitteh.org
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.element.ClientLinked;

import java.util.Optional;

/**
 * A particular status of a mode.
 */
public interface ModeStatus<ModeType extends Mode> extends ClientLinked {
    /**
     * Describes the mode status.
     */
    enum Action {
        /**
         * This mode is being added.
         */
        ADD('+'),
        /**
         * This mode is being removed.
         */
        REMOVE('-');

        private final char c;

        Action(char c) {
            this.c = c;
        }

        /**
         * Gets the character that represents this action.
         *
         * @return character representing the action
         */
        public char getChar() {
            return this.c;
        }
    }

    /**
     * Gets if the mode is being set or removed.
     *
     * @return the action, add or remove
     */
    @NonNull Action getAction();

    /**
     * Gets the {@link Mode} describing this mode status.
     *
     * @return the mode
     */
    @NonNull ModeType getMode();

    /**
     * Gets the parameter for the mode status, if applicable.
     *
     * @return parameter if present
     */
    @NonNull Optional<String> getParameter();
}
