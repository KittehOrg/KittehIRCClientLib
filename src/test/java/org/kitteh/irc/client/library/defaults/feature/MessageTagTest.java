package org.kitteh.irc.client.library.defaults.feature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.FakeClient;
import org.kitteh.irc.client.library.element.MessageTag;

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
