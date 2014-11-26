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

import org.kitteh.irc.elements.Actor;
import org.kitteh.irc.elements.Channel;
import org.kitteh.irc.event.ActorChannelEvent;

/**
 * A {@link org.kitteh.irc.elements.User} has invited somebody to a
 * {@link org.kitteh.irc.elements.Channel}!
 */
public class ChannelInviteEvent extends ActorChannelEvent<Actor> {
    private final String target;

    /**
     * Creates the event.
     *
     * @param channel the channel
     * @param actor   the actor inviting another
     * @param target  the nick invited
     */
    public ChannelInviteEvent(Channel channel, Actor actor, String target) {
        super(actor, channel);
        this.target = target;
    }

    /**
     * Gets the invited nick.
     *
     * @return the nickname of the invited user
     */
    public String getTarget() {
        return this.target;
    }
}