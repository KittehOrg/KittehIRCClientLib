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
package org.kitteh.irc.client.library.element.mode;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ClientLinked;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * A particular status of a mode.
 */
public class ModeStatus<ModeType extends Mode> implements ClientLinked {
    private final ModeType mode;
    private final Optional<String> parameter;
    private final boolean setting;

    /**
     * Creates a status without a parameter.
     *
     * @param setting true for setting mode, false for removing
     * @param mode mode to set
     */
    public ModeStatus(boolean setting, @Nonnull ModeType mode) {
        this.mode = Sanity.nullCheck(mode, "Mode cannot be null");
        this.parameter = Optional.empty();
        this.setting = setting;
    }

    /**
     * Creates a status.
     *
     * @param setting true for setting mode, false for removing
     * @param mode mode to set
     * @param parameter parameter
     */
    public ModeStatus(boolean setting, @Nonnull ModeType mode, @Nonnull String parameter) {
        this.mode = Sanity.nullCheck(mode, "Mode cannot be null");
        this.parameter = Optional.of(Sanity.safeMessageCheck(parameter, "Parameter"));
        this.setting = setting;
    }

    @Override
    @Nonnull
    public Client getClient() {
        return this.getMode().getClient();
    }

    /**
     * Gets the {@link Mode} describing this mode status.
     *
     * @return the mode
     */
    @Nonnull
    public ModeType getMode() {
        return this.mode;
    }

    /**
     * Gets the parameter for the mode status, if applicable.
     *
     * @return parameter if present
     */
    @Nonnull
    public Optional<String> getParameter() {
        return this.parameter;
    }

    /**
     * Gets if this mode is being set.
     *
     * @return true for setting, false for removing
     */
    public boolean isSetting() {
        return this.setting;
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("client", this.getClient()).add("mode", this.mode).add("setting", this.setting).add("parameter", this.parameter).toString();
    }
}
