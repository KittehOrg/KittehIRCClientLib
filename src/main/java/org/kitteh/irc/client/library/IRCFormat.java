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

/**
 * Represents various formatting available in IRC.
 * <p>
 * The {@link #toString} method provides the String you need.
 */
public enum IRCFormat {
    BLACK(1),
    BLUE(12),
    BOLD('\u0002'),
    BROWN(5),
    CYAN(11),
    DARK_BLUE(2),
    DARK_GRAY(14),
    DARK_GREEN(3),
    GREEN(9),
    LIGHT_GRAY(15),
    MAGENTA(13),
    RESET('\u000f'),
    OLIVE(7),
    PURPLE(6),
    RED(4),
    REVERSE('\u0016'),
    TEAL(10),
    UNDERLINE('\u001f'),
    WHITE(0),
    YELLOW(8);

    private String toString;

    private IRCFormat(char ch) {
        this.toString = String.valueOf(ch);
    }

    private IRCFormat(int color) {
        if ((color & 15) != color) {
            throw new AssertionError("Impossible color id: " + color);
        }
        final StringBuilder builder = new StringBuilder();
        builder.append(COLOR_CHAR);
        if (color < 10) {
            builder.append('0');
        }
        builder.append(color);
        this.toString = builder.toString();
    }

    public static final char COLOR_CHAR = '\u0003';

    @Override
    public String toString() {
        return this.toString;
    }
}