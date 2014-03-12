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

    /**
     * Registers any non-static methods annotated with
     * {@link org.kitteh.irc.event.EventHandler}, provided they have a single
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