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

import net.engio.mbassy.bus.SyncMessageBus;
import net.engio.mbassy.bus.common.Properties;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import org.kitteh.irc.client.library.EventManager;
import org.kitteh.irc.client.library.event.helper.ClientEvent;
import org.kitteh.irc.client.library.exception.KittehEventException;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

final class IRCEventManager implements EventManager {
    private class Exceptional implements IPublicationErrorHandler {
        @Override
        public void handleError(@Nonnull PublicationError publicationError) {
            Exception exceptional;
            Throwable thrown = publicationError.getCause();
            if ((publicationError.getCause() instanceof InvocationTargetException) && (thrown.getCause() instanceof KittehServerMessageException)) {
                exceptional = (KittehServerMessageException) thrown.getCause();
            } else {
                exceptional = new KittehEventException(thrown);
            }
            IRCEventManager.this.client.getExceptionListener().queue(exceptional);
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).toString();
        }
    }

    private final SyncMessageBus<Object> bus = new SyncMessageBus<>(new BusConfiguration().addFeature(Feature.SyncPubSub.Default()).setProperty(Properties.Handler.PublicationError, new Exceptional()));
    private final InternalClient client;
    private final Set<Object> listeners = new HashSet<>();

    IRCEventManager(@Nonnull InternalClient client) {
        this.client = client;
    }

    @Override
    public void callEvent(@Nonnull Object event) {
        Sanity.nullCheck(event, "Event cannot be null");
        if (event instanceof ClientEvent) {
            Sanity.truthiness(((ClientEvent) event).getClient() == this.client, "Event cannot be from another client!");
        }
        this.bus.publish(event);
    }

    @Nonnull
    @Override
    public synchronized Set<Object> getRegisteredEventListeners() {
        return new HashSet<>(this.listeners);
    }

    @Override
    public synchronized void registerEventListener(@Nonnull Object listener) {
        Sanity.nullCheck(listener, "Listener cannot be null");
        this.listeners.add(listener);
        this.bus.subscribe(listener);
    }

    @Override
    public synchronized void unregisterEventListener(@Nonnull Object listener) {
        Sanity.nullCheck(listener, "Listener cannot be null");
        this.listeners.remove(listener);
        this.bus.unsubscribe(listener);
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).add("client", this.client).toString();
    }
}
