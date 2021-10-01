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
        Assertions.assertEquals(tags.get(0).getName(), "aaa", "Failed to process valid tag name");
        Assertions.assertEquals(tags.get(0).getValue().get(), "bbb", "Failed to process valid tag value");
        Assertions.assertEquals(tags.get(1).getName(), "ccc", "Failed to process valid tag name");
        Assertions.assertTrue(!tags.get(1).getValue().isPresent(), "Failed to process lack of tag value");
        Assertions.assertEquals(tags.get(2).getName(), "example.com/ddd", "Failed to process valid tag name");
        Assertions.assertEquals(tags.get(2).getValue().get(), "eee", "Failed to process valid tag value");
    }

    private static final String TIME = "2012-06-30T23:59:60.419Z";

    /**
     * Tests processing of the time tag.
     */
    @Test
    public void timeTag() {
        List<MessageTag> tags = new FakeClient().getMessageTagManager().getCapabilityTags("time=" + TIME);
        Assertions.assertEquals(1, tags.size(), "Failed to process time tag");
        Assertions.assertTrue(tags.get(0) instanceof MessageTag.Time, "Failed to process time tag as MessageTag.Time");
        Assertions.assertEquals(((MessageTag.Time) tags.get(0)).getTime(), Instant.parse(TIME), "Failed to process time tag");
    }
}
