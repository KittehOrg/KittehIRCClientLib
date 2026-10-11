/*
 * * Copyright (C) 2013-2026 Matt Baxter https://kitteh.org
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

import net.engio.mbassy.common.ReflectionUtils;
import net.engio.mbassy.dispatch.el.ElFilter;
import net.engio.mbassy.listener.Enveloped;
import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.IncludeFilters;
import net.engio.mbassy.listener.MessageHandler;
import net.engio.mbassy.listener.MessageListener;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.listener.Synchronized;
import net.engio.mbassy.subscription.MessageEnvelope;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This hacky class combines some mbassador code with some modifications to make it nicer for KICL to be a module.
 */
class MethodHandleMetadataReader extends MetadataReader {
    public static class MethodHandleMessageHandler extends MessageHandler {
        private final MethodHandle methodHandle;

        private MethodHandleMessageHandler(@NonNull Map<String, Object> properties, @NonNull MethodHandle methodHandle) {
            super(properties);
            this.methodHandle = methodHandle;
        }

        public @NonNull MethodHandle getMethodHandle() {
            return this.methodHandle;
        }
    }

    private static final MethodType INVOCATION_TYPE = MethodType.methodType(void.class, Object.class, Object.class);

    private final Map<Class<?>, IMessageFilter<?>> filterCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, MethodHandles.Lookup> lookups = new ConcurrentHashMap<>();

    /**
     * Supplies a lookup to use when reading the given listener class.
     * MBassador reads a class's metadata only on its first subscription,
     * so register before subscribing and remove afterward.
     *
     * @param listenerClass listener class
     * @param lookup lookup with full privilege access in the listener's module
     */
    public void registerLookup(@NonNull Class<?> listenerClass, MethodHandles.@NonNull Lookup lookup) {
        this.lookups.put(Sanity.nullCheck(listenerClass, "listenerClass"), Sanity.nullCheck(lookup, "lookup"));
    }

    /**
     * Removes a lookup supplied by {@link #registerLookup(Class, MethodHandles.Lookup)}.
     *
     * @param listenerClass listener class
     */
    public void removeLookup(@NonNull Class<?> listenerClass) {
        this.lookups.remove(Sanity.nullCheck(listenerClass, "listenerClass"));
    }

    // Super method with tweaks
    @Override
    public MessageListener getMessageListener(Class target) {
        MethodHandles.Lookup supplied = this.lookups.get(target);
        MessageListener listenerMetadata = new MessageListener(target);
        Method[] allHandlers = ReflectionUtils.getMethods(method -> ReflectionUtils.getAnnotation(method, Handler.class) != null, target);
        for (Method handler : allHandlers) {
            if (!ReflectionUtils.containsOverridingMethod(allHandlers, handler)) {
                Handler handlerConfig = ReflectionUtils.getAnnotation(handler, Handler.class);
                if (!handlerConfig.enabled() || !isValidMessageHandler(handler)) {
                    continue;
                }

                Method overriddenHandler = ReflectionUtils.getOverridingMethod(handler, target);
                Method method = (overriddenHandler == null) ? handler : overriddenHandler;
                MethodHandle methodHandle = resolve(method, supplied).asType(INVOCATION_TYPE);
                Map<String, Object> handlerProperties = createProperties(method, handlerConfig, this.getFilters(handler, handlerConfig, supplied), listenerMetadata);
                listenerMetadata.addHandler(new MethodHandleMessageHandler(handlerProperties, methodHandle));
            }
        }
        return listenerMetadata;
    }

