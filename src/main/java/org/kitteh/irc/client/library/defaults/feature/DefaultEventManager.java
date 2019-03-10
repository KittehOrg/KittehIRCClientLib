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
package org.kitteh.irc.client.library.defaults.feature;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.listener.Handler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.connection.ClientConnectionEndedEvent;
import org.kitteh.irc.client.library.event.helper.ClientEvent;
import org.kitteh.irc.client.library.exception.KittehEventException;
import org.kitteh.irc.client.library.exception.KittehNagException;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;
import org.kitteh.irc.client.library.feature.filter.EchoMessage;
import org.kitteh.irc.client.library.feature.filter.FilterProcessor;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;
import org.kitteh.irc.client.library.feature.filter.ToSelfOnly;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link EventManager}.
 */
public class DefaultEventManager implements EventManager {
    /**
     * Exception handler.
     */
    public static class Exceptional implements IPublicationErrorHandler {
        private final Client client;

        /**
         * Constructs this exceptional class.
         *
         * @param client client to send exceptions to
         */
        public Exceptional(@NonNull Client client) {
            this.client = client;
        }

        @Override
        public void handleError(@NonNull PublicationError publicationError) {
            Exception exceptional;
            Throwable thrown = publicationError.getCause();
            if ((thrown instanceof InvocationTargetException) && (thrown.getCause() instanceof KittehServerMessageException)) {
                exceptional = (KittehServerMessageException) thrown.getCause();
            } else if ((thrown instanceof InvocationTargetException) && (thrown.getCause() instanceof KittehNagException)) {
                exceptional = (KittehNagException) thrown.getCause();
            } else {
                exceptional = new KittehEventException(thrown);
            }
            this.client.getExceptionListener().queue(exceptional);
        }

        @Override
        public @NonNull String toString() {
            return new ToStringer(this).toString();
        }
    }

    private final MBassador<Object> bus;
    private final Client client;
    private final Map<Class<? extends Annotation>, FilterProcessor<?, ? extends Annotation>> filters = new ConcurrentHashMap<>();
    private final Set<Object> listeners = new HashSet<>();

    /**
     * Constructs the event manager.
     *
     * @param client client for which this manager will operate
     */
    public DefaultEventManager(@NonNull Client client) {
        BusConfiguration configuration = new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default().setSubscriptionFactory(new FilteringSubscriptionFactory(this.filters)))
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default())
                .addPublicationErrorHandler(new Exceptional(client));
        this.bus = new MBassador<>(configuration);
        this.client = client;
        // Defaults!
        this.registerAnnotationFilter(CommandFilter.class, new CommandFilter.Processor());
        this.registerAnnotationFilter(EchoMessage.class, new EchoMessage.Processor());
        this.registerAnnotationFilter(NumericFilter.class, new NumericFilter.Processor());
        this.registerAnnotationFilter(ToSelfOnly.class, new ToSelfOnly.Processor());

        this.registerEventListener(this);
    }

    @Override
    public void callEvent(@NonNull Object event) {
        Sanity.nullCheck(event, "Event cannot be null");
        if (event instanceof ClientEvent) {
            Sanity.truthiness(((ClientEvent) event).getClient() == this.client, "Event cannot be from another client!");
        }
        this.bus.publish(event);
    }

    @Override
    public synchronized @NonNull Set<Object> getRegisteredEventListeners() {
        return new HashSet<>(this.listeners);
    }

    @Override
    public @NonNull Map<Class<? extends Annotation>, FilterProcessor<?, ? extends Annotation>> getAnnotationFilters() {
        return Collections.unmodifiableMap(new HashMap<>(this.filters));
    }

    @Override
    public <A extends Annotation> void registerAnnotationFilter(Class<A> annotationClass, FilterProcessor<?, A> filterProcessor) {
        this.filters.put(annotationClass, filterProcessor);
    }

    @Override
    public synchronized void registerEventListener(@NonNull Object listener) {
        Sanity.nullCheck(listener, "Listener cannot be null");
        this.listeners.add(listener);
        this.bus.subscribe(listener);
    }

    @Override
    public synchronized void unregisterEventListener(@NonNull Object listener) {
        Sanity.nullCheck(listener, "Listener cannot be null");
        this.listeners.remove(listener);
        this.bus.unsubscribe(listener);
    }

    /**
     * Just the manager listening for shutdown, don't worry about it.
     *
     * @param event event of doom
     */
    @Handler(priority = Integer.MIN_VALUE)
    public void onShutdown(ClientConnectionEndedEvent event) {
        if (!event.canAttemptReconnect()) {
            this.bus.shutdown();
        }
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).add("client", this.client).toString();
    }
}
