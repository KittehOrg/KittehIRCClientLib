package org.kitteh.irc.client.library.feature;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Tests the DefaultMessage system.
 */
public class DefaultMessageTest {
    @Test
    public void testSetKeyReturnsSetValue() {
        SimpleDefaultMessageMap defaults = new SimpleDefaultMessageMap("foo")
            .setDefault(DefaultMessage.QUIT, "My kittens bring all the boys to the yard");

        Assert.assertEquals("My kittens bring all the boys to the yard", defaults.getDefault(DefaultMessage.QUIT).orElse(null));
    }

    @Test
    public void testDefaultValueInGetDefaults() {
        SimpleDefaultMessageMap defaults = new SimpleDefaultMessageMap("foo")
                .setDefault(DefaultMessage.QUIT, "bar");

        Map<DefaultMessage, String> defaultMessages = defaults.getDefaults();

        int foos = 0, bars = 0, unknowns = 0;
        for (String message : defaultMessages.values()) {
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

        Assert.assertEquals(DefaultMessage.values().length - 1, foos);
        Assert.assertEquals(1, bars);
        Assert.assertEquals(0, unknowns);
    }

    @Test
    public void testCanReSetSetting() {
        SimpleDefaultMessageMap defaults = new SimpleDefaultMessageMap("foo")
                .setDefault(DefaultMessage.QUIT, "stuff")
                .setDefault(DefaultMessage.QUIT, "otherStuff");

        Assert.assertEquals("otherStuff", defaults.getDefault(DefaultMessage.QUIT).orElse(null));
    }

    @Test
    public void testGetDefaultWithIfUnsetParameter() {
        SimpleDefaultMessageMap defaults = new SimpleDefaultMessageMap("foo")
            .setDefault(DefaultMessage.QUIT, "kittens");

        Assert.assertEquals("kittens", defaults.getDefault(DefaultMessage.QUIT, "set").orElse(null));
        Assert.assertEquals("unset", defaults.getDefault(DefaultMessage.KICK, "unset").orElse(null));
    }
}
