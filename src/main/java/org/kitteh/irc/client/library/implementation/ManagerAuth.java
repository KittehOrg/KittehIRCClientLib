/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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
import org.kitteh.irc.client.library.auth.AuthManager;
import org.kitteh.irc.client.library.auth.protocol.AuthProtocol;
import org.kitteh.irc.client.library.auth.protocol.element.EventListening;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

final class ManagerAuth implements AuthManager {
    private final Client client;
    private final Set<AuthProtocol> protocols = new HashSet<>();

    ManagerAuth(@Nonnull Client client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public synchronized Optional<AuthProtocol> addProtocol(@Nonnull AuthProtocol protocol) {
        Sanity.nullCheck(protocol, "Protocol cannot be null");
        List<AuthProtocol> matching = this.protocols.stream().filter(p -> p.getClass() == protocol.getClass()).collect(Collectors.toList());
        Optional<AuthProtocol> removed = Optional.ofNullable(matching.isEmpty() ? null : matching.get(0));
        removed.ifPresent(this::removeProtocol);
        this.protocols.add(protocol);
        if (protocol instanceof EventListening) {
            this.client.getEventManager().registerEventListener(((EventListening) protocol).getEventListener());
        }
        return removed;
    }

    @Nonnull
    @Override
    public synchronized Set<AuthProtocol> getProtocols() {
        return Collections.unmodifiableSet(new HashSet<>(this.protocols));
    }

    @Override
    public synchronized void removeProtocol(@Nonnull AuthProtocol protocol) {
        Sanity.nullCheck(protocol, "Protocol cannot be null");
        this.protocols.remove(protocol);
        if (protocol instanceof EventListening) {
            this.client.getEventManager().unregisterEventListener(((EventListening) protocol).getEventListener());
        }
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("client", this.client).toString();
    }
}
