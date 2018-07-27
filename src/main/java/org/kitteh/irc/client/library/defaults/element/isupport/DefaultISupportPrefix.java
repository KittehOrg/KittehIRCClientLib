/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.defaults.element.mode.DefaultChannelUserMode;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.exception.KittehServerISupportException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link ISupportParameter.Prefix}.
 */
public class DefaultISupportPrefix extends DefaultISupportParameterValueRequired implements ISupportParameter.Prefix {
    private static final Pattern PATTERN = Pattern.compile("\\(([a-zA-Z]+)\\)([^ ]+)");

    private final List<ChannelUserMode> modes;

    /**
     * Constructs the object.
     *
     * @param client client
     * @param name parameter name
     * @param value parameter value, if present
     */
    public DefaultISupportPrefix(@NonNull Client client, @NonNull String name, @Nullable String value) {
        super(client, name, value);
        if (value == null) {
            throw new KittehServerISupportException(name, "No prefixes defined");
        }
        Matcher matcher = PATTERN.matcher(value);
        if (!matcher.find()) {
            throw new KittehServerISupportException(name, "Data does not match expected pattern");
        }
        String modes = matcher.group(1);
        String display = matcher.group(2);
        if (modes.length() != display.length()) {
            throw new KittehServerISupportException(name, "Prefix and mode size mismatch");
        }
        List<ChannelUserMode> prefixList = new ArrayList<>();
        for (int index = 0; index < modes.length(); index++) {
            prefixList.add(new DefaultChannelUserMode(client, modes.charAt(index), display.charAt(index)));
        }
        this.modes = Collections.unmodifiableList(prefixList);
    }

    @Override
    public @NonNull List<ChannelUserMode> getModes() {
        return this.modes;
    }
}
