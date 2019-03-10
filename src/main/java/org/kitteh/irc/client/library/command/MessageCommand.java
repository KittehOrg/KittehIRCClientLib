/*
 * * Copyright (C) 2013-2019 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.command;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.MessageReceiver;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

/**
 * Sends a message.
 */
// TODO multi-line, cutter
public class MessageCommand extends Command<MessageCommand> {
    private String target;
    private String message;

    /**
     * Constructs a message command.
     *
     * @param client the client on which this command is executing
     * @throws IllegalArgumentException if null parameters
     */
    public MessageCommand(@NonNull Client client) {
        super(client);
    }

    /**
     * Sets the target of this message.
     *
     * @param target target
     * @return this command
     * @throws IllegalArgumentException if target is null or contains invalid characters
     */
    public @NonNull MessageCommand target(@NonNull String target) {
        this.target = Sanity.safeMessageCheck(target, "Target");
        return this;
    }

    public @NonNull MessageCommand target(@NonNull MessageReceiver target) {
        this.target = Sanity.nullCheck(target, "Target").getMessagingName();
        return this;
    }

    /**
     * Sets the message to send.
     *
     * @param message message
     * @return this command
     * @throws IllegalArgumentException if message contains invalid characters
     */
    public @NonNull MessageCommand message(@Nullable String message) {
        this.message = Sanity.safeMessageCheck(message, "Message");
        return this;
    }

    /**
     * Executes the command.
     *
     * @throws IllegalStateException if target or message is not defined
     */
    @Override
    public void execute() {
        if (this.target == null) {
            throw new IllegalStateException("Target not defined");
        }
        if (this.message == null) {
            throw new IllegalStateException("Message not defined");
        }
        this.sendCommandLine("PRIVMSG " + this.target + " :" + this.message);
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("target", this.target).add("message", this.message);
    }
}
