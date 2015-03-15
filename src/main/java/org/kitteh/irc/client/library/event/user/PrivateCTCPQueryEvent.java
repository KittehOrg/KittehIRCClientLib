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
package org.kitteh.irc.client.library.event.user;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.ActorMessageEvent;

/**
 * The client has received a CTCP message! There are a few (FINGER, PING,
 * TIME, VERSION) queries which have a default reply message. Others are
 * simply ignored by default. The method {@link #getMessage()} returns the
 * message with the delimiter character (1) removed.
 * <p>
 * See {@link PrivateCTCPReplyEvent} for received CTCP replies.
 */
public class PrivateCTCPQueryEvent extends ActorMessageEvent<User> {
    private String reply;

    /**
     * Creates the event
     *
     * @param client client for which this is occurring
     * @param sender sender of the query
     * @param message message sent
     * @param reply reply to be sent, if any
     */
    public PrivateCTCPQueryEvent(Client client, User sender, String message, String reply) {
        super(client, sender, message);
        this.reply = reply;
    }

    /**
     * Gets the reply to be sent to the CTCP sender.
     *
     * @return the reply, or null if no reply will be sent
     */
    public String getReply() {
        return this.reply;
    }

    /**
     * Sets the reply to send to the CTCP sender. Null for no reply.
     *
     * @param reply message to send back
     */
    public void setReply(String reply) {
        this.reply = reply;
    }
}