package org.kitteh.irc.client.library.defaults.feature.sts;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.FakeClient;
import org.kitteh.irc.client.library.defaults.element.DefaultCapabilityState;
import org.kitteh.irc.client.library.defaults.element.DefaultServerMessage;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesNewSupportedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.exception.KittehServerMessageException;
import org.kitteh.irc.client.library.feature.sts.StsClientState;
import org.kitteh.irc.client.library.feature.sts.StsHandler;
import org.kitteh.irc.client.library.feature.sts.StsMachine;
import org.kitteh.irc.client.library.feature.sts.StsPolicy;
import org.kitteh.irc.client.library.feature.sts.StsStorageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests around the STS event handler and the underlying machine.
 */
public class StsHandlerTest {
    /**
     * Checks that the STS Handler works when we connect in a plaintext manner and give
     * it a policy.
     */
    @Test
    public void testHandlerWhenInsecure() {
        final FakeClient client = new FakeClient();
        client.setSecure(false);
        final StubMachine machine = new StubMachine();
        Assertions.assertEquals(StsClientState.UNKNOWN, machine.getCurrentState());

        StsHandler handler = new StsHandler(machine, client);

        List<CapabilityState> capabilities = new ArrayList<>();
        final String policyString = "draft/sts=" + StsPolicy.POLICY_OPTION_KEY_PORT + "=1234," + StsPolicy.POLICY_OPTION_KEY_DURATION + "=300,foobar";
        capabilities.add(new DefaultCapabilityState(client, policyString));
        List<ServerMessage> messages = new ArrayList<>();
        messages.add(new DefaultServerMessage(":test.kitteh CAP ^o^ LS :" + policyString, new ArrayList<>()));
        handler.onCapLs(new CapabilitiesSupportedListEvent(client, messages, true, capabilities));
        Assertions.assertEquals(StsClientState.STS_PRESENT_RECONNECTING, machine.getCurrentState());

        StsPolicy extractedPolicy = machine.getPolicy();
        final String port = extractedPolicy.getOptions().get(StsPolicy.POLICY_OPTION_KEY_PORT);
        Assertions.assertEquals("1234", port);

        final String duration = extractedPolicy.getOptions().get(StsPolicy.POLICY_OPTION_KEY_DURATION);
        Assertions.assertEquals("300", duration);

        Assertions.assertTrue(extractedPolicy.getFlags().contains("foobar"));
    }

    /**
     * Checks that the STS Handler works now that the STS spec has
     * been ratified, and no longer uses a draft/ prefix.
     */
    @Test
    public void testHandlerWithNonDraftPolicy() {
        final FakeClient client = new FakeClient();
        client.setSecure(false);
        final StubMachine machine = new StubMachine();
        Assertions.assertEquals(StsClientState.UNKNOWN, machine.getCurrentState());

        StsHandler handler = new StsHandler(machine, client);

        List<CapabilityState> capabilities = new ArrayList<>();
        final String policyString = "sts=" + StsPolicy.POLICY_OPTION_KEY_PORT + "=7681," + StsPolicy.POLICY_OPTION_KEY_DURATION + "=300,foobar";
        capabilities.add(new DefaultCapabilityState(client, policyString));
        List<ServerMessage> messages = new ArrayList<>();
        messages.add(new DefaultServerMessage(":test.kitteh CAP ^o^ LS :" + policyString, new ArrayList<>()));
        handler.onCapLs(new CapabilitiesSupportedListEvent(client, messages, true, capabilities));
        Assertions.assertEquals(StsClientState.STS_PRESENT_RECONNECTING, machine.getCurrentState());

        StsPolicy extractedPolicy = machine.getPolicy();
        final String port = extractedPolicy.getOptions().get(StsPolicy.POLICY_OPTION_KEY_PORT);
        Assertions.assertEquals("7681", port);

        final String duration = extractedPolicy.getOptions().get(StsPolicy.POLICY_OPTION_KEY_DURATION);
        Assertions.assertEquals("300", duration);

        Assertions.assertTrue(extractedPolicy.getFlags().contains("foobar"));
    }

