package org.kitteh.irc.client.library.implementation;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.sts.STSClientState;
import org.kitteh.irc.client.library.feature.sts.STSMachine;
import org.kitteh.irc.client.library.feature.sts.STSStorageManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StsTest {

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
        final String policyString = "sts=port=1234,duration=300,foobar";
        capabilities.add(new ManagerCapability.IRCCapabilityState(client, policyString));
        List<ServerMessage> messages = new ArrayList<>();
        messages.add(new IRCServerMessage(":test.kitteh CAP ^o^ LS :" + policyString, new ArrayList<>()));
        handler.onCapLs(new CapabilitiesSupportedListEvent(client, messages, true, capabilities));
        Assert.assertEquals(machine.getCurrentState(), STSClientState.STS_PRESENT_RECONNECTING);

        Map<String, Optional<String>> extractedPolicy = machine.getPolicy();
        final Optional<String> port = extractedPolicy.get("port");
        Assert.assertTrue(port.isPresent());
        Assert.assertTrue(port.get().equals("1234"));

        final Optional<String> duration = extractedPolicy.get("duration");
        Assert.assertTrue(duration.isPresent());
        Assert.assertTrue(duration.get().equals("300"));

        Assert.assertTrue(extractedPolicy.containsKey("foobar"));
        Assert.assertFalse(extractedPolicy.get("foobar").isPresent());
    }

    /**
     * Checks an error is raised for an invalid port value.
     */
    @Test(expected=KittehServerMessageException.class)
    public void testHandlerWithInvalidPort() {
        final FakeClient client = new FakeClient();
        client.getConfig().set(Config.SSL, false);
        final StubMachine machine = new StubMachine();
        STSHandler handler = new STSHandler(machine, client);
        List<CapabilityState> capabilities = new ArrayList<>();
        final String policyString = "sts=port=cats";
        capabilities.add(new ManagerCapability.IRCCapabilityState(client, policyString));
        List<ServerMessage> messages = new ArrayList<>();
        messages.add(new IRCServerMessage(":test.kitteh CAP ^o^ LS :" + policyString, new ArrayList<>()));
        handler.onCapLs(new CapabilitiesSupportedListEvent(client, messages, true, capabilities));
    }

    /**
     * Checks an error is raised for an missing port value.
     */
    @Test(expected=KittehServerMessageException.class)
    public void testHandlerWithMissingPortValue() {
        final FakeClient client = new FakeClient();
        client.getConfig().set(Config.SSL, false);
        final StubMachine machine = new StubMachine();
        STSHandler handler = new STSHandler(machine, client);
        List<CapabilityState> capabilities = new ArrayList<>();
        final String policyString = "sts=port";
        capabilities.add(new ManagerCapability.IRCCapabilityState(client, policyString));
        List<ServerMessage> messages = new ArrayList<>();
        messages.add(new IRCServerMessage(":test.kitteh CAP ^o^ LS :" + policyString, new ArrayList<>()));
        handler.onCapLs(new CapabilitiesSupportedListEvent(client, messages, true, capabilities));
    }

    private class StubMachine implements STSMachine {

        private STSClientState state = STSClientState.UNKNOWN;
        private Map<String, Optional<String>> policy;

        @Nonnull
        @Override
        public STSClientState getCurrentState() {
            return this.state;
        }

        @Override
        public void setCurrentState(@Nonnull STSClientState newState) {
            this.state = newState;
        }

        @Override
        public STSStorageManager getStorageManager() {
            return null;
        }

        @Override
        public void setStsPolicy(@Nonnull Map<String, Optional<String>> policy) {
            this.policy = policy;
        }


        Map<String, Optional<String>> getPolicy() {
            return policy;
        }
    }
}
