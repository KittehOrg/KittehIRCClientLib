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

/**
 * An entity on an IRC server.
 */
public class Actor {
    /**
     * Gets an Actor for the given name. Acquires the proper subclass based
     * on the provided name. If no subclass can be found, an Actor object
     * will be provided.
     *
     * @param name the Actor's name
     * @return an Actor object for the given name
     */
    public static Actor getActor(String name) {
        try {
            if (User.isUser(name)) {
                return new User(name);
            } else if (Channel.isChannel(name)) {
                return new Channel(name);
            }
        } catch (Throwable ignored) {
            // NOOP
        }
        return new Actor(name);
    }

    private final String name;

    protected Actor(String name) {
        this.name = name;
    }

    /**
     * Gets the Actor's name.
     *
     * @return the Actor's name
     */
    public String getName() {
        return this.name;
    }
}