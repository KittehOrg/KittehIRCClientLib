package org.kitteh.irc.client.library.defaults.feature;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.FakeClient;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.feature.CaseMapping;
import org.kitteh.irc.client.library.util.Listener;
import org.kitteh.irc.client.library.util.TriFunction;
import org.mockito.Mockito;

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
        DefaultISupportManager manager = this.getManager();
        ISupportParameter param = manager.createParameter("MEOW=PURR");
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
        DefaultISupportManager manager = this.getManager();
        ISupportParameter param = manager.createParameter(ISupportParameter.CaseMapping.NAME + '=' + CaseMapping.RFC1459.name());
        Assert.assertTrue(ISupportParameter.CaseMapping.class.isAssignableFrom(param.getClass()));
        Assert.assertEquals(CaseMapping.RFC1459, ((ISupportParameter.CaseMapping) param).getCaseMapping());
    }

    /**
     * Tests casemapping.
     */
    @Test
    public void casemappingFailName() {
        DefaultISupportManager manager = this.getManager();
        Assert.assertFalse(ISupportParameter.CaseMapping.class.isAssignableFrom(manager.createParameter(ISupportParameter.CaseMapping.NAME + "=MEOW").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests casemapping.
     */
    @Test
    public void casemappingFailEmpty() {
        DefaultISupportManager manager = this.getManager();
        Assert.assertFalse(ISupportParameter.CaseMapping.class.isAssignableFrom(manager.createParameter(ISupportParameter.CaseMapping.NAME).getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests channellen.
     */
    @Test
    public void channellen() {
        DefaultISupportManager manager = this.getManager();
        ISupportParameter param = manager.createParameter(ISupportParameter.ChannelLen.NAME + "=15");
        Assert.assertTrue(ISupportParameter.ChannelLen.class.isAssignableFrom(param.getClass()));
        Assert.assertEquals(15, ((ISupportParameter.ChannelLen) param).getInteger());
    }

    /**
     * Tests channellen.
     */
    @Test
    public void channellenFailValue() {
        DefaultISupportManager manager = this.getManager();
        Assert.assertFalse(ISupportParameter.ChannelLen.class.isAssignableFrom(manager.createParameter(ISupportParameter.ChannelLen.NAME + "=MEOW").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests chanlimit.
     */
    @Test
    public void channelLimit() {
        DefaultISupportManager manager = this.getManager();
        ISupportParameter param = manager.createParameter(ISupportParameter.ChanLimit.NAME + "=#:5,!:3");
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
        DefaultISupportManager manager = this.getManager();
        Assert.assertFalse(ISupportParameter.ChanLimit.class.isAssignableFrom(manager.createParameter(ISupportParameter.ChanLimit.NAME + "=MEOW").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests chanlimit.
     */
    @Test
    public void chanlimitFailValueInt() {
        DefaultISupportManager manager = this.getManager();
        Assert.assertFalse(ISupportParameter.ChanLimit.class.isAssignableFrom(manager.createParameter(ISupportParameter.ChanLimit.NAME + "=#:MEOW").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests channellen.
     */
    @Test
    public void channellenFailEmpty() {
        DefaultISupportManager manager = this.getManager();
        Assert.assertFalse(ISupportParameter.ChannelLen.class.isAssignableFrom(manager.createParameter(ISupportParameter.ChannelLen.NAME).getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests chanmodes.
     */
    @Test
    public void chanmodes() {
        DefaultISupportManager manager = this.getManager();
        ISupportParameter param = manager.createParameter(ISupportParameter.ChanModes.NAME + "=ME,O,W,CA,T");
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
        DefaultISupportManager manager = this.getManager();
        ISupportParameter param = manager.createParameter(ISupportParameter.ChanTypes.NAME + "=#!");
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
        DefaultISupportManager manager = this.getManager();
        ISupportParameter param = manager.createParameter(ISupportParameter.Network.NAME + "=Meow");
        Assert.assertTrue(ISupportParameter.Network.class.isAssignableFrom(param.getClass()));
        Assert.assertEquals("Meow", ((ISupportParameter.Network) param).getNetworkName());
    }

    /**
     * Tests nicklen.
     */
    @Test
    public void nicklen() {
        DefaultISupportManager manager = this.getManager();
        ISupportParameter param = manager.createParameter(ISupportParameter.NickLen.NAME + "=4");
        Assert.assertTrue(ISupportParameter.NickLen.class.isAssignableFrom(param.getClass()));
        Assert.assertEquals(4, ((ISupportParameter.NickLen) param).getInteger());
    }

    /**
     * Tests prefix.
     */
    @Test
    public void prefix() {
        DefaultISupportManager manager = this.getManager();
        ISupportParameter param = manager.createParameter(ISupportParameter.Prefix.NAME + "=(ov)@+");
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
        DefaultISupportManager manager = this.getManager();
        Assert.assertFalse(ISupportParameter.Prefix.class.isAssignableFrom(manager.createParameter(ISupportParameter.Prefix.NAME + "=(ov@+").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests prefix.
     */
    @Test
    public void prefixFailSize() {
        DefaultISupportManager manager = this.getManager();
        Assert.assertFalse(ISupportParameter.Prefix.class.isAssignableFrom(manager.createParameter(ISupportParameter.Prefix.NAME + "=(ov)@").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests whox.
     */
    @Test
    public void whox() {
        Assert.assertTrue(ISupportParameter.WhoX.class.isAssignableFrom(this.getManager().createParameter(ISupportParameter.WhoX.NAME).getClass()));
        Assert.assertTrue(ISupportParameter.WhoX.class.isAssignableFrom(this.getManager().createParameter(ISupportParameter.WhoX.NAME + "=MEOW").getClass()));
    }

    private class KittenParameter implements ISupportParameter {
        private KittenParameter(boolean meow) {
            if (meow) {
                throw new RuntimeException();
            }
        }

        @NonNull
        @Override
        public Client getClient() {
            return null;
        }

        @NonNull
        @Override
        public String getName() {
            return "Meow";
        }

        @NonNull
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
        DefaultISupportManager manager = this.getManager();
        TriFunction<Client, String, String, ? extends ISupportParameter> kitten = (client, name, value) -> new KittenParameter(false);
        TriFunction<Client, String, String, ? extends ISupportParameter> naughtyKitten = (client, name, value) -> new KittenParameter(true);

        // Register
        Assert.assertTrue(!manager.registerParameter("KITTEN", kitten).isPresent());
        // Is it registered?
        Optional<TriFunction<Client, String, String, ? extends ISupportParameter>> optionalGotten = manager.getCreator("KITTEN");
        Assert.assertTrue(optionalGotten.isPresent());
        Assert.assertEquals(kitten, optionalGotten.get());
        // Get parameter
        Assert.assertTrue(KittenParameter.class.isAssignableFrom(manager.createParameter("KITTEN").getClass()));
        // Remove
        Optional<TriFunction<Client, String, String, ? extends ISupportParameter>> optionalRemoved = manager.unregisterParameter("KITTEN");
        Assert.assertTrue(optionalRemoved.isPresent());
        Assert.assertEquals(kitten, optionalRemoved.get());

        // Register it again, to test removal on replacement registration.
        manager.registerParameter("KITTEN", kitten);
        // Register the naughty version
        Optional<TriFunction<Client, String, String, ? extends ISupportParameter>> optionalReplaced = manager.registerParameter("KITTEN", naughtyKitten);
        // Was it replaced?
        Assert.assertTrue(optionalReplaced.isPresent());
        Assert.assertEquals(kitten, optionalReplaced.get());

        // Fire exception
        Assert.assertFalse(KittenParameter.class.isAssignableFrom(manager.createParameter("KITTEN").getClass()));
        this.verifyException(manager);
    }

    /**
     * Tests toString.
     */
    @Test
    public void stringTo() {
        Assert.assertEquals(DefaultISupportManager.class.getSimpleName() + " ()", this.getManager().toString());
    }

    private void verifyException(@NonNull DefaultISupportManager manager) {
        Mockito.verify(manager.getClient()).getExceptionListener();
    }

    private DefaultISupportManager getManager() {
        Client.WithManagement client = Mockito.mock(Client.WithManagement.class);
        Mockito.when(client.getExceptionListener()).thenReturn(new Listener<>(new FakeClient(), null));
        return new DefaultISupportManager(client);
    }
}
