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
import org.kitteh.irc.elements.MessageSender;

/**
 * Fires when a message is sent to a channel.
 */
public class ChannelMessageEvent {
    private Channel channel;
    private String message;
    private MessageSender sender;

    /**
     * Creates a PrivateMessageEvent
     *
     * @param sender who sent it
     * @param channel channel receiving
     * @param message message sent
     */
    public ChannelMessageEvent(MessageSender sender, Channel channel, String message) {
        this.channel = channel;
        this.message = message;
        this.sender = sender;
    }

    /**
     * Gets the channel receiving the message.
     *
     * @return channel receiving message
     */
    public Channel getChannel() {
        return this.channel;
    }

    /**
     * Gets the message sent
     *
     * @return message sent
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Gets the sender of the message.
     *
     * @return message sender
     */
    public MessageSender getSender() {
        return this.sender;
    }
}