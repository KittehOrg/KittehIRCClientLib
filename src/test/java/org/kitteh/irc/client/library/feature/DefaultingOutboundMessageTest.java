package org.kitteh.irc.client.library.feature;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Tests the OutboundMessages system.
 */
public class DefaultingOutboundMessageTest {
    @Test(expected = IllegalArgumentException.class)
    public void testCannotCreateDefaultQuotedString() {
        new SimpleDefaultingOutboundMessageMap("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotSetQuotedString() {
        new SimpleDefaultingOutboundMessageMap("foo")
            .setDefault(DefaultingOutboundMessage.QUIT, "");
    }

    @Test
    public void testSetKeyReturnsSetValue() {
        SimpleDefaultingOutboundMessageMap defaults = new SimpleDefaultingOutboundMessageMap("foo")
            .setDefault(DefaultingOutboundMessage.QUIT, "My kittens bring all the boys to the yard");

        Assert.assertEquals("My kittens bring all the boys to the yard", defaults.getDefault(DefaultingOutboundMessage.QUIT).orElse(null));
    }

    @Test
    public void testDefaultValueInGetDefaults() {
        SimpleDefaultingOutboundMessageMap defaults = new SimpleDefaultingOutboundMessageMap("foo")
                .setDefault(DefaultingOutboundMessage.QUIT, "bar");

        Map<DefaultingOutboundMessage, String> outboundMessages = defaults.getDefaults();

        int foos = 0, bars = 0, unknowns = 0;
        for (String message : outboundMessages.values()) {
            switch (message) {
                case "bar":
                    bars++;
                    break;
                case "foo":
                    foos++;
                    break;
                default:
                    unknowns++;
                    break;
            }
        }

        Assert.assertEquals(DefaultingOutboundMessage.values().length - 1, foos);
        Assert.assertEquals(1, bars);
        Assert.assertEquals(0, unknowns);
    }

    @Test
    public void testCanReSetSetting() {
        SimpleDefaultingOutboundMessageMap defaults = new SimpleDefaultingOutboundMessageMap("foo")
                .setDefault(DefaultingOutboundMessage.QUIT, "stuff")
                .setDefault(DefaultingOutboundMessage.QUIT, "otherStuff");

        Assert.assertEquals("otherStuff", defaults.getDefault(DefaultingOutboundMessage.QUIT).orElse(null));
    }

    @Test
    public void testGetDefaultWithIfUnsetParameter() {
        SimpleDefaultingOutboundMessageMap defaults = new SimpleDefaultingOutboundMessageMap("foo")
            .setDefault(DefaultingOutboundMessage.QUIT, "kittens");

        Assert.assertEquals("kittens", defaults.getDefault(DefaultingOutboundMessage.QUIT, "set").orElse(null));
        Assert.assertEquals("unset", defaults.getDefault(DefaultingOutboundMessage.KICK, "unset").orElse(null));
    }
}
