package org.kitteh.irc.client.library.defaults;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;

import java.util.Set;

/**
 * Tests the intended channels snapshot.
 */
public class IntendedChannelsTest {
    private Client.WithManagement client;

    @BeforeEach
    public void setUp() {
        this.client = (Client.WithManagement) Client.builder().build();
    }

    @AfterEach
    public void tearDown() {
        this.client.shutdown();
    }

    /**
     * Tests that the snapshot matches per the casemapping, keeps the
     * requested spelling, and is a snapshot.
     */
    @Test
    public void testCaseInsensitiveSnapshot() {
        this.client.addChannel("#Kitteh[]");
        Set<String> intended = this.client.getIntendedChannels();
        Assertions.assertTrue(intended.contains("#kitteh[]"));
        // Default RFC1459 casemapping
        Assertions.assertTrue(intended.contains("#KITTEH{}"));
        Assertions.assertEquals(Set.of("#Kitteh[]"), Set.copyOf(intended));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> intended.add("#meow"));
        this.client.removeChannel("#kitteh[]");
        Assertions.assertTrue(intended.contains("#kitteh[]"));
        Assertions.assertTrue(this.client.getIntendedChannels().isEmpty());
    }
}
