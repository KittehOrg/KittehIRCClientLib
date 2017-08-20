/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
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

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.bus.error.MessageBusException;
import net.engio.mbassy.dispatch.DelegatingMessageDispatcher;
import net.engio.mbassy.dispatch.IHandlerInvocation;
import net.engio.mbassy.dispatch.IMessageDispatcher;
import net.engio.mbassy.subscription.SubscriptionContext;
import net.engio.mbassy.subscription.SubscriptionFactory;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
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
    public FilteringSubscriptionFactory(@Nonnull Map<Class<? extends Annotation>, FilterProcessor<?, ? extends Annotation>> filters) {
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
            dispatcher = new FilteredMessageDispatcher(dispatcher, filterWrappers.toArray(new FilterProcessorWrapper[filterWrappers.size()]));
        }
        return dispatcher;
    }

    private final class FilteredMessageDispatcher extends DelegatingMessageDispatcher {
        private final FilterProcessorWrapper[] filters;

        private FilteredMessageDispatcher(IMessageDispatcher dispatcher, FilterProcessorWrapper[] filters) {
            super(dispatcher);
            this.filters = filters;
        }

        @Override
        public void dispatch(MessagePublication publication, Object message, Iterable listeners) {
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
