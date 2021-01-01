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
package org.kitteh.irc.client.library.element.mode;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

/**
 * A list of mode statuses.
 *
 * @param <ModeType> type of modes being listed
 */
public interface ModeStatusList<ModeType extends Mode> {
    /**
     * Gets if the given mode is present in the list.
     *
     * @param mode mode to check
     * @return true if present at least once
     */
    boolean contains(@NonNull ModeType mode);

    /**
     * Gets if the given mode character is present in the list.
     *
     * @param mode mode to check
     * @return true if present at least once
     */
    boolean containsMode(char mode);

    /**
     * Gets all mode statuses of a given mode.
     *
     * @param mode mode to check
     * @return all matching modes or empty if none match
     */
    @NonNull List<ModeStatus<ModeType>> getByMode(@NonNull ModeType mode);

    /**
     * Gets all mode statuses of a given mode character.
     *
     * @param mode mode to check
     * @return all matching modes or empty if none match
     */
    @NonNull List<ModeStatus<ModeType>> getByMode(char mode);

    /**
     * Gets the list of statuses.
     *
     * @return status list
     */
    @NonNull List<ModeStatus<ModeType>> getAll();

    /**
     * Gets the statuses in a convenient String format.
     *
     * @return string of modes
     */
    @NonNull String getAsString();
}
