/*
 * * Copyright (C) 2013-2014 Matt Baxter http://kitteh.org
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
package org.kitteh.irc;

/**
 * Channel mode types.
 */
public enum ChannelModeType {
    /**
     * Always has parameter, which is a mask.
     */
    A(true, true),
    /**
     * Always has parameter.
     */
    B(true, true),
    /**
     * Has parameter when setting.
     */
    C(true, false),
    /**
     * Never has parameters.
     */
    D(false, false);

    private final boolean parameterOnRemoval;
    private final boolean parameterOnSetting;

    private ChannelModeType(boolean parameterOnSetting, boolean parameterOnRemoval) {
        this.parameterOnRemoval = parameterOnRemoval;
        this.parameterOnSetting = parameterOnSetting;
    }

    public boolean isParameterOnRemoval() {
        return this.parameterOnRemoval;
    }

    public boolean isParameterOnSetting() {
        return this.parameterOnSetting;
    }
}