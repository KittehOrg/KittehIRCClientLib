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
package org.kitteh.irc.client.library.feature.filter;

import net.engio.mbassy.bus.BusRuntime;
import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.MessageBusException;
import net.engio.mbassy.common.StrongConcurrentSet;
import net.engio.mbassy.common.WeakConcurrentSet;
import net.engio.mbassy.dispatch.DelegatingMessageDispatcher;
import net.engio.mbassy.dispatch.IMessageDispatcher;
import net.engio.mbassy.listener.MessageHandler;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionContext;
import net.engio.mbassy.subscription.SubscriptionFactory;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FilteringSubscriptionFactory extends SubscriptionFactory {
    private static final Constructor<Subscription> SUBSCRIPTION_CONSTRUCTOR;
    private final Map<Class<? extends Annotation>, FilterProcessor<?, ? extends Annotation>> filters;

    public FilteringSubscriptionFactory(@Nonnull Map<Class<? extends Annotation>, FilterProcessor<?, ? extends Annotation>> filters) {
        this.filters = Sanity.nullCheck(filters, "filters");
    }

    static {
        try {
            SUBSCRIPTION_CONSTRUCTOR = Subscription.class.getDeclaredConstructor(SubscriptionContext.class, IMessageDispatcher.class, Collection.class);
            SUBSCRIPTION_CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Cannot initialize subscriptions");
        }
    }

    @Override
    public Subscription createSubscription(BusRuntime runtime, MessageHandler handlerMetadata) throws MessageBusException {
        try {
            SubscriptionContext context = new SubscriptionContext(runtime, handlerMetadata, runtime.get(IBusConfiguration.Properties.PublicationErrorHandlers));
            IMessageDispatcher dispatcher = this.buildDispatcher(context, this.buildInvocationForHandler(context));
            List<FilterProcessorWrapper> filterWrappers = new ArrayList<>();
            for (Map.Entry<Class<? extends Annotation>, FilterProcessor<?, ? extends Annotation>> entry : this.filters.entrySet()) {
                Annotation[] annotations = handlerMetadata.getMethod().getAnnotationsByType(entry.getKey());
                if (annotations.length > 0) {
                    filterWrappers.add(new FilterProcessorWrapper(entry.getValue(), annotations));
                }
            }
            if (!filterWrappers.isEmpty()) {
                dispatcher = new FilteredMessageDispatcher(dispatcher, filterWrappers.toArray(new FilterProcessorWrapper[filterWrappers.size()]));
            }
            return SUBSCRIPTION_CONSTRUCTOR.newInstance(context, dispatcher, handlerMetadata.useStrongReferences() ? new StrongConcurrentSet<>() : new WeakConcurrentSet<>());
        } catch (Exception e) {
            throw new MessageBusException(e);
        }
    }

    private final class FilteredMessageDispatcher extends DelegatingMessageDispatcher {
        private final FilterProcessorWrapper[] filters;

        private FilteredMessageDispatcher(IMessageDispatcher dispatcher, FilterProcessorWrapper[] filters) {
            super(dispatcher);
            this.filters = filters;
        }

        @Override
        public void dispatch(IMessagePublication publication, Object message, Iterable listeners) {
            for (FilterProcessorWrapper filter : this.filters) {
                if (!filter.filterProcessor.accepts(message, filter.annotations)) {
                    return;
                }
            }
            this.getDelegate().dispatch(publication, message, listeners);
        }
    }

    private final class FilterProcessorWrapper {
        private final Annotation[] annotations;
        private final FilterProcessor filterProcessor;

        private FilterProcessorWrapper(FilterProcessor filterProcessor, Annotation[] annotations) {
            this.annotations = annotations;
            this.filterProcessor = filterProcessor;
        }
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).toString();
    }
}
