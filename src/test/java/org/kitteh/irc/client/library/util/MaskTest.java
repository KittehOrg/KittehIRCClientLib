package org.kitteh.irc.client.library.util;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.implementation.TestUser;
import org.kitteh.irc.client.library.test.TestUtil;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;

/**
 * It's a mask test.
 */
public class MaskTest {
    /**
     * Tests a full string.
     */
    @Test
    public void testFullString() {
        final Mask mask = Mask.fromString("mbaxter!~mbax@kitten.institute");
        TestUtil.assertOptionalEquals(mask.getNick(), "mbaxter");
        TestUtil.assertOptionalEquals(mask.getUser(), "~mbax");
        TestUtil.assertOptionalEquals(mask.getHost(), "kitten.institute");
        Assert.assertEquals("mbaxter!~mbax@kitten.institute", mask.asString());
    }

    /**
     * Tests a nick.
     */
    @Test
    public void testNick() {
        final Mask mask = Mask.fromNick("mbaxter");
        TestUtil.assertOptionalEquals(mask.getNick(), "mbaxter");
        Assert.assertFalse(mask.getUser().isPresent());
        Assert.assertFalse(mask.getHost().isPresent());
        Assert.assertEquals("mbaxter!*@*", mask.asString());
    }

    /**
     * Tests a user string.
     */
    @Test
    public void testUser() {
        final Mask mask = Mask.fromUserString("~mbax");
        Assert.assertFalse(mask.getNick().isPresent());
        Assert.assertTrue(mask.getUser().isPresent());
        TestUtil.assertOptionalEquals(mask.getUser(), "~mbax");
        Assert.assertEquals("*!~mbax@*", mask.asString());
    }

    /**
     * Tests a host.
     */
    @Test
    public void testHost() {
        final Mask mask = Mask.fromHost("kitten.institute");
        Assert.assertFalse(mask.getNick().isPresent());
        Assert.assertFalse(mask.getUser().isPresent());
        TestUtil.assertOptionalEquals(mask.getHost(), "kitten.institute");
        Assert.assertEquals("*!*@kitten.institute", mask.asString());
    }

    /**
     * Tests that a user passes a predicate test.
     */
    @Test
    public void testPredicateTest() {
        final Mask mask = Mask.fromNick("kashike");
        final User kashike = new TestUser("kashike", "kashike", "is.a.miserable.ninja", null);
        final User lol768 = new TestUser("lol768", "lol768", "lol768.com", null);
        Assert.assertTrue(mask.test(kashike));
        Assert.assertFalse(mask.test(lol768));
    }

    /**
     * Tests matches against a channel.
     */
    @Test
    public void testMatches() {
        final Channel channel = Mockito.mock(Channel.class);
        final User mbaxter = new TestUser("mbaxter", "~mbax", "kitten.institute", null);
        final User kashike = new TestUser("kashike", "kashike", "is.a.miserable.ninja", null);
        final User lol768 = new TestUser("lol768", "lol768", "lol768.com", null);
        final User zarthus = new TestUser("Zarthus", "Zarthus", "lynvie.com", null);
        Mockito.when(channel.getUsers()).thenReturn(Arrays.asList(mbaxter, kashike, lol768, zarthus));

        Mask mask = Mask.fromNick("mbaxter");
        Collection<User> matches = mask.getMatches(channel);
        Assert.assertTrue(matches.size() == 1);
        Assert.assertTrue(matches.contains(mbaxter));
        Assert.assertFalse(matches.contains(kashike));
        Assert.assertFalse(matches.contains(lol768));
        Assert.assertFalse(matches.contains(zarthus));

        mask = Mask.fromNick("kashike");
        matches = mask.getMatches(channel);
        Assert.assertTrue(matches.size() == 1);
        Assert.assertFalse(matches.contains(mbaxter));
        Assert.assertTrue(matches.contains(kashike));
        Assert.assertFalse(matches.contains(lol768));
        Assert.assertFalse(matches.contains(zarthus));

        mask = Mask.fromHost("lynvie.com");
        matches = mask.getMatches(channel);
        Assert.assertTrue(matches.size() == 1);
        Assert.assertFalse(matches.contains(mbaxter));
        Assert.assertFalse(matches.contains(kashike));
        Assert.assertFalse(matches.contains(lol768));
        Assert.assertTrue(matches.contains(zarthus));

        mask = Mask.fromHost("*");
        matches = mask.getMatches(channel);
        Assert.assertTrue(matches.size() == 4);
        Assert.assertTrue(matches.contains(mbaxter));
        Assert.assertTrue(matches.contains(kashike));
        Assert.assertTrue(matches.contains(lol768));
        Assert.assertTrue(matches.contains(zarthus));
    }

