package org.kitteh.irc.client.library;

import org.junit.jupiter.api.Test;

/**
 * Test building a builder.
 */
public final class DefaultBuilderTest {
    /**
     * Tries to create a builder. Blows up otherwise.
     */
    @Test
    public void builder() {
        try {
            Client.builder();
        } catch (RuntimeException e) {
            throw new AssertionError("Cannot into reflection", e);
        }
    }
}
