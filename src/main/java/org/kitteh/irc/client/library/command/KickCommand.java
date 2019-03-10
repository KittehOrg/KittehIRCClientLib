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
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageType;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

/**
 * Get your KICKs on Route 66.
 */
public class KickCommand extends ChannelCommand<KickCommand> {
    private String target;
    private @Nullable String reason;

    /**
     * Constructs a KICK command for a given channel.
     *
     * @param client the client on which this command is executing
     * @param channel channel targeted
     * @throws IllegalArgumentException if null parameters
     * @see Channel#kick(User)
     * @see Channel#kick(User, String)
     */
    public KickCommand(@NonNull Client client, @NonNull String channel) {
        super(client, channel);
        this.reason = this.getClient().getDefaultMessageMap().getDefault(DefaultMessageType.KICK).orElse(null);
    }

    /**
     * Sets the target of this kick.
     *
     * @param target target
     * @return this command
     * @throws IllegalArgumentException if target is null or contains invalid characters
     */
    public @NonNull KickCommand target(@NonNull String target) {
        this.target = Sanity.safeMessageCheck(target, "Target");
        return this;
    }

    /**
     * Sets the target of this kick.
     *
     * @param target target
     * @return this command
     * @throws IllegalArgumentException if target is null or from a different Client
     */
    public @NonNull KickCommand target(@NonNull User target) {
        Sanity.nullCheck(target, "Target");
        Sanity.truthiness(target.getClient() == this.getClient(), "User comes from a different client");
        this.target(target.getNick());
        return this;
    }

    /**
     * Sets the reason for this kick.
     *
     * @param reason reason or null to provide no reason
     * @return this command
     * @throws IllegalArgumentException if reason contains invalid characters
     */
    public @NonNull KickCommand reason(@Nullable String reason) {
        this.reason = (reason == null) ? null : Sanity.safeMessageCheck(reason, "Reason");
        return this;
    }

    /**
     * Executes the command.
     *
     * @throws IllegalStateException if target is not defined
     */
    @Override
    public void execute() {
        if (this.target == null) {
            throw new IllegalStateException("Target not defined");
        }
        this.sendCommandLine("KICK " + this.getChannel() + ' ' + this.target + (this.reason != null ? (" :" + this.reason) : ""));
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("target", this.target).add("reason", this.reason);
    }
}
