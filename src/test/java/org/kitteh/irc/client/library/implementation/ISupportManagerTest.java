package org.kitteh.irc.client.library.implementation;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.feature.CaseMapping;
import org.kitteh.irc.client.library.util.TriFunction;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Tests the ISupportManager implementation
 */
public class ISupportManagerTest {
    /**
     * Tests generally.
     */
    @Test
    public void testParam() {
        ManagerISupport manager = this.getManager();
        ISupportParameter param = manager.getParameter("MEOW=PURR");
        Assert.assertEquals(manager.getClient(), param.getClient());
        Assert.assertEquals("MEOW", param.getName());
        Assert.assertTrue(param.getValue().isPresent());
        Assert.assertEquals("PURR", param.getValue().get());
        Assert.assertTrue(param.toString().contains("MEOW") && param.toString().contains("PURR"));
    }

    /**
     * Tests casemapping.
     */
    @Test
    public void casemapping() {
        ManagerISupport manager = this.getManager();
        ISupportParameter param = manager.getParameter(ISupportParameter.CaseMapping.NAME + '=' + CaseMapping.RFC1459.name());
        Assert.assertTrue(ISupportParameter.CaseMapping.class.isAssignableFrom(param.getClass()));
        Assert.assertEquals(CaseMapping.RFC1459, ((ISupportParameter.CaseMapping) param).getCaseMapping());
    }

