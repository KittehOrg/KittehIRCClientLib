package org.kitteh.irc.client.library.defaults;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;

/**
 * Tests the outgoing line length limits: 4094 bytes of tag data, and 510
 * bytes (512 with CR-LF) for the rest of the line.
 */
public class SendRawLineLengthTest {
    private Client client;

    @BeforeEach
    public void setUp() {
        this.client = Client.builder().build();
    }

    @AfterEach
    public void tearDown() {
        this.client.shutdown();
    }

    private static String body(int bytes) {
        String prefix = "PRIVMSG #kitteh :";
        return prefix + "a".repeat(bytes - prefix.length());
    }

    private static String tags(int bytes) {
        return "+example=" + "a".repeat(bytes - "+example=".length());
    }

    @Test
    public void testUntagged() {
        Assertions.assertDoesNotThrow(() -> this.client.sendRawLine(body(510)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.client.sendRawLine(body(511)));
    }

    @Test
    public void testUntaggedCountsBytes() {
        // 3 bytes per character in UTF-8, so the limit is reached well before 510 chars
        String prefix = "PRIVMSG #kitteh :";
        int fit = (510 - prefix.length()) / 3;
        Assertions.assertDoesNotThrow(() -> this.client.sendRawLine(prefix + "中".repeat(fit)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.client.sendRawLine(prefix + "中".repeat(fit + 1)));
    }

    @Test
    public void testTagged() {
        Assertions.assertDoesNotThrow(() -> this.client.sendRawLine('@' + tags(4094) + ' ' + body(510)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.client.sendRawLine('@' + tags(4095) + ' ' + body(30)));
    }

    @Test
    public void testTaggedBodyLimitIndependentOfTags() {
        // Short tags don't lend their unused budget to the body
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.client.sendRawLine("@+a=b " + body(511)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.client.sendRawLine("@+a=b " + body(4000)));
    }

    @Test
    public void testTagsWithoutCommand() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.client.sendRawLine("@+a=b"));
    }
}
