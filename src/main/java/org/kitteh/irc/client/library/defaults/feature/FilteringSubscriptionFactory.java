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

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.MessageBusException;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.dispatch.DelegatingMessageDispatcher;
import net.engio.mbassy.dispatch.HandlerInvocation;
import net.engio.mbassy.dispatch.IHandlerInvocation;
import net.engio.mbassy.dispatch.IMessageDispatcher;
import net.engio.mbassy.dispatch.ReflectiveHandlerInvocation;
import net.engio.mbassy.subscription.SubscriptionContext;
import net.engio.mbassy.subscription.SubscriptionFactory;
import org.jspecify.annotations.NonNull;
import org.kitteh.irc.client.library.feature.filter.FilterProcessor;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A filtering factory for filters.
 */
public class FilteringSubscriptionFactory extends SubscriptionFactory {
    private final Map<Class<? extends Annotation>, FilterProcessor<?, ? extends Annotation>> filters;

    /**
     * Constructs the filter factory.
     *
     * @param filters the filters
     */
    public FilteringSubscriptionFactory(@NonNull Map<Class<? extends Annotation>, FilterProcessor<?, ? extends Annotation>> filters) {
        this.filters = Sanity.nullCheck(filters, "filters");
    }

    @Override
    protected IMessageDispatcher buildDispatcher(SubscriptionContext context, IHandlerInvocation invocation) throws MessageBusException {
        IMessageDispatcher dispatcher = super.buildDispatcher(context, invocation);
        List<FilterProcessorWrapper> filterWrappers = new ArrayList<>();
        for (Map.Entry<Class<? extends Annotation>, FilterProcessor<?, ? extends Annotation>> entry : this.filters.entrySet()) {
            Annotation[] annotations = context.getHandler().getMethod().getAnnotationsByType(entry.getKey());
            if (annotations.length > 0) {
                filterWrappers.add(new FilterProcessorWrapper(entry.getValue(), annotations));
            }
        }
        if (!filterWrappers.isEmpty()) {
            dispatcher = new FilteredMessageDispatcher(dispatcher, filterWrappers.toArray(new FilterProcessorWrapper[0]));
        }
        return dispatcher;
    }

    @Override
    protected IHandlerInvocation createBaseHandlerInvocation(SubscriptionContext context) throws MessageBusException {
        if (context.getHandler().getHandlerInvocation() != ReflectiveHandlerInvocation.class) {
            return super.createBaseHandlerInvocation(context);
        }
        return new MethodHandleInvocation(context);
    }

    private static final class FilteredMessageDispatcher extends DelegatingMessageDispatcher {
        private final FilterProcessorWrapper[] filters;

        private FilteredMessageDispatcher(IMessageDispatcher dispatcher, FilterProcessorWrapper[] filters) {
            super(dispatcher);
            this.filters = filters;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void dispatch(MessagePublication publication, Object message, Iterable listeners) {
            for (FilterProcessorWrapper filter : this.filters) {
                if (!filter.filterProcessor.accepts(message, filter.annotations)) {
                    return;
                }
            }
            this.getDelegate().dispatch(publication, message, listeners);
        }
    }

    private record FilterProcessorWrapper(FilterProcessor filterProcessor, Annotation[] annotations) {
    }

    /*
     * Method handle invocation, courtesy of @akardapolov (licensed MIT, contribution to unreleased mbassador update)
     * https://github.com/bennidi/mbassador/commit/bb4adec460e1c5bf55b5bca6323425242bbef334
     *
     * Tweaked for my usage
     */
    public static class MethodHandleInvocation extends HandlerInvocation {
        private static final MethodType INVOCATION_TYPE = MethodType.methodType(void.class, Object.class, Object.class);

        private final MethodHandle handle;

        public MethodHandleInvocation(SubscriptionContext context) {
            super(context);
            if (context.getHandler() instanceof MethodHandleMetadataReader.MethodHandleMessageHandler handler) {
                this.handle = handler.getMethodHandle();
            } else {
                Method method = context.getHandler().getMethod();
                try {
                    this.handle = MethodHandles.lookup().unreflect(method).asType(INVOCATION_TYPE);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Could not create MethodHandle for handler: " + method.getName(), e);
                }
            }
        }

        @Override
        public void invoke(final Object listener, final Object message, MessagePublication publication) {
            try {
                this.handle.invokeExact(listener, message);
            } catch (Throwable e) {
                this.handleError(publication, e, listener);
            }
        }

        private void handleError(MessagePublication publication, Throwable e, Object listener) {
            PublicationError error = new PublicationError(
                    e,
                    "Error during invocation of message handler via MethodHandle.",
                    this.getContext().getHandler().getMethod(),
                    listener,
                    publication
            );

            publication.markError(error);

            Collection<IPublicationErrorHandler> errorHandlers =
                    this.getContext().getRuntime().get(IBusConfiguration.Properties.PublicationErrorHandlers);

            if (errorHandlers != null) {
                for (IPublicationErrorHandler handler : errorHandlers) {
                    handler.handleError(error);
                }
            }
        }
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).toString();
    }
}
