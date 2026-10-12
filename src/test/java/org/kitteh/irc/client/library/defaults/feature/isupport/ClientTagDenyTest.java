package org.kitteh.irc.client.library.defaults.feature.isupport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportClientTagDeny;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.mockito.Mockito;

import java.util.List;

public class ClientTagDenyTest {
    private static ISupportParameter.ClientTagDeny deny(String value) {
        return new DefaultISupportClientTagDeny(Mockito.mock(Client.class), "CLIENTTAGDENY", value);
    }

    @Test
    public void testEmpty() {
        Assertions.assertTrue(deny(null).isAllowed("+typing"), "Missing value should allow all");
        Assertions.assertTrue(deny("").isAllowed("+typing"), "Empty value should allow all");
    }

    @Test
    public void testAllBlockedWithExemption() {
        ISupportParameter.ClientTagDeny deny = deny("*,-typing");
        Assertions.assertTrue(deny.isAllowed("+typing"), "Exempt tag with prefix should be allowed");
        Assertions.assertTrue(deny.isAllowed("typing"), "Exempt tag without prefix should be allowed");
        Assertions.assertFalse(deny.isAllowed("+TYPING"), "Tag names are case-sensitive");
        Assertions.assertFalse(deny.isAllowed("+reply"), "Non-exempt tag should be blocked");
    }

    @Test
    public void testSpecificBlocked() {
        ISupportParameter.ClientTagDeny deny = deny("typing,example.com/foo");
        Assertions.assertFalse(deny.isAllowed("+typing"), "Listed tag with prefix should be blocked");
        Assertions.assertFalse(deny.isAllowed("+example.com/foo"), "Listed vendor tag should be blocked");
        Assertions.assertTrue(deny.isAllowed("+Typing"), "Tag names are case-sensitive");
        Assertions.assertTrue(deny.isAllowed("+reply"), "Unlisted tag should be allowed");
    }

    @Test
    public void testEmptyEntries() {
        ISupportParameter.ClientTagDeny deny = deny("*,,-typing,");
        Assertions.assertEquals(List.of("*", "-typing"), deny.getList(), "Empty entries should be skipped");
        Assertions.assertTrue(deny.isAllowed("+typing"), "Exempt tag should be allowed");
    }
}
