/*
 * * Copyright (C) 2013-2019 Matt Baxter https://kitteh.org
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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.mode.Mode;
import org.kitteh.irc.client.library.element.mode.ModeStatus;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.util.Optional;

/**
 * A particular default status of a mode.
 */
public class DefaultModeStatus<ModeType extends Mode> implements ModeStatus<ModeType> {
    private final ModeType mode;
    private final @Nullable String parameter;
    private final Action action;

    /**
     * Creates a status without a parameter.
     *
     * @param action adding or removing
     * @param mode mode to set
     */
    public DefaultModeStatus(Action action, @NonNull ModeType mode) {
        this.mode = Sanity.nullCheck(mode, "Mode");
        this.parameter = null;
        this.action = action;
    }

    /**
     * Creates a status.
     *
     * @param action adding or removing
     * @param mode mode to set
     * @param parameter parameter
     */
    public DefaultModeStatus(Action action, @NonNull ModeType mode, @NonNull String parameter) {
        this.mode = Sanity.nullCheck(mode, "Mode");
        this.parameter = Sanity.safeMessageCheck(parameter, "Parameter");
        this.action = action;
    }

    @Override
    public @NonNull Action getAction() {
        return this.action;
    }

    @Override
    public @NonNull Client getClient() {
        return this.getMode().getClient();
    }

    @Override
    public @NonNull ModeType getMode() {
        return this.mode;
    }

    @Override
    public @NonNull Optional<String> getParameter() {
        return Optional.ofNullable(this.parameter);
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).add("client", this.getClient()).add("mode", this.mode).add("action", this.action).add("parameter", this.parameter).toString();
    }
}
