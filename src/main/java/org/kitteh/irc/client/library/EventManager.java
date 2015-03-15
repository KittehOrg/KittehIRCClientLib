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
package org.kitteh.irc.client.library;

import org.kitteh.irc.client.library.util.Pair;
import org.kitteh.irc.client.library.exception.KittehEventException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Processes and registers events for a single {@link Client} instance.
 */
public final class EventManager {
    private final IRCClient client;
    private final Map<Class<?>, Set<Pair<Object, Method>>> registeredEvents = new ConcurrentHashMap<>();

    EventManager(IRCClient client) {
        this.client = client;
    }

    /**
     * Registers any non-static methods annotated with {@link EventHandler},
     * provided they have a single parameter. This parameter is the event.
     * <p>
     * The class listened to must be the same class called in
     * {@link #callEvent(Object)}, not a super or subclass.
     *
     * @param listener listener in which to register events
     */
    public void registerEventListener(Object listener) {
        Arrays.stream(listener.getClass().getDeclaredMethods())
                // Disregard the following situations:
                .filter(method -> !Modifier.isStatic(method.getModifiers())) // 1. Static method. We're registering objects here not classes.
                .filter(method -> method.getAnnotation(EventHandler.class) != null) // 2. No EventHandler annotation
                .filter(method -> method.getParameterTypes().length == 1) // 3. Methods with 0 or 2+ parameters. We only want methods with one parameter.
                .filter(method -> (method.getParameterTypes()[0].getModifiers() & (Modifier.ABSTRACT | Modifier.INTERFACE)) == 0) // 4. Parameters which are abstract classes or interfaces. We don't send subclasses, making those useless to register.
                .forEach(method -> {
                    method.setAccessible(true);
                    this.getSet(method.getParameterTypes()[0]).add(new Pair<>(listener, method));
                });
    }

    /**
     * Calls an event, triggering any registered methods for the event class.
     *
     * @param event event to call
     */
    public void callEvent(Object event) {
        Set<Pair<Object, Method>> set = this.registeredEvents.get(event.getClass());
        if (set != null) {
            for (Pair<Object, Method> pair : set) {
                try {
                    pair.getRight().invoke(pair.getLeft(), event);
                } catch (Throwable thrown) {
                    this.client.getExceptionListener().queue(new KittehEventException(thrown));
                }
            }
        }
    }

    private synchronized Set<Pair<Object, Method>> getSet(Class<?> type) {
        Set<Pair<Object, Method>> set = this.registeredEvents.get(type);
        if (set == null) {
            set = Collections.newSetFromMap(new ConcurrentHashMap<>());
            this.registeredEvents.put(type, set);
        }
        return set;
    }
}