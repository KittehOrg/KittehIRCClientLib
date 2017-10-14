/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.element;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.Cutter;
import org.kitteh.irc.client.library.util.StringUtil;

import javax.annotation.Nonnull;

/**
 * Represents an {@link Actor} capable of receiving messages.
 */
public interface MessageReceiver extends Actor {
    /**
     * Gets the name used when sending messages.
     *
     * @return the name by which you can message this receiver
     */
    @Nonnull
    String getMessagingName();

    /**
     * Gets the lowercase version of the name used when sending messages.
     *
     * @return the lowercase version of the name by which you can message this receiver
     */
    @Nonnull
    default String getLowerCaseMessagingName() {
        return StringUtil.toLowerCase(this, this.getMessagingName());
    }

    /**
     * Sends this actor a CTCP message.
     *
     * @param message the message to send
     * @see Client#sendCTCPMessage(MessageReceiver, String)
     */
    default void sendCTCPMessage(@Nonnull String message) {
        this.getClient().sendCTCPMessage(this, message);
    }

    /**
     * Sends this actor a message.
     *
     * @param message the message to send
     * @see Client#sendMessage(MessageReceiver, String)
     */
    default void sendMessage(@Nonnull String message) {
        this.getClient().sendMessage(this, message);
    }

    /**
     * Sends this actor a potentially multi-line message using the client's
     * default message cutter.
     * <p>
     * Note that bots may not react appropriately to a message split across
     * multiple lines.
     *
     * @param message message to send
     * @see Client#sendMultiLineMessage(MessageReceiver, String)
     */
    default void sendMultiLineMessage(@Nonnull String message) {
        this.getClient().sendMultiLineMessage(this, message);
    }

    /**
     * Sends this actor a potentially multi-line message using a specified
     * message cutter.
     * <p>
     * Note that bots may not react appropriately to a message split across
     * multiple lines.
     *
     * @param message message to send
     * @param cutter cutter to utilize
     * @see Client#sendMultiLineMessage(MessageReceiver, String, Cutter)
     */
    default void sendMultiLineMessage(@Nonnull String message, @Nonnull Cutter cutter) {
        this.getClient().sendMultiLineMessage(this, message, cutter);
    }

    /**
     * Sends this actor a notice.
     *
     * @param message the message to send
     * @see Client#sendNotice(MessageReceiver, String)
     */
    default void sendNotice(@Nonnull String message) {
        this.getClient().sendNotice(this, message);
    }

    /**
     * Sends this actor a potentially multi-line notice using the client's
     * default message cutter.
     * <p>
     * Note that bots may not react appropriately to a message split across
     * multiple lines.
     *
     * @param message message to send
     * @see Client#sendMultiLineNotice(MessageReceiver, String)
     */
    default void sendMultiLineNotice(@Nonnull String message) {
        this.getClient().sendMultiLineNotice(this, message);
    }

    /**
     * Sends this actor a potentially multi-line notice using a specified
     * message cutter.
     * <p>
     * Note that bots may not react appropriately to a message split across
     * multiple lines.
     *
     * @param message message to send
     * @param cutter cutter to utilize
     * @see Client#sendMultiLineNotice(MessageReceiver, String, Cutter)
     */
    default void sendMultiLineNotice(@Nonnull String message, @Nonnull Cutter cutter) {
        this.getClient().sendMultiLineNotice(this, message, cutter);
    }
}
