package org.kitteh.irc.client.library.util.mask;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.TestUtil;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.implementation.TestUser;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MaskTest {
    private static void assertContainsOnly(final Mask mask, final List<? extends User> yes, final List<? extends User> no) {
        final Channel channel = Mockito.mock(Channel.class);
        TestUtil.assertContainsOnly(Mockito.when(channel.getUsers()), () -> mask.getMatches(channel), yes, no);
    }

    @Test
    public void testHost() {
        final HostMask mask = HostMask.fromHost("kitten.institute");

        Assert.assertEquals("kitten.institute", mask.getHost());
        Assert.assertEquals("*!*@kitten.institute", mask.asString());

        final User mbaxter = new TestUser("mbaxter", "~mbax", "kitten.institute", null);
        final User kashike = new TestUser("kashike", "kashike", "is.sleeping.in.the.kingdom.of.kandarin.xyz", null);

        assertContainsOnly(mask, Collections.singletonList(mbaxter), Collections.singletonList(kashike));
    }

    @Test
    public void testNick() {
        final NickMask mask = NickMask.fromNick("mbaxter");

        Assert.assertEquals("mbaxter", mask.getNick());
        Assert.assertEquals("mbaxter!*@*", mask.asString());

        final Channel channel = Mockito.mock(Channel.class);
        final User mbaxter = new TestUser("mbaxter", "~mbax", "kitten.institute", null);
        final User kashike = new TestUser("kashike", "kashike", "is.sleeping.in.the.kingdom.of.kandarin.xyz", null);
        Mockito.when(channel.getUsers()).thenReturn(Arrays.asList(mbaxter, kashike));

        final Collection<User> matches = mask.getMatches(channel);
        Assert.assertTrue(matches.size() == 1);
        Assert.assertTrue(matches.contains(mbaxter));
        Assert.assertFalse(matches.contains(kashike));
    }
}
