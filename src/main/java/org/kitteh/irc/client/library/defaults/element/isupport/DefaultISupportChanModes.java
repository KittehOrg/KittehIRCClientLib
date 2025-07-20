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
package org.kitteh.irc.client.library.defaults.element.isupport;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.mode.DefaultChannelMode;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.mode.ChannelMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link ISupportParameter.ChanModes}.
 */
public class DefaultISupportChanModes extends DefaultISupportParameterValueRequired implements ISupportParameter.ChanModes {
    private final List<ChannelMode> modes;

    /**
     * Constructs the object.
     *
     * @param client client
     * @param name parameter name
     * @param value parameter value, if present
     */
    public DefaultISupportChanModes(@NonNull Client client, @NonNull String name, @Nullable String value) {
        super(client, name, value);
        String[] modes = value.split(",");
        List<ChannelMode> modesList = new ArrayList<>();
        for (int typeId = 0; (typeId < modes.length) && (typeId < 4); typeId++) {
            for (char mode : modes[typeId].toCharArray()) {
                ChannelMode.Type type = switch (typeId) {
                    case 0 -> ChannelMode.Type.A_MASK;
                    case 1 -> ChannelMode.Type.B_PARAMETER_ALWAYS;
                    case 2 -> ChannelMode.Type.C_PARAMETER_ON_SET;
                    default -> ChannelMode.Type.D_PARAMETER_NEVER;
                };
                modesList.add(new DefaultChannelMode(client, mode, type));
            }
        }
        this.modes = Collections.unmodifiableList(modesList);
    }

    @Override
    public @NonNull List<ChannelMode> getModes() {
        return this.modes;
    }
}
