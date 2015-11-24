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
package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.ISupportManager;

import javax.annotation.Nonnull;
import java.util.Set;

abstract class InternalClient implements Client {
    abstract void connect();

    @Nonnull
    abstract ActorProvider getActorProvider();

    @Nonnull
    @Override
    public abstract IRCCapabilityManager getCapabilityManager();

    @Nonnull
    abstract Config getConfig();

    @Nonnull
    abstract Listener<Exception> getExceptionListener();

    @Nonnull
    abstract Listener<String> getInputListener();

    @Nonnull
    abstract Set<String> getIntendedChannels();

    @Nonnull
    public abstract IRCISupportManager getISupportManager();

    @Nonnull
    abstract Listener<String> getOutputListener();

    @Nonnull
    abstract String getRequestedNick();

    @Override
    @Nonnull
    public abstract IRCServerInfo getServerInfo();

    abstract void ping();

    abstract void processLine(@Nonnull String line);

    abstract void resetServerInfo();

    abstract void sendNickChange(@Nonnull String newNick);

    abstract void setCurrentNick(@Nonnull String nick);

    abstract void startSending();
}
