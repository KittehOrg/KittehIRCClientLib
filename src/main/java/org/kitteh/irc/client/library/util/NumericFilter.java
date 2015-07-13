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
package org.kitteh.irc.client.library.util;

import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.subscription.SubscriptionContext;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a specific numeric to listen to for the {@link
 * ClientReceiveNumericEvent}. This annotation can be repeated.
 *
 * The below code only listens to numeric 1:
 * <pre>
 *     {@code @NumericFilter(1)}
 *     {@code @Handler(filters = @Filter(NumericFilter.Filter.class))}
 *     public void numeric1(ClientReceiveNumericEvent event) {
 *         this.currentNick = event.getArgs()[0];
 *     }
 * </pre>
 */
@Repeatable(NumericFilter.Numerics.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NumericFilter {
    /**
     * A Filter of numerics.
     */
    class Filter implements IMessageFilter<ClientReceiveNumericEvent> {
        @Override
        public boolean accepts(ClientReceiveNumericEvent event, SubscriptionContext subscriptionContext) {
            NumericFilter[] numericFilters = subscriptionContext.getHandler().getMethod().getAnnotationsByType(NumericFilter.class);
            for (NumericFilter numericFilter : numericFilters) {
                if (numericFilter.value() == event.getNumeric()) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * This is an annotation for storing repeated Numeric annotations. Just
     * use the Numeric annotation instead, multiple times!
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Numerics {
        /**
         * Gets the stored annotations.
         *
         * @return stored annotations
         */
        NumericFilter[] value();
    }

    /**
     * The numeric to listen to.
     *
     * @return the numeric
     */
    int value();
}