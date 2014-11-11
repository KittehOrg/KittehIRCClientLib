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
package org.kitteh.irc.event.channel;

import org.kitteh.irc.elements.Channel;
import org.kitteh.irc.elements.Actor;
import org.kitteh.irc.event.ActorChannelMessageEvent;

/**
 * An {@link org.kitteh.irc.elements.Actor} has kicked a user!
 */
public class ChannelKickEvent extends ActorChannelMessageEvent<Actor> {
    private final String target;

    /**
     * Creates the event.
     *
     * @param channel channel being left
     * @param actor actor kicking the targeted user
     * @param target targeted nick
     * @param message message the user left
     */
    public ChannelKickEvent(Channel channel, Actor actor, String target, String message) {
        super(actor, channel, message);
        this.target = target;
    }

    /**
     * Gets the kicked nick.
     *
     * @return the nickname of the kicked user
     */
    public String getTarget() {
        return this.target;
    }
}