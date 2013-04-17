/*
 * * Copyright (C) 2013 Matt Baxter http://kitteh.org
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

public enum IRCFormat {
    BLACK("\u000301"),
    BLUE("\u000312"),
    BOLD("\u0002"),
    BROWN("\u000305"),
    CYAN("\u000311"),
    DARK_BLUE("\u000302"),
    DARK_GRAY("\u000314"),
    DARK_GREEN("\u000303"),
    GREEN("\u000309"),
    LIGHT_GRAY("\u000315"),
    MAGENTA("\u000313"),
    RESET("\u000f"),
    OLIVE("\u000307"),
    PURPLE("\u000306"),
    RED("\u000304"),
    REVERSE("\u0016"),
    TEAL("\u000310"),
    UNDERLINE("\u001f"),
    WHITE("\u000300"),
    YELLOW("\u000308");

    private String string;

    private IRCFormat(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return this.string;
    }
}