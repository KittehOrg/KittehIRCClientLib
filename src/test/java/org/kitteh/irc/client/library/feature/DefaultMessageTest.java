package org.kitteh.irc.client.library.feature;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageType;
import org.kitteh.irc.client.library.feature.defaultmessage.SimpleDefaultMessageMap;

import java.util.Map;

/**
 * Tests the DefaultMessage system.
 */
public class DefaultMessageTest {
    @Test
    public void testSetKeyReturnsSetValue() {
        SimpleDefaultMessageMap defaults = new SimpleDefaultMessageMap("foo")
                .setDefault(DefaultMessageType.QUIT, "My kittens bring all the boys to the yard");

        Assert.assertEquals("My kittens bring all the boys to the yard", defaults.getDefault(DefaultMessageType.QUIT).orElse(null));
    }

    @Test
    public void testDefaultValueInGetDefaults() {
        SimpleDefaultMessageMap defaults = new SimpleDefaultMessageMap("foo")
                .setDefault(DefaultMessageType.QUIT, "bar");

        Map<DefaultMessageType, String> defaultMessages = defaults.getDefaults();

        int foos = 0;
        int bars = 0;
        int unknowns = 0;
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

        Assert.assertEquals(DefaultMessageType.values().length - 1, foos);
        Assert.assertEquals(1, bars);
        Assert.assertEquals(0, unknowns);
    }

    @Test
    public void testCanReSetSetting() {
        SimpleDefaultMessageMap defaults = new SimpleDefaultMessageMap("foo")
                .setDefault(DefaultMessageType.QUIT, "stuff")
                .setDefault(DefaultMessageType.QUIT, "otherStuff");

        Assert.assertEquals("otherStuff", defaults.getDefault(DefaultMessageType.QUIT).orElse(null));
    }

    @Test
    public void testGetDefaultWithIfUnsetParameter() {
        SimpleDefaultMessageMap defaults = new SimpleDefaultMessageMap("unset")
                .setDefault(DefaultMessageType.QUIT, "kittens");

        Assert.assertEquals("kittens", defaults.getDefault(DefaultMessageType.QUIT, "set").orElse(null));
        Assert.assertEquals("unset", defaults.getDefault(DefaultMessageType.KICK, "unset").orElse(null));
    }
}