    @Test
    public void testSlashHost() {
        final Channel channel = Mockito.mock(Channel.class);
        final User mbaxter = new TestUser("mbaxter", "~mbax", "meow/kitties", null);
        final User kashike = new TestUser("kashike", "kashike", "hiss/kitties", null);
        final User lol768 = new TestUser("lol768", "lol768", "hiss/kitties", null);
        final User zarthus = new TestUser("Zarthus", "Zarthus", "meow/kitties", null);
        final User bendem = new TestUser("bendem", "bendem", "irc.bendem.be", null);
        Mockito.when(channel.getUsers()).thenReturn(Arrays.asList(mbaxter, kashike, lol768, zarthus, bendem));

        Mask mask = Mask.fromHost("meow/kitties");
        Collection<User> matches = mask.getMatches(channel);
        Assert.assertTrue(matches.size() == 2);
        Assert.assertTrue(matches.contains(mbaxter));
        Assert.assertFalse(matches.contains(kashike));
        Assert.assertFalse(matches.contains(lol768));
        Assert.assertTrue(matches.contains(zarthus));
        Assert.assertFalse(matches.contains(bendem));

        mask = Mask.fromHost("hiss/kitties");
        matches = mask.getMatches(channel);
        Assert.assertTrue(matches.size() == 2);
        Assert.assertFalse(matches.contains(mbaxter));
        Assert.assertTrue(matches.contains(kashike));
        Assert.assertTrue(matches.contains(lol768));
        Assert.assertFalse(matches.contains(zarthus));
        Assert.assertFalse(matches.contains(bendem));
    }

    @Test
    public void testUserPatternTest() {
        final Channel channel = Mockito.mock(Channel.class);
        final User mbaxter = new TestUser("mbaxter", "~mbax", "meow/kitties", null);
        final User kashike = new TestUser("kashike", "kashike", "hiss/kitties", null);
        final User lol768 = new TestUser("lol768", "lol768", "hiss/kitties", null);
        final User zarthus = new TestUser("Zarthus", "Zarthus", "meow/kitties", null);
        final User bendem = new TestUser("bendem", "bendem", "irc.bendem.be", null);
        Mockito.when(channel.getUsers()).thenReturn(Arrays.asList(mbaxter, kashike, lol768, zarthus, bendem));

        Mask mask = Mask.fromHost("*/kitties");
        Collection<User> matches = mask.getMatches(channel);
        Assert.assertTrue(matches.size() == 4);
        Assert.assertTrue(matches.contains(mbaxter));
        Assert.assertTrue(matches.contains(kashike));
        Assert.assertTrue(matches.contains(lol768));
        Assert.assertTrue(matches.contains(zarthus));
        Assert.assertFalse(matches.contains(bendem));
    }

    @Test
    public void testStringPatternTest() {
        final Mask mask = Mask.fromString("*!*@*/kitties");
        Assert.assertTrue(mask.test("mbaxter!~mbax@meow/kitties"));
        Assert.assertTrue(mask.test("kashike!kashike@hiss/kitties"));
        Assert.assertTrue(mask.test("lol768!lol768@hiss/kitties"));
        Assert.assertTrue(mask.test("Zarthus!Zarthus@meow/kitties"));
        Assert.assertFalse(mask.test("bendem!bendem@irc.bendem.be"));
    }

