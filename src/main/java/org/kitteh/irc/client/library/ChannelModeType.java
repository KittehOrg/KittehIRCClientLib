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
package org.kitteh.irc.client.library;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Channel mode types.
 */
public enum ChannelModeType {
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

    private final static Map<Character, ChannelModeType> DEFAULT_MODES = new HashMap<Character, ChannelModeType>() {
        {
            put('b', ChannelModeType.A_MASK);
            put('k', ChannelModeType.B_PARAMETER_ALWAYS);
            put('l', ChannelModeType.C_PARAMETER_ON_SET);
            put('i', ChannelModeType.D_PARAMETER_NEVER);
            put('m', ChannelModeType.D_PARAMETER_NEVER);
            put('n', ChannelModeType.D_PARAMETER_NEVER);
            put('p', ChannelModeType.D_PARAMETER_NEVER);
            put('s', ChannelModeType.D_PARAMETER_NEVER);
            put('t', ChannelModeType.D_PARAMETER_NEVER);
        }
    };

    /**
     * Gets the default modes expected by the client if not sent by the
     * server.
     *
     * @return default expected nodes
     */
    static Map<Character, ChannelModeType> getDefaultModes() {
        return new ConcurrentHashMap<>(DEFAULT_MODES);
    }

    private final boolean parameterRequiredOnRemoval;
    private final boolean parameterRequiredOnSetting;

    private ChannelModeType(boolean parameterRequiredOnSetting, boolean parameterRequiredOnRemoval) {
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