    // MessageHandler.Properties.Create, minus setAccessible.
    private static Map<String, Object> createProperties(Method handler, Handler handlerConfig, IMessageFilter<?>[] filter, MessageListener listenerConfig) {
        Enveloped enveloped = ReflectionUtils.getAnnotation( handler, Enveloped.class );
        if(!handlerConfig.condition().isEmpty()){
            if (!ElFilter.isELAvailable()) {
                throw new IllegalStateException("A handler uses an EL filter but no EL implementation is available.");
            }

            IMessageFilter<?>[] expandedFilter = new IMessageFilter[filter.length + 1];
            System.arraycopy(filter, 0, expandedFilter, 0, filter.length);
            expandedFilter[filter.length] = new ElFilter();
            filter = expandedFilter;
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put(MessageHandler.Properties.HandlerMethod, handler);
        properties.put(MessageHandler.Properties.Filter, filter);
        properties.put(MessageHandler.Properties.Condition, cleanEL(handlerConfig.condition()));
        properties.put(MessageHandler.Properties.Priority, handlerConfig.priority());
        properties.put(MessageHandler.Properties.Invocation, handlerConfig.invocation());
        properties.put(MessageHandler.Properties.InvocationMode, handlerConfig.delivery());
        properties.put(MessageHandler.Properties.Enveloped, enveloped != null);
        properties.put(MessageHandler.Properties.AcceptSubtypes, !handlerConfig.rejectSubtypes());
        properties.put(MessageHandler.Properties.Listener, listenerConfig);
        properties.put(MessageHandler.Properties.IsSynchronized, ReflectionUtils.getAnnotation( handler, Synchronized.class) != null);
        properties.put(MessageHandler.Properties.HandledMessages, (enveloped != null ? enveloped.messages() : handler.getParameterTypes()));
        return properties;
    }

    // MessageHandler.Properties.cleanEL
    private static String cleanEL(String expression) {
        String trimmed = expression.trim();
        return (trimmed.startsWith("${") || trimmed.startsWith("#{")) ? expression : ("${" + expression + '}');
    }

    // Same rules as MetadataReader, but invalid handlers are an error rather than a println.
    private static boolean isValidMessageHandler(Method handler) {
        if (handler.getParameterCount() != 1) {
            throw new IllegalArgumentException("Handler " + handler + " must have exactly one parameter");
        }
        Enveloped enveloped = ReflectionUtils.getAnnotation(handler, Enveloped.class);
        if (enveloped != null) {
            if (!MessageEnvelope.class.isAssignableFrom(handler.getParameterTypes()[0])) {
                throw new IllegalArgumentException("Handler " + handler + " is @Enveloped but does not take a MessageEnvelope");
            }
            if (enveloped.messages().length == 0) {
                throw new IllegalArgumentException("Handler " + handler + " is @Enveloped but declares no message types");
            }
        }
        return true;
    }

    private IMessageFilter<?>[] getFilters(Method handler, Handler config, MethodHandles.@Nullable Lookup supplied) {
        List<Filter> filters = new ArrayList<>();
        Collections.addAll(filters, config.filters());
        for (Annotation annotation : handler.getAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();
            IncludeFilters include = type.getAnnotation(IncludeFilters.class);
            if (include != null) {
                Collections.addAll(filters, include.value());
            }
            Filter filter = type.getAnnotation(Filter.class);
            if (filter != null) {
                filters.add(filter);
            }
        }
        IMessageFilter<?>[] result = new IMessageFilter<?>[filters.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.filterCache.computeIfAbsent(filters.get(i).value(), filterClass -> instantiate(filterClass, supplied));
        }
        return result;
    }

    private static IMessageFilter<?> instantiate(Class<?> filterClass, MethodHandles.@Nullable Lookup supplied) {
        try {
            MethodType type = MethodType.methodType(void.class);
            MethodHandle constructor;
            try {
                constructor = privateLookup(filterClass, supplied).findConstructor(filterClass, type);
            } catch (IllegalAccessException exception) {
                constructor = MethodHandles.lookup().findConstructor(filterClass, type); // Public, exported, not opened
            }
            return (IMessageFilter<?>) constructor.invoke();
        } catch (Throwable thrown) {
            throw new IllegalArgumentException("Could not instantiate filter " + filterClass.getName() + accessHint(filterClass), thrown);
        }
    }

    private static MethodHandle resolve(Method method, MethodHandles.@Nullable Lookup supplied) {
        try {
            return privateLookup(method.getDeclaringClass(), supplied).unreflect(method);
        } catch (IllegalAccessException exception) {
            try {
                return MethodHandles.lookup().unreflect(method); // Public, exported, not opened
            } catch (IllegalAccessException ignored) {
                // Report the private lookup failure below
            }
            throw new IllegalArgumentException("Cannot access handler " + method + accessHint(method.getDeclaringClass()), exception);
        }
    }

    private static MethodHandles.Lookup privateLookup(Class<?> target, MethodHandles.@Nullable Lookup supplied) throws IllegalAccessException {
        MethodHandles.Lookup base = (supplied != null) ? supplied : MethodHandles.lookup();
        // Named modules don't read the unnamed module (classpath) by default
        MethodHandleMetadataReader.class.getModule().addReads(target.getModule());
        return MethodHandles.privateLookupIn(target, base);
    }

    private static String accessHint(Class<?> target) {
        Module module = target.getModule();
        if (!module.isNamed()) {
            return "";
        }
        return ". Either open package " + target.getPackageName() + " to module " + MethodHandleMetadataReader.class.getModule().getName() +
                " or register the listener with a MethodHandles.Lookup from module " + module.getName();
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).toString();
    }
}
