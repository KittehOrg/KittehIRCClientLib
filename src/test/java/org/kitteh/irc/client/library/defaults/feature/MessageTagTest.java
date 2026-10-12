package org.kitteh.irc.client.library.defaults.feature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.FakeClient;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.feature.MessageTagManager;

import java.time.Instant;
import java.util.List;

/**
 * Test out message tag processing
 */
public class MessageTagTest {
    /**
     * Tests a multi-tag environment.
     */
    @Test
    public void multiTag() {
        List<MessageTag> tags = new FakeClient().getMessageTagManager().getCapabilityTags("aaa=bbb;ccc;example.com/ddd=eee");
        Assertions.assertEquals(3, tags.size(), "Failed to process multiple tags");
        Assertions.assertEquals("aaa", tags.get(0).getName(), "Failed to process valid tag name");
        Assertions.assertEquals("bbb", tags.get(0).getValue().orElse(null), "Failed to process valid tag value");
        Assertions.assertEquals("ccc", tags.get(1).getName(), "Failed to process valid tag name");
        Assertions.assertTrue(tags.get(1).getValue().isEmpty(), "Failed to process lack of tag value");
        Assertions.assertEquals("example.com/ddd", tags.get(2).getName(), "Failed to process valid tag name");
        Assertions.assertEquals("eee", tags.get(2).getValue().orElse(null), "Failed to process valid tag value");
    }

    /**
     * Tests unescaping of tag values.
     */
    @Test
    public void escapedValues() {
        List<MessageTag> tags = new FakeClient().getMessageTagManager().getCapabilityTags("a=x\\ny\\rz;b=c\\d;c=e\\;d=f\\:g\\sh\\\\i;e=\\\\n");
        Assertions.assertEquals(5, tags.size(), "Failed to process escaped tags");
        Assertions.assertEquals("x\ny\rz", tags.get(0).getValue().orElse(null), "Failed to unescape CR/LF");
        Assertions.assertEquals("cd", tags.get(1).getValue().orElse(null), "Failed to drop backslash of invalid escape");
        Assertions.assertEquals("e", tags.get(2).getValue().orElse(null), "Failed to drop trailing backslash");
        Assertions.assertEquals("f;g h\\i", tags.get(3).getValue().orElse(null), "Failed to unescape semicolon/space/backslash");
        Assertions.assertEquals("\\n", tags.get(4).getValue().orElse(null), "Failed to unescape backslash before n");
    }

    /**
     * Tests the escaping example from the IRCv3 message tags spec, and empty values.
     */
    @Test
    public void specExampleAndEmptyValues() {
        List<MessageTag> tags = new FakeClient().getMessageTagManager().getCapabilityTags("+example=raw+:=,escaped\\:\\s\\\\;a=;b=\\");
        Assertions.assertEquals(3, tags.size(), "Failed to process tags");
        Assertions.assertEquals("raw+:=,escaped; \\", tags.get(0).getValue().orElse(null), "Failed to unescape spec example");
        Assertions.assertTrue(tags.get(1).getValue().isEmpty(), "Failed to treat empty value as missing");
        Assertions.assertTrue(tags.get(2).getValue().isEmpty(), "Failed to treat value unescaping to empty as missing");
    }

    /**
     * Tests tag name validation for outgoing tags.
     */
    @Test
    public void validNames() {
        for (String name : new String[]{"msgid", "a", "-", "+typing", "+draft/reply", "example.com/foo-bar", "+xn--e1afmkfd.org/foo", "a.b-c/D9"}) {
            Assertions.assertTrue(MessageTag.isValidName(name), "Should accept name: " + name);
        }
        for (String name : new String[]{"", "+", "++a", "my tag", "a;b", "a=b", "a_b", "a/", "/a", "+/a", "a/b/c", "a/b.c", "é"}) {
            Assertions.assertFalse(MessageTag.isValidName(name), "Should reject name: " + name);
        }
    }

    /**
     * Tests that entries without a key are skipped.
     */
    @Test
    public void emptyKeys() {
        List<MessageTag> tags = new FakeClient().getMessageTagManager().getCapabilityTags("a;;=b;c");
        Assertions.assertEquals(2, tags.size(), "Failed to skip entries without a key");
        Assertions.assertEquals("a", tags.get(0).getName());
        Assertions.assertEquals("c", tags.get(1).getName());
    }

    /**
     * Tests that escaping a value and unescaping it gives back the original.
     */
    @Test
    public void escapeRoundTrip() {
        String raw = "a;b c\\d\re\nf\\:g\\s";
        MessageTag outgoing = new MessageTagManager.DefaultMessageTag("+example", raw);
        List<MessageTag> tags = new FakeClient().getMessageTagManager().getCapabilityTags(outgoing.getAsString());
        Assertions.assertEquals(1, tags.size());
        Assertions.assertEquals(raw, tags.getFirst().getValue().orElse(null), "Failed to round-trip escaped value");
    }

    /**
     * Tests that tag keys are case-sensitive, and that the final duplicate wins.
     */
    @Test
    public void caseSensitiveKeys() {
        List<MessageTag> tags = new FakeClient().getMessageTagManager().getCapabilityTags("Key=1;key=2;Key=3");
        Assertions.assertEquals(2, tags.size(), "Failed to keep case-distinct keys");
        Assertions.assertEquals("Key", tags.get(0).getName());
        Assertions.assertEquals("3", tags.get(0).getValue().orElse(null), "Failed to keep final duplicate");
        Assertions.assertEquals("key", tags.get(1).getName());
        Assertions.assertEquals("2", tags.get(1).getValue().orElse(null));
    }

    private static final String TIME = "2012-06-30T23:59:60.419Z";

    /**
     * Tests processing of the time tag.
     */
    @Test
    public void timeTag() {
        List<MessageTag> tags = new FakeClient().getMessageTagManager().getCapabilityTags("time=" + TIME);
        Assertions.assertEquals(1, tags.size(), "Failed to process time tag");
        Assertions.assertInstanceOf(MessageTag.Time.class, tags.getFirst(), "Failed to process time tag as MessageTag.Time");
        Assertions.assertEquals(((MessageTag.Time) tags.getFirst()).getTime(), Instant.parse(TIME), "Failed to process time tag");
    }
}