    /**
     * Tests casemapping.
     */
    @Test
    public void casemappingFailName() {
        ManagerISupport manager = this.getManager();
        Assert.assertFalse(ISupportParameter.CaseMapping.class.isAssignableFrom(manager.getParameter(ISupportParameter.CaseMapping.NAME + "=MEOW").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests casemapping.
     */
    @Test
    public void casemappingFailEmpty() {
        ManagerISupport manager = this.getManager();
        Assert.assertFalse(ISupportParameter.CaseMapping.class.isAssignableFrom(manager.getParameter(ISupportParameter.CaseMapping.NAME).getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests channellen.
     */
    @Test
    public void channellen() {
        ManagerISupport manager = this.getManager();
        ISupportParameter param = manager.getParameter(ISupportParameter.ChannelLen.NAME + "=15");
        Assert.assertTrue(ISupportParameter.ChannelLen.class.isAssignableFrom(param.getClass()));
        Assert.assertEquals(15, ((ISupportParameter.ChannelLen) param).getInteger());
    }

    /**
     * Tests channellen.
     */
    @Test
    public void channellenFailValue() {
        ManagerISupport manager = this.getManager();
        Assert.assertFalse(ISupportParameter.ChannelLen.class.isAssignableFrom(manager.getParameter(ISupportParameter.ChannelLen.NAME + "=MEOW").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests chanlimit.
     */
    @Test
    public void channelLimit() {
        ManagerISupport manager = this.getManager();
        ISupportParameter param = manager.getParameter(ISupportParameter.ChanLimit.NAME + "=#:5,!:3");
        Assert.assertTrue(ISupportParameter.ChanLimit.class.isAssignableFrom(param.getClass()));
        ISupportParameter.ChanLimit limit = (ISupportParameter.ChanLimit) param;
        Assert.assertEquals(2, limit.getLimits().size());
        Assert.assertTrue(limit.getLimits().containsKey('#'));
        Assert.assertEquals(5, limit.getLimits().get('#').intValue());
        Assert.assertTrue(limit.getLimits().containsKey('!'));
        Assert.assertEquals(3, limit.getLimits().get('!').intValue());
    }

    /**
     * Tests chanlimit.
     */
    @Test
    public void chanlimitFailValueSplit() {
        ManagerISupport manager = this.getManager();
        Assert.assertFalse(ISupportParameter.ChanLimit.class.isAssignableFrom(manager.getParameter(ISupportParameter.ChanLimit.NAME + "=MEOW").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests chanlimit.
     */
    @Test
    public void chanlimitFailValueInt() {
        ManagerISupport manager = this.getManager();
        Assert.assertFalse(ISupportParameter.ChanLimit.class.isAssignableFrom(manager.getParameter(ISupportParameter.ChanLimit.NAME + "=#:MEOW").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests channellen.
     */
    @Test
    public void channellenFailEmpty() {
        ManagerISupport manager = this.getManager();
        Assert.assertFalse(ISupportParameter.ChannelLen.class.isAssignableFrom(manager.getParameter(ISupportParameter.ChannelLen.NAME).getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests chanmodes.
     */
    @Test
    public void chanmodes() {
        ManagerISupport manager = this.getManager();
        ISupportParameter param = manager.getParameter(ISupportParameter.ChanModes.NAME + "=ME,O,W,CA,T");
        Assert.assertTrue(ISupportParameter.ChanModes.class.isAssignableFrom(param.getClass()));
        ISupportParameter.ChanModes modes = (ISupportParameter.ChanModes) param;
        Assert.assertEquals(6, modes.getModes().size());
        Assert.assertEquals('M', modes.getModes().get(0).getChar());
        Assert.assertEquals(ChannelMode.Type.A_MASK, modes.getModes().get(0).getType());
        Assert.assertEquals('E', modes.getModes().get(1).getChar());
        Assert.assertEquals(ChannelMode.Type.A_MASK, modes.getModes().get(1).getType());
        Assert.assertEquals('O', modes.getModes().get(2).getChar());
        Assert.assertEquals(ChannelMode.Type.B_PARAMETER_ALWAYS, modes.getModes().get(2).getType());
        Assert.assertEquals('W', modes.getModes().get(3).getChar());
        Assert.assertEquals(ChannelMode.Type.C_PARAMETER_ON_SET, modes.getModes().get(3).getType());
        Assert.assertEquals('C', modes.getModes().get(4).getChar());
        Assert.assertEquals(ChannelMode.Type.D_PARAMETER_NEVER, modes.getModes().get(4).getType());
        Assert.assertEquals('A', modes.getModes().get(5).getChar());
        Assert.assertEquals(ChannelMode.Type.D_PARAMETER_NEVER, modes.getModes().get(5).getType());
    }

    /**
     * Tests chantypes.
     */
    @Test
    public void chantypes() {
        ManagerISupport manager = this.getManager();
        ISupportParameter param = manager.getParameter(ISupportParameter.ChanTypes.NAME + "=#!");
        Assert.assertTrue(ISupportParameter.ChanTypes.class.isAssignableFrom(param.getClass()));
        ISupportParameter.ChanTypes types = (ISupportParameter.ChanTypes) param;
        Assert.assertEquals(2, types.getTypes().size());
        Assert.assertEquals('#', types.getTypes().get(0).charValue());
        Assert.assertEquals('!', types.getTypes().get(1).charValue());
    }

    /**
     * Tests network.
     */
    @Test
    public void network() {
        ManagerISupport manager = this.getManager();
        ISupportParameter param = manager.getParameter(ISupportParameter.Network.NAME + "=Meow");
        Assert.assertTrue(ISupportParameter.Network.class.isAssignableFrom(param.getClass()));
        Assert.assertEquals("Meow", ((ISupportParameter.Network) param).getNetworkName());
    }

    /**
     * Tests nicklen.
     */
    @Test
    public void nicklen() {
        ManagerISupport manager = this.getManager();
        ISupportParameter param = manager.getParameter(ISupportParameter.NickLen.NAME + "=4");
        Assert.assertTrue(ISupportParameter.NickLen.class.isAssignableFrom(param.getClass()));
        Assert.assertEquals(4, ((ISupportParameter.NickLen) param).getInteger());
    }

    /**
     * Tests prefix.
     */
    @Test
    public void prefix() {
        ManagerISupport manager = this.getManager();
        ISupportParameter param = manager.getParameter(ISupportParameter.Prefix.NAME + "=(ov)@+");
        Assert.assertTrue(ISupportParameter.Prefix.class.isAssignableFrom(param.getClass()));
        ISupportParameter.Prefix prefix = (ISupportParameter.Prefix) param;
        Assert.assertEquals(2, prefix.getModes().size());
        Assert.assertEquals('o', prefix.getModes().get(0).getChar());
        Assert.assertEquals('@', prefix.getModes().get(0).getNickPrefix());
        Assert.assertEquals('v', prefix.getModes().get(1).getChar());
        Assert.assertEquals('+', prefix.getModes().get(1).getNickPrefix());
    }

    /**
     * Tests prefix.
     */
    @Test
    public void prefixFailPattern() {
        ManagerISupport manager = this.getManager();
        Assert.assertFalse(ISupportParameter.Prefix.class.isAssignableFrom(manager.getParameter(ISupportParameter.Prefix.NAME + "=(ov@+").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests prefix.
     */
    @Test
    public void prefixFailSize() {
        ManagerISupport manager = this.getManager();
        Assert.assertFalse(ISupportParameter.Prefix.class.isAssignableFrom(manager.getParameter(ISupportParameter.Prefix.NAME + "=(ov)@").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests whox.
     */
    @Test
    public void whox() {
        Assert.assertTrue(ISupportParameter.WHOX.class.isAssignableFrom(this.getManager().getParameter(ISupportParameter.WHOX.NAME).getClass()));
        Assert.assertTrue(ISupportParameter.WHOX.class.isAssignableFrom(this.getManager().getParameter(ISupportParameter.WHOX.NAME + "=MEOW").getClass()));
    }

    private class KittenParameter implements ISupportParameter {
        private KittenParameter(boolean meow) {
            if (meow) {
                throw new RuntimeException();
            }
        }

        @Nonnull
        @Override
        public Client getClient() {
            return null;
        }

        @Nonnull
        @Override
        public String getName() {
            return "Meow";
        }

        @Nonnull
        @Override
        public Optional<String> getValue() {
            return Optional.empty();
        }
    }

    /**
     * Registers a custom parameter and tests it out.
     */
    @Test
    public void registrationAndRun() {
        ManagerISupport manager = this.getManager();
        TriFunction<Client, String, Optional<String>, ? extends ISupportParameter> kitten = (client, name, value) -> new KittenParameter(false);
        TriFunction<Client, String, Optional<String>, ? extends ISupportParameter> naughtyKitten = (client, name, value) -> new KittenParameter(true);

        // Register
        Assert.assertTrue(!manager.registerParameter("KITTEN", kitten).isPresent());
        // Is it registered?
        Optional<TriFunction<Client, String, Optional<String>, ? extends ISupportParameter>> optionalGotten = manager.getCreator("KITTEN");
        Assert.assertTrue(optionalGotten.isPresent());
        Assert.assertEquals(kitten, optionalGotten.get());
        // Get parameter
        Assert.assertTrue(KittenParameter.class.isAssignableFrom(manager.getParameter("KITTEN").getClass()));
        // Remove
        Optional<TriFunction<Client, String, Optional<String>, ? extends ISupportParameter>> optionalRemoved = manager.unregisterParameter("KITTEN");
        Assert.assertTrue(optionalRemoved.isPresent());
        Assert.assertEquals(kitten, optionalRemoved.get());

        // Register it again, to test removal on replacement registration.
        manager.registerParameter("KITTEN", kitten);
        // Register the naughty version
        Optional<TriFunction<Client, String, Optional<String>, ? extends ISupportParameter>> optionalReplaced = manager.registerParameter("KITTEN", naughtyKitten);
        // Was it replaced?
        Assert.assertTrue(optionalReplaced.isPresent());
        Assert.assertEquals(kitten, optionalReplaced.get());

        // Fire exception
        Assert.assertFalse(KittenParameter.class.isAssignableFrom(manager.getParameter("KITTEN").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests toString.
     */
    @Test
    public void stringTo() {
        Assert.assertEquals(ManagerISupport.class.getSimpleName() + " ()", this.getManager().toString());
    }

    private void verifyException(@Nonnull ManagerISupport manager) {
        Mockito.verify(manager.getClient()).getExceptionListener();
    }

    private ManagerISupport getManager() {
        InternalClient client = Mockito.mock(InternalClient.class);
        Mockito.when(client.getExceptionListener()).thenReturn(new Listener<>("Client", null));
        return new ManagerISupport(client);
    }
}
