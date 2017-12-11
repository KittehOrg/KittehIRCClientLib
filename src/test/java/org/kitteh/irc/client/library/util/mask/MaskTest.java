package org.kitteh.irc.client.library.util.mask;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.TestUtil;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.implementation.TestUser;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MaskTest {
    private static void assertContainsOnly(final Mask mask, final List<? extends User> yes, final List<? extends User> no) {
        final Channel channel = Mockito.mock(Channel.class);
        TestUtil.assertContainsOnly(Mockito.when(channel.getUsers()), () -> mask.getMatches(channel), yes, no);
    }

    @Test
    public void testHostSingle() {
        final HostMask mask = HostMask.fromHost("kitten.institute");

        Assert.assertEquals("kitten.institute", mask.getHost());
        Assert.assertEquals("*!*@kitten.institute", mask.asString());

        MaskTest.assertContainsOnly(mask, Collections.singletonList(
            new TestUser("mbaxter", "~mbax", "kitten.institute", null)
        ), Collections.singletonList(
            new TestUser("kashike", "kashike", "is.sleeping.in.the.kingdom.of.kandarin.xyz", null)
        ));
    }

    @Test
    public void testHostMulti() {
        final HostMask mask = HostMask.fromHost("kitten.institute");

        Assert.assertEquals("kitten.institute", mask.getHost());
        Assert.assertEquals("*!*@kitten.institute", mask.asString());

        MaskTest.assertContainsOnly(mask, Arrays.asList(
            new TestUser("mbaxter", "~mbax", "kitten.institute", null),
            new TestUser("kashike", "kashike", "kitten.institute", null)
        ), Collections.singletonList(
            new TestUser("lol768", "lol768", "andy.in.toy.story", null)
        ));
    }

    @Test
    public void testName() {
        final User user = new TestUser("mbaxter", "~mbax", "kitten.institute", null);
        final NameMask mask = NameMask.fromUser(user);

        Assert.assertEquals("mbaxter", mask.getNick().orElse(null));
        Assert.assertEquals("~mbax", mask.getUserString().orElse(null));
        Assert.assertEquals("kitten.institute", mask.getHost().orElse(null));
        Assert.assertEquals("mbaxter!~mbax@kitten.institute", mask.asString());

        MaskTest.assertContainsOnly(mask, Collections.singletonList(
            new TestUser("mbaxter", "~mbax", "kitten.institute", null)
        ), Arrays.asList(
            new TestUser("kashike", "kashike", "kitten.institute", null),
            new TestUser("lol768", "lol768", "andy.in.toy.story", null)
        ));
    }

    @Test
    public void testNick() {
        final NickMask mask = NickMask.fromNick("mbaxter");

        Assert.assertEquals("mbaxter", mask.getNick());
        Assert.assertEquals("mbaxter!*@*", mask.asString());

        MaskTest.assertContainsOnly(mask, Collections.singletonList(
            new TestUser("mbaxter", "~mbax", "kitten.institute", null)
        ), Collections.singletonList(
            new TestUser("kashike", "kashike", "is.sleeping.in.the.kingdom.of.kandarin.xyz", null)
        ));
    }

    @Test
    public void testUserString() {
        final UserStringMask mask = UserStringMask.fromUserString("~mbax");

        Assert.assertEquals("~mbax", mask.getUserString());
        Assert.assertEquals("*!~mbax@*", mask.asString());

        MaskTest.assertContainsOnly(mask, Collections.singletonList(
            new TestUser("mbaxter", "~mbax", "kitten.institute", null)
        ), Collections.singletonList(
            new TestUser("kashike", "kashike", "is.sleeping.in.the.kingdom.of.kandarin.xyz", null)
        ));
    }
}