    /**
     * Checks that the STS Handler works when the STS policy arrives
     * via CAP new.
     */
    @Test
    public void testHandlerWhenInsecureUsingCapNew() {
        final FakeClient client = new FakeClient();
        client.setSecure(false);
        final StubMachine machine = new StubMachine();
        Assertions.assertEquals(StsClientState.UNKNOWN, machine.getCurrentState());

        StsHandler handler = new StsHandler(machine, client);

        List<CapabilityState> capabilities = new ArrayList<>();
        final String policyString = "draft/sts=" + StsPolicy.POLICY_OPTION_KEY_PORT + "=1234," + StsPolicy.POLICY_OPTION_KEY_DURATION + "=300,foobar";
        capabilities.add(new DefaultCapabilityState(client, policyString));
        ServerMessage message = new DefaultServerMessage(":test.kitteh CAP ^o^ NEW :" + policyString, new ArrayList<>());
        handler.onCapNew(new CapabilitiesNewSupportedEvent(client, message, true, capabilities));
        Assertions.assertEquals(StsClientState.STS_PRESENT_RECONNECTING, machine.getCurrentState());

        StsPolicy extractedPolicy = machine.getPolicy();
        final String port = extractedPolicy.getOptions().get(StsPolicy.POLICY_OPTION_KEY_PORT);
        Assertions.assertEquals("1234", port);

        final String duration = extractedPolicy.getOptions().get(StsPolicy.POLICY_OPTION_KEY_DURATION);
        Assertions.assertEquals("300", duration);

        Assertions.assertTrue(extractedPolicy.getFlags().contains("foobar"));
    }

    /**
     * Checks an error is raised for an invalid port value.
     */
    @Test
    public void testHandlerWithInvalidPort() {
        final FakeClient client = new FakeClient();
        client.setSecure(false);
        final StubMachine machine = new StubMachine();
        StsHandler handler = new StsHandler(machine, client);
        List<CapabilityState> capabilities = new ArrayList<>();
        final String policyString = "draft/sts=" + StsPolicy.POLICY_OPTION_KEY_PORT + "=cats";
        capabilities.add(new DefaultCapabilityState(client, policyString));
        List<ServerMessage> messages = new ArrayList<>();
        messages.add(new DefaultServerMessage(":test.kitteh CAP ^o^ LS :" + policyString, new ArrayList<>()));
        Assertions.assertThrows(KittehServerMessageException.class,
                () -> handler.onCapLs(new CapabilitiesSupportedListEvent(client, messages, true, capabilities)));
    }

    /**
     * Checks an error is raised for an missing port value.
     */
    @Test
    public void testHandlerWithMissingPortValue() {
        final FakeClient client = new FakeClient();
        client.setSecure(false);
        final StubMachine machine = new StubMachine();
        StsHandler handler = new StsHandler(machine, client);
        List<CapabilityState> capabilities = new ArrayList<>();
        final String policyString = "draft/sts=" + StsPolicy.POLICY_OPTION_KEY_PORT;
        capabilities.add(new DefaultCapabilityState(client, policyString));
        List<ServerMessage> messages = new ArrayList<>();
        messages.add(new DefaultServerMessage(":test.kitteh CAP ^o^ LS :" + policyString, new ArrayList<>()));
        Assertions.assertThrows(KittehServerMessageException.class,
                () -> handler.onCapLs(new CapabilitiesSupportedListEvent(client, messages, true, capabilities)));
    }

    private static class StubMachine implements StsMachine {

        private StsClientState state = StsClientState.UNKNOWN;
        private StsPolicy policy;

        @Override
        public @NonNull StsClientState getCurrentState() {
            return this.state;
        }

        @Override
        public void setCurrentState(@NonNull StsClientState newState) {
            this.state = newState;
        }

        @Override
        public @NonNull StsStorageManager getStorageManager() {
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
        public void setStsPolicy(@NonNull StsPolicy policy) {
            this.policy = policy;
        }

        StsPolicy getPolicy() {
            return this.policy;
        }
    }
}
