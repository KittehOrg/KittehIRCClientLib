/*
 * * Copyright (C) 2013-2021 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.feature;

import net.engio.mbassy.listener.Handler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.filter.FilterProcessor;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

/**
 * Processes and registers events for a single {@link Client} instance. This
 * event manager utilizes MBassador, a lightweight event bus.
 */
public interface EventManager {
    /**
     * Calls an event, triggering any registered methods for the event class.
     *
     * @param event event to call
     * @throws IllegalArgumentException for a null event
     */
    void callEvent(@NonNull Object event);

    /**
     * Gets all registered listener objects.
     *
     * @return a set of objects
     */
    @NonNull Set<Object> getRegisteredEventListeners();

    /**
     * Gets a map of all registered annotation filters.
     *
     * @return a map of annotations to annotation filter processors
     */
    @NonNull Map<Class<? extends Annotation>, FilterProcessor<?, ? extends Annotation>> getAnnotationFilters();

    /**
     * Registers an annotation to be used in filtering events if present on
     * a handler method. Annotations are only processed on listeners
     * registered after they are added, so add all annotations prior to
     * registering any listeners.
     *
     * @param annotationClass annotation to register
     * @param filterProcessor filter processor to process such annotations
     * @param <A> annotation type
     */
    <A extends Annotation> void registerAnnotationFilter(Class<A> annotationClass, FilterProcessor<?, A> filterProcessor);

    /**
     * Registers annotated with {@link Handler} with sync invocation,
     * provided they have a single parameter. This parameter is the event.
     *
     * @param listener listener in which to register events
     * @throws IllegalArgumentException for a null listener
     */
    void registerEventListener(@NonNull Object listener);

    /**
     * Unregisters a listener.
     *
     * @param listener listener to unregister
     * @throws IllegalArgumentException for a null listener
     */
    void unregisterEventListener(@NonNull Object listener);
}
