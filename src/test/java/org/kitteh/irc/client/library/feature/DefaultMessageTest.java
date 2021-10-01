package org.kitteh.irc.client.library.feature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.defaults.feature.SimpleDefaultMessageMap;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageType;

import java.util.Map;

/**
 * Tests the DefaultMessage system.
 */
public class DefaultMessageTest {
    @Test
    public void testSetKeyReturnsSetValue() {
        SimpleDefaultMessageMap defaults = new SimpleDefaultMessageMap("foo")
                .setDefault(DefaultMessageType.QUIT, "My kittens bring all the boys to the yard");

        Assertions.assertEquals("My kittens bring all the boys to the yard", defaults.getDefault(DefaultMessageType.QUIT).orElse(null));
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

        Assertions.assertEquals(DefaultMessageType.values().length - 1, foos);
        Assertions.assertEquals(1, bars);
        Assertions.assertEquals(0, unknowns);
    }

    @Test
    public void testCanReSetSetting() {
        SimpleDefaultMessageMap defaults = new SimpleDefaultMessageMap("foo")
                .setDefault(DefaultMessageType.QUIT, "stuff")
                .setDefault(DefaultMessageType.QUIT, "otherStuff");

        Assertions.assertEquals("otherStuff", defaults.getDefault(DefaultMessageType.QUIT).orElse(null));
    }

    @Test
    public void testGetDefaultWithIfUnsetParameter() {
        SimpleDefaultMessageMap defaults = new SimpleDefaultMessageMap("unset")
                .setDefault(DefaultMessageType.QUIT, "kittens");

        Assertions.assertEquals("kittens", defaults.getDefault(DefaultMessageType.QUIT, "set").orElse(null));
        Assertions.assertEquals("unset", defaults.getDefault(DefaultMessageType.KICK, "unset").orElse(null));
    }
}
