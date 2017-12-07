package org.kitteh.irc.client.library.implementation;

import org.junit.Assert;
import org.junit.Test;
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
        Assert.assertTrue("Failed to process multiple tags", tags.size() == 3);
        Assert.assertEquals("Failed to process valid tag name", tags.get(0).getName(), "aaa");
        Assert.assertEquals("Failed to process valid tag value", tags.get(0).getValue().get(), "bbb");
        Assert.assertEquals("Failed to process valid tag name", tags.get(1).getName(), "ccc");
        Assert.assertTrue("Failed to process lack of tag value", !tags.get(1).getValue().isPresent());
        Assert.assertEquals("Failed to process valid tag name", tags.get(2).getName(), "example.com/ddd");
        Assert.assertEquals("Failed to process valid tag value", tags.get(2).getValue().get(), "eee");
    }

    private static final String TIME = "2012-06-30T23:59:60.419Z";

    /**
     * Tests processing of the time tag.
     */
    @Test
    public void timeTag() {
        List<MessageTag> tags = new FakeClient().getMessageTagManager().getCapabilityTags("time=" + TIME);
        Assert.assertTrue("Failed to process time tag", tags.size() == 1);
        Assert.assertTrue("Failed to process time tag as MessageTag.Time", tags.get(0) instanceof MessageTag.Time);
        Assert.assertEquals("Failed to process time tag", ((MessageTag.Time) tags.get(0)).getTime(), Instant.parse(TIME));
    }
}
