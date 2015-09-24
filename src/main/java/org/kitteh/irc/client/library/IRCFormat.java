/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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

import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;

/**
 * Represents various formatting available in IRC.
 * <p>
 * The {@link #toString} method provides the String you need.
 */
public enum IRCFormat {
    /**
     * Black.
     */
    BLACK(1),
    /**
     * Blue.
     */
    BLUE(12),
    /**
     * It’s a BOLD strategy Cotton. Let’s see if it pays off for him.
     */
    BOLD('\u0002'),
    /**
     * Brown.
     */
    BROWN(5),
    /**
     * Cyan.
     */
    CYAN(11),
    /**
     * Dark blue.
     */
    DARK_BLUE(2),
    /**
     * Dark gray.
     */
    DARK_GRAY(14),
    /**
     * Dark green.
     */
    DARK_GREEN(3),
    /**
     * Green.
     */
    GREEN(9),
    /**
     * Light gray.
     */
    LIGHT_GRAY(15),
    /**
     * Magenta.
     */
    MAGENTA(13),
    /**
     * Reset formatting.
     */
    RESET('\u000f'),
    /**
     * Olive.
     */
    OLIVE(7),
    /**
     * Purple.
     */
    PURPLE(6),
    /**
     * Red.
     */
    RED(4),
    /**
     * esreveR.
     */
    REVERSE('\u0016'),
    /**
     * Teal.
     */
    TEAL(10),
    /**
     * Underline.
     */
    UNDERLINE('\u001f'),
    /**
     * White.
     */
    WHITE(0),
    /**
     * Yello. Oh yeah.
     */
    YELLOW(8);

    private final int color;
    private final boolean isColor;
    private final String toString;

    IRCFormat(char ch) {
        this.color = -1;
        this.isColor = false;
        this.toString = String.valueOf(ch);
    }

    IRCFormat(int color) {
        if ((color & 15) != color) {
            throw new AssertionError("Impossible color id: " + color);
        }
        this.color = color;
        this.isColor = true;
        this.toString = COLOR_CHAR + ((color < 10) ? "0" : "") + color;
    }

    /**
     * Character indicating color.
     */
    public static final char COLOR_CHAR = '\u0003';

    /**
     * Strips color from a given input.
     *
     * @param input input to strip
     * @return stripped input
     */
    @Nonnull
    public static String stripColor(@Nonnull String input) {
        Sanity.nullCheck(input, "Input cannot be null");
        return input.replaceAll(COLOR_CHAR + "[0-9]{1,2}", "");
    }

    /**
     * Strips formatting from a given input.
     *
     * @param input input to strip
     * @return stripped input
     */
    @Nonnull
    public static String stripFormatting(@Nonnull String input) {
        Sanity.nullCheck(input, "Input cannot be null");
        return input.replaceAll("[" + BOLD + RESET + REVERSE + UNDERLINE + ']', "");
    }

    /**
     * Gets if the format is a color.
     *
     * @return true if a color
     */
    public boolean isColor() {
        return this.isColor;
    }

    /**
     * Gets the int value for the color, if this is a color.
     *
     * @return color int or -1 if not a color
     * @see #isColor()
     */
    public int getColorChar() {
        return this.color;
    }

    @Nonnull
    @Override
    public String toString() {
        return this.toString;
    }

    /**
     * Gets a String for displaying this color with a given background color.
     *
     * @param background background color
     * @return the color string
     * @throws IllegalArgumentException if using or providing a non-color
     */
    @Nonnull
    public String withBackground(@Nonnull IRCFormat background) {
        Sanity.nullCheck(background, "Background cannot be null");
        Sanity.truthiness(this.isColor, "Cannot use non-color foreground.");
        Sanity.truthiness(background.isColor, "Cannot use non-color background");
        return this.toString() + ',' + background.color;
    }
}
