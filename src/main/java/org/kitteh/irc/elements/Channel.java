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
package org.kitteh.irc.elements;

import java.util.regex.Pattern;

/**
 * Represents an IRC channel.
 */
public class Channel extends Actor {
    // Pattern: ([#!&\+][^ ,\07\r\n]{1,49})
    // Screw it, let's assume IRCDs disregard length policy
    // New pattern: ([#!&\+][^ ,\07\r\n]+)
    private static final Pattern PATTERN = Pattern.compile("([#!&\\+][^ ,\\07\\r\\n]+)");

    /**
     * Gets if a given String is a valid channel name.
     *
     * @param name name to test
     * @return true if not null and a valid channel name
     */
    public static boolean isChannel(String name) {
        return name != null && PATTERN.matcher(name).matches();
    }

    Channel(String channel) {
        super(channel);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Channel && ((Channel) o).getName().toLowerCase().equals(this.getName().toLowerCase());
    }

    @Override
    public int hashCode() {
        return this.getName().toLowerCase().hashCode() * 2;
    }
}