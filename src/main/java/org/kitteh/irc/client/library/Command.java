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

import java.util.HashMap;
import java.util.Map;

/**
 * Commands used in client/server communication.
 */
enum Command {
    INVITE,
    JOIN,
    KICK,
    MODE,
    NICK,
    NOTICE,
    PART,
    PRIVMSG,
    TOPIC,
    QUIT;

    private static final Map<String, Command> nameMap = new HashMap<>();

    static {
        for (Command command : values()) {
            nameMap.put(command.name(), command);
        }
    }

    /**
     * Gets a Command by name. Case insensitive.
     *
     * @param name the name of the Command to get
     * @return the matching Command or null if no match
     */
    public static Command getByName(String name) {
        return nameMap.get(name.toUpperCase());
    }

    @Override
    public String toString() {
        return this.name(); // Explicitly overriding as a reminder that this is used as such
    }
}