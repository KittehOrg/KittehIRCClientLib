/*
 * * Copyright (C) 2013-2025 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a specific command to listen to for the {@link
 * ClientReceiveCommandEvent}. This annotation can be repeated.
 * <p>
 * The below code only listens to PRIVMSG:
 * <pre>
 *     {@code @CommandFilter("PRIVMSG")}
 *     {@code @Handler)}
 *     public void privmsg(ClientReceiveCommandEvent event) {
 *         System.out.println("We get signal");
 *     }
 * </pre>
 */
@Filter(CommandFilter.Processor.class)
@Repeatable(CommandFilter.Commands.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandFilter {
    /**
     * Processes this annotation-based filter.
     */
    class Processor implements FilterProcessor<ClientReceiveCommandEvent, CommandFilter>, IMessageFilter<ClientReceiveCommandEvent> {
        /**
         * Constructs the processor.
         */
        public Processor() {
        }

        @Override
        public boolean accepts(@NonNull ClientReceiveCommandEvent event, @NonNull CommandFilter[] commandFilters) {
            for (CommandFilter commandFilter : commandFilters) {
                if (commandFilter.value().equalsIgnoreCase(event.getCommand())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean accepts(ClientReceiveCommandEvent event, SubscriptionContext context) {
            return this.accepts(event, context.getHandler().getMethod().getAnnotationsByType(CommandFilter.class));
        }
    }

    /**
     * This is an annotation for storing repeated CommandFilter annotations.
     * Just use the CommandFilter annotation instead, multiple times!
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Commands {
        /**
         * Gets the stored annotations.
         *
         * @return stored annotations
         */
        @NonNull CommandFilter[] value();
    }

    /**
     * The command to listen to.
     *
     * @return the command
     */
    @NonNull String value();
}
