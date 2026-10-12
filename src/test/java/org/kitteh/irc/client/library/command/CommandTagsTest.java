package org.kitteh.irc.client.library.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.mockito.Mockito;

/**
 * Tests tags on outgoing commands.
 */
public class CommandTagsTest {
    /**
     * Tests that values are escaped per the IRCv3 message tags spec.
     */
    @Test
    public void testEscaping() {
        Client client = Mockito.mock(Client.class);

        new TagMessageCommand(client).target("#kitteh").tags()
                .add("+example", "raw+:=,escaped; \\")
                .add("+crlf", "a\r\nb")
                .add("+flag")
                .then().execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("@+example=raw+:=,escaped\\:\\s\\\\;+crlf=a\\r\\nb;+flag TAGMSG #kitteh");
    }

    /**
     * Tests valid tag names.
     */
    @Test
    public void testValidNames() {
        Command<?>.Tags tags = new TagMessageCommand(Mockito.mock(Client.class)).tags();
        Assertions.assertDoesNotThrow(() -> tags
                .add("msgid")
                .add("+typing", "active")
                .add("+draft/reply", "abc")
                .add("example.com/foo-bar")
                .add("+xn--e1afmkfd.org/foo"));
    }

    /**
     * Tests invalid tag names.
     */
    @Test
    public void testInvalidNames() {
        Command<?>.Tags tags = new TagMessageCommand(Mockito.mock(Client.class)).tags();
        for (String name : new String[]{"", "+", "my tag", "a;b", "a=b", "a/", "/a", "++a", "a_b"}) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> tags.add(name), "Should reject name: " + name);
            Assertions.assertThrows(IllegalArgumentException.class, () -> tags.add(name, "x"), "Should reject name: " + name);
            Assertions.assertThrows(IllegalArgumentException.class, () -> tags.add(new MessageTagManager.DefaultMessageTag(name, null)), "Should reject name: " + name);
        }
    }
}
