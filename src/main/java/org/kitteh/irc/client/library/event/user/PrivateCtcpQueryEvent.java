/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.abstractbase.ActorPrivateMessageEventBase;
import org.kitteh.irc.client.library.event.helper.ActorMessageEvent;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * The client has received a CTCP message! There are a few (FINGER, PING,
 * TIME, VERSION) queries which have a default reply message. Others are
 * simply ignored by default. The method {@link #getMessage()} returns the
 * unescaped message with the delimiter removed. Note that the reply is not
 * sent if the target is not the client.
 * <p>
 * See {@link PrivateCtcpReplyEvent} for received CTCP replies.
 */
public class PrivateCtcpQueryEvent extends ActorPrivateMessageEventBase<User> implements ActorMessageEvent<User> {
    @Nullable
    private String reply;

    /**
     * Creates the event
     *
     * @param client client for which this is occurring
     * @param originalMessages original messages
     * @param sender sender of the query
     * @param target target of the query
     * @param message message sent
     * @param reply reply to be sent, if any
     */
    public PrivateCtcpQueryEvent(@Nonnull Client client, @Nonnull List<ServerMessage> originalMessages, @Nonnull User sender, @Nonnull String target, @Nonnull String message, @Nullable String reply) {
        super(client, originalMessages, sender, target, message);
        this.reply = reply;
    }

    /**
     * Gets the reply to be sent to the CTCP sender.
     *
     * @return the reply if one is set
     */
    @Nonnull
    public Optional<String> getReply() {
        return Optional.ofNullable(this.reply);
    }

    /**
     * Sets the reply to send to the CTCP sender. Note that this method will
     * not work if listening async. For async replies, set this to null and
     * use {@link Client#sendCtcpReply(String, String)}.
     *
     * @param reply message to send back
     */
    public void setReply(@Nullable String reply) {
        this.reply = (reply == null) ? null : Sanity.safeMessageCheck(reply, "Reply");
    }

    @Override
    @Nonnull
    protected ToStringer toStringer() {
        return super.toStringer().add("reply", this.reply);
    }
}
