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
package org.kitteh.irc.event;

import org.kitteh.irc.elements.Channel;
import org.kitteh.irc.elements.User;

/**
 * The bot has received a CTCP message!
 */
public class ChannelCTCPEvent {
    private Channel channel;
    private String message;
    private User sender;

    public ChannelCTCPEvent(User sender, Channel channel, String message) {
        this.channel = channel;
        this.message = message;
        this.sender = sender;
    }

    /**
     * Gets the channel to which the message was sent.
     *
     * @return target channel
     */
    public Channel getChannel() {
        return this.channel;
    }

    /**
     * Gets the CTCP message sent.
     *
     * @return the CTCP message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Gets the sender of the CTCP message.
     *
     * @return the sender
     */
    public User getSender() {
        return this.sender;
    }
}