    @Test
    public void testPredicateBasedTestWithQuestionMarks() {
        final Channel channel = Mockito.mock(Channel.class);
        final User mbaxter = new TestUser("mbax", "~mbax", "kitten.institute", null);
        final User kashike = new TestUser("kashike", "kashike", "is.a.miserable.ninja", null);
        final User lol768 = new TestUser("lol", "lol768", "lol768.com", null);
        final User zarthus = new TestUser("Zarthus", "Zarthus", "lynvie.com", null);
        final User bendem = new TestUser("bendem", "bendem", "irc.bendem.be", null);
        Mockito.when(channel.getUsers()).thenReturn(Arrays.asList(mbaxter, kashike, lol768, zarthus, bendem));

        Mask mask = Mask.fromString("??????*!*@*.*");
        Collection<User> matches = mask.getMatches(channel);
        Assert.assertTrue(matches.size() == 3);
        Assert.assertFalse(matches.contains(mbaxter));
        Assert.assertTrue(matches.contains(kashike));
        Assert.assertFalse(matches.contains(lol768));
        Assert.assertTrue(matches.contains(zarthus));
        Assert.assertTrue(matches.contains(bendem));
    }

    @Test
    public void testStringBasedTestWithQuestionMarks() {
        final Mask mask = Mask.fromString("????*!*@*");
        Assert.assertFalse(mask.test("m!~mbax@meow/kitties"));
        Assert.assertFalse(mask.test("ka!kashike@hiss/kitties"));
        Assert.assertFalse(mask.test("lol!lol768@hiss/kitties"));
        Assert.assertTrue(mask.test("Zart!Zarthus@meow/kitties"));
        Assert.assertTrue(mask.test("bende!bendem@irc.bendem.be"));
    }

    @Test
    public void testColored() {
        final Channel channel = Mockito.mock(Channel.class);
        final User mbaxter = new TestUser("mbaxter", "~mbax", StringUtil.makeRainbow("meow/kitties"), null);
        final User kashike = new TestUser("kashike", "kashike", "hiss/kitties", null);
        final User lol768 = new TestUser("lol768", "lol768", "hiss/kitties", null);
        final User zarthus = new TestUser("Zarthus", "Zarthus", "meow/kitties", null);
        final User bendem = new TestUser("bendem", "bendem", "irc.bendem.be", null);
        Mockito.when(channel.getUsers()).thenReturn(Arrays.asList(mbaxter, kashike, lol768, zarthus, bendem));

        Mask mask = Mask.fromHost(StringUtil.makeRainbow("meow/kitties"));
        Collection<User> matches = mask.getMatches(channel);
        Assert.assertTrue(matches.size() == 1);
        Assert.assertTrue(matches.contains(mbaxter));
        Assert.assertFalse(matches.contains(kashike));
        Assert.assertFalse(matches.contains(lol768));
        Assert.assertFalse(matches.contains(zarthus));
        Assert.assertFalse(matches.contains(bendem));

        mask = Mask.fromHost("meow/kitties");
        matches = mask.getMatches(channel);
        Assert.assertTrue(matches.size() == 1);
        Assert.assertFalse(matches.contains(mbaxter));
        Assert.assertFalse(matches.contains(kashike));
        Assert.assertFalse(matches.contains(lol768));
        Assert.assertTrue(matches.contains(zarthus));
        Assert.assertFalse(matches.contains(bendem));

        mask = Mask.fromHost("hiss/kitties");
        matches = mask.getMatches(channel);
        Assert.assertTrue(matches.size() == 2);
        Assert.assertFalse(matches.contains(mbaxter));
        Assert.assertTrue(matches.contains(kashike));
        Assert.assertTrue(matches.contains(lol768));
        Assert.assertFalse(matches.contains(zarthus));
        Assert.assertFalse(matches.contains(bendem));
    }
}
