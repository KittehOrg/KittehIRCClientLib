package org.kitteh.irc.client.library.implementation;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.defaults.DefaultCapabilityState;
import org.kitteh.irc.client.library.element.defaults.DefaultServerMessage;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesNewSupportedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.sts.STSClientState;
import org.kitteh.irc.client.library.feature.sts.STSMachine;
import org.kitteh.irc.client.library.feature.sts.STSPolicy;
import org.kitteh.irc.client.library.feature.sts.STSStorageManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests around the STS event handler and the underlying machine.
 */
public class STSHandlerTest {

    /**
     * Checks that the STS Handler works when we connect in a plaintext manner and give
     * it a policy.
     */
    @Test
    public void testHandlerWhenInsecure() {
        final FakeClient client = new FakeClient();
        client.getConfig().set(Config.SSL, false);
        final StubMachine machine = new StubMachine();
        Assert.assertEquals(machine.getCurrentState(), STSClientState.UNKNOWN);

        STSHandler handler = new STSHandler(machine, client);

        List<CapabilityState> capabilities = new ArrayList<>();
        final String policyString = "draft/sts=" + STSPolicy.POLICY_OPTION_KEY_PORT + "=1234," + STSPolicy.POLICY_OPTION_KEY_DURATION + "=300,foobar";
        capabilities.add(new DefaultCapabilityState(client, policyString));
        List<ServerMessage> messages = new ArrayList<>();
        messages.add(new DefaultServerMessage(":test.kitteh CAP ^o^ LS :" + policyString, new ArrayList<>()));
        handler.onCapLs(new CapabilitiesSupportedListEvent(client, messages, true, capabilities));
        Assert.assertEquals(machine.getCurrentState(), STSClientState.STS_PRESENT_RECONNECTING);

        STSPolicy extractedPolicy = machine.getPolicy();
        final String port = extractedPolicy.getOptions().get(STSPolicy.POLICY_OPTION_KEY_PORT);
        Assert.assertTrue(port.equals("1234"));

        final String duration = extractedPolicy.getOptions().get(STSPolicy.POLICY_OPTION_KEY_DURATION);
        Assert.assertTrue(duration.equals("300"));

        Assert.assertTrue(extractedPolicy.getFlags().contains("foobar"));
    }

    /**
     * Checks that the STS Handler works when the STS policy arrives
     * via CAP new.
     */
    @Test
    public void testHandlerWhenInsecureUsingCapNew() {
        final FakeClient client = new FakeClient();
        client.getConfig().set(Config.SSL, false);
        final StubMachine machine = new StubMachine();
        Assert.assertEquals(machine.getCurrentState(), STSClientState.UNKNOWN);

        STSHandler handler = new STSHandler(machine, client);

        List<CapabilityState> capabilities = new ArrayList<>();
        final String policyString = "draft/sts=" + STSPolicy.POLICY_OPTION_KEY_PORT + "=1234," + STSPolicy.POLICY_OPTION_KEY_DURATION + "=300,foobar";
        capabilities.add(new DefaultCapabilityState(client, policyString));
        List<ServerMessage> messages = new ArrayList<>();
        messages.add(new DefaultServerMessage(":test.kitteh CAP ^o^ LS :" + policyString, new ArrayList<>()));
        handler.onCapNew(new CapabilitiesNewSupportedEvent(client, messages, true, capabilities));
        Assert.assertEquals(machine.getCurrentState(), STSClientState.STS_PRESENT_RECONNECTING);

        STSPolicy extractedPolicy = machine.getPolicy();
        final String port = extractedPolicy.getOptions().get(STSPolicy.POLICY_OPTION_KEY_PORT);
        Assert.assertTrue(port.equals("1234"));

        final String duration = extractedPolicy.getOptions().get(STSPolicy.POLICY_OPTION_KEY_DURATION);
        Assert.assertTrue(duration.equals("300"));

        Assert.assertTrue(extractedPolicy.getFlags().contains("foobar"));
    }

    /**
     * Checks an error is raised for an invalid port value.
     */
    @Test(expected = KittehServerMessageException.class)
    public void testHandlerWithInvalidPort() {
        final FakeClient client = new FakeClient();
        client.getConfig().set(Config.SSL, false);
        final StubMachine machine = new StubMachine();
        STSHandler handler = new STSHandler(machine, client);
        List<CapabilityState> capabilities = new ArrayList<>();
        final String policyString = "draft/sts=" + STSPolicy.POLICY_OPTION_KEY_PORT + "=cats";
        capabilities.add(new DefaultCapabilityState(client, policyString));
        List<ServerMessage> messages = new ArrayList<>();
        messages.add(new DefaultServerMessage(":test.kitteh CAP ^o^ LS :" + policyString, new ArrayList<>()));
        handler.onCapLs(new CapabilitiesSupportedListEvent(client, messages, true, capabilities));
    }

    /**
     * Checks an error is raised for an missing port value.
     */
    @Test(expected = KittehServerMessageException.class)
    public void testHandlerWithMissingPortValue() {
        final FakeClient client = new FakeClient();
        client.getConfig().set(Config.SSL, false);
        final StubMachine machine = new StubMachine();
        STSHandler handler = new STSHandler(machine, client);
        List<CapabilityState> capabilities = new ArrayList<>();
        final String policyString = "draft/sts=" + STSPolicy.POLICY_OPTION_KEY_PORT;
        capabilities.add(new DefaultCapabilityState(client, policyString));
        List<ServerMessage> messages = new ArrayList<>();
        messages.add(new DefaultServerMessage(":test.kitteh CAP ^o^ LS :" + policyString, new ArrayList<>()));
        handler.onCapLs(new CapabilitiesSupportedListEvent(client, messages, true, capabilities));
    }

    private class StubMachine implements STSMachine {

        private STSClientState state = STSClientState.UNKNOWN;
        private STSPolicy policy;

        @Nonnull
        @Override
        public STSClientState getCurrentState() {
            return this.state;
        }

        @Override
        public void setCurrentState(@Nonnull STSClientState newState) {
            this.state = newState;
        }

        @Nonnull
        @Override
        public STSStorageManager getStorageManager() {
            return null;
        }

        /**
         * Provides a key->value map of properties, making up the STS policy.
         * <p>
         * It is expected the policy is valid at this stage.
         *
         * @param policy the valid STS policy
         */
        @Override
        public void setSTSPolicy(@Nonnull STSPolicy policy) {
            this.policy = policy;
        }

        STSPolicy getPolicy() {
            return policy;
        }
    }
}
