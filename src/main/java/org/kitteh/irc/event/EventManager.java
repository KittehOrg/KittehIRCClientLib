package org.kitteh.irc.event;

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

    public void registerEvents(Object object) {
        try {
            Method[] methods = object.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                EventHandler handler = method.getAnnotation(EventHandler.class);
                if (handler == null) {
                    continue;
                }
                Class<?>[] types = method.getParameterTypes();
                if (types.length != 1) {
                    System.out.println("Event handlers must only have one parameter. Invalid method " + method.getName());
                    continue;
                }
                method.setAccessible(true);
                this.getSet(types[0]).add(new Pair<>(object, method));
            }
        } catch (Exception e) {
            System.out.println("Exception registering " + object.getClass().getSimpleName() + ":");
            e.printStackTrace();
        }
    }

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