package org.kitteh.irc.client.library.defaults.feature;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.FakeClient;
import org.kitteh.irc.client.library.defaults.element.DefaultActor;
import org.kitteh.irc.client.library.defaults.element.DefaultServerMessage;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.feature.filter.FilterProcessor;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A test that ensures filtering works without explicitly registering filters.
 */
public class CustomEventManagerTest {
    private class StrippedEventManager implements EventManager {
        private final MBassador<Object> bus = new MBassador<>();

        @Override
        public void callEvent(@NonNull Object event) {
            this.bus.publish(event);
        }

        @Override
        public @NonNull Set<Object> getRegisteredEventListeners() {
            return Collections.emptySet();
        }

        @Override
        public @NonNull Map<Class<? extends Annotation>, FilterProcessor<?, ? extends Annotation>> getAnnotationFilters() {
            return Collections.emptyMap();
        }

        @Override
        public <A extends Annotation> void registerAnnotationFilter(Class<A> annotationClass, FilterProcessor<?, A> filterProcessor) {
            // NOOP
        }

        @Override
        public void registerEventListener(@NonNull Object listener) {
            this.bus.subscribe(listener);
        }

        @Override
        public void unregisterEventListener(@NonNull Object listener) {
            this.bus.unsubscribe(listener);
        }
    }

    private static final AtomicInteger unfilteredNumericCount = new AtomicInteger();
    private static final AtomicInteger filteredNumericCount = new AtomicInteger();

    /**
     * Tests filtering.
     */
    @Test
    public void testStrippedDownFiltering() {
        final FakeClient client = new FakeClient();
        final Actor actor = new DefaultActor(client, "test");
        final StrippedEventManager em = new StrippedEventManager();
        em.registerEventListener(this);
        em.callEvent(new ClientReceiveNumericEvent(client, new DefaultServerMessage.NumericCommand(200, "", Collections.emptyList()), actor, "", 200, Collections.emptyList()));
        em.callEvent(new ClientReceiveNumericEvent(client, new DefaultServerMessage.NumericCommand(300, "", Collections.emptyList()), actor, "", 300, Collections.emptyList()));
        Assertions.assertEquals(2, unfilteredNumericCount.get());
        Assertions.assertEquals(1, filteredNumericCount.get());
    }

    @Handler
    private void unfilteredNumeric(final ClientReceiveNumericEvent event) {
        unfilteredNumericCount.getAndIncrement();
    }

    @Handler
    @NumericFilter(200)
    private void filteredNumeric(final ClientReceiveNumericEvent event) {
        filteredNumericCount.getAndIncrement();
    }
}
