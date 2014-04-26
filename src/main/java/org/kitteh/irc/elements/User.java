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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User on an IRC network.
 */
public class User extends Actor {
    // Valid nick chars: \w\[]^`{}|-_
    // Pattern unescaped: ([\w\\\[\]\^`\{\}\|\-_]+)!([~\w]+)@([\w\.\-:]+)
    // You know what? Screw it.
    // Let's just do it assuming no IRCD can handle following the rules.
    // New pattern: ([^!@]+)!([^!@]+)@([^!@]+)
    private static final Pattern PATTERN = Pattern.compile("([^!@]+)!([^!@]+)@([^!@]+)");

    public static boolean isUser(String name) {
        return name != null && PATTERN.matcher(name).matches();
    }

    private final String host;
    private final String nick;
    private final String user;

    User(String mask) {
        super(mask);
        Matcher matcher = PATTERN.matcher(mask);
        matcher.find();
        this.nick = matcher.group(1);
        this.user = matcher.group(2);
        this.host = matcher.group(3);
    }

    public String getHost() {
        return this.host;
    }

    public String getNick() {
        return this.nick;
    }

    public String getUser() {
        return this.user;
    }
}