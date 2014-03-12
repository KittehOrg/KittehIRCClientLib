/*
 * * Copyright (C) 2013-2014 Matt Baxter http://kitteh.org
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
package org.kitteh.irc;

import org.kitteh.irc.util.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages them events.
 */
public final class EventManager {
    private final Map<Class<?>, Set<Pair<Object, Method>>> registeredEvents = new ConcurrentHashMap<>();

    EventManager() {
        // NOOP
    }

    /**
     * Registers any non-static methods annotated with
     * {@link EventHandler}, provided they have a single
     * parameter. This parameter is the event.
     * <p/>
     * The class listened to must be the same class called in
     * {@link #callEvent(Object)}, not a super or subclass.
     *
     * @param listener listener in which to register events
     */
    public void registerEventListener(Object listener) {
        try {
            Method[] methods = listener.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (Modifier.isStatic(method.getModifiers()) || method.getAnnotation(EventHandler.class) == null) {
                    continue;
                }
                Class<?>[] types = method.getParameterTypes();
                if (types.length != 1) {
                    System.out.println("Event handlers must only have one parameter. Invalid method " + method.getName() + " in class " + listener.getClass().getSimpleName());
                    continue;
                }
                method.setAccessible(true);
                this.getSet(types[0]).add(new Pair<>(listener, method));
            }
        } catch (Exception e) {
            System.out.println("Exception registering " + listener.getClass().getSimpleName() + ":");
            e.printStackTrace();
        }
    }

    /**
     * Calls an event.
     *
     * @param event event to call
     */
    public void callEvent(Object event) {
        Set<Pair<Object, Method>> set = this.registeredEvents.get(event.getClass());
        if (set != null) {
            for (Pair<Object, Method> pair : set) {
                try {
                    pair.getB().invoke(pair.getA(), event);
                } catch (Throwable thrown) {
                    System.out.println("Exception calling event " + event.getClass().getSimpleName() + " in " + pair.getA().getClass().getSimpleName());
                }
            }
        }
    }

    private synchronized Set<Pair<Object, Method>> getSet(Class<?> type) {
        Set<Pair<Object, Method>> set = this.registeredEvents.get(type);
        if (set != null) {
            set = Collections.newSetFromMap(new ConcurrentHashMap<Pair<Object, Method>, Boolean>());
            this.registeredEvents.put(type, set);
        }
        return set;
    }
}