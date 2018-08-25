/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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

import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.subscription.SubscriptionContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.helper.ActorMessageEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

/**
 * Only get messages sent by self.
 */
@Filter(EchoMessage.Processor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EchoMessage {
    /**
     * {@inheritDoc}
     */
    class Processor implements FilterProcessor<ActorMessageEvent<? extends Actor>, EchoMessage>, IMessageFilter<ActorMessageEvent<? extends Actor>> {
        /**
         * Constructs the processor.
         */
        public Processor() {
        }

        @Override
        public boolean accepts(@NonNull ActorMessageEvent<? extends Actor> event, @NonNull EchoMessage[] annotations) {
            Optional<User> client = event.getClient().getUser();
            return client.isPresent() && client.get().equals(event.getActor());
        }

        @Override
        public boolean accepts(ActorMessageEvent<? extends Actor> event, SubscriptionContext context) {
            return this.accepts(event, context.getHandler().getMethod().getAnnotationsByType(EchoMessage.class));
        }
    }
}
