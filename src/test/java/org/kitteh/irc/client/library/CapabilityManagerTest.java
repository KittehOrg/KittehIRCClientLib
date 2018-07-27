package org.kitteh.irc.client.library;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.feature.CapabilityManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Tests the CapabilityManager.
 */
public class CapabilityManagerTest {
    /**
     * Test default getters.
     */
    @Test
    public void testDefaultGetCapabilityMethodsInCapabilityManager() {
        StubCapabilityManager sut = new StubCapabilityManager();
        Assert.assertEquals(2, sut.getCapabilities().size());
        Assert.assertTrue(sut.getCapability("Test1").isPresent());
        Assert.assertTrue(sut.getSupportedCapability("Test1").isPresent());
        Assert.assertFalse(sut.getSupportedCapability("Test2").isPresent());
        Assert.assertFalse(sut.getCapability("Cats").isPresent());
    }

    /**
     * Tests ability to acquire natively supported capabilities.
     */
    @Test
    public void testNativeCapabilityRetrieval() {
        List<String> caps = CapabilityManager.Defaults.getDefaults();
        Assert.assertFalse(caps.isEmpty());
        Assert.assertTrue(caps.contains(CapabilityManager.Defaults.ACCOUNT_NOTIFY));
    }

    /**
     * Tests the constructor's ability to construct.
     *
     * @throws Exception if horrible things happen
     */
    @Test
    public void testConstructorIsPrivate() throws Exception {
        Constructor<CapabilityManager.Defaults> constructor = CapabilityManager.Defaults.class.getDeclaredConstructor();
        Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    /**
     * Stubby manager.
     */
    public class StubCapabilityManager implements CapabilityManager {
        @NonNull
        @Override
        public List<CapabilityState> getCapabilities() {
            return Arrays.asList(new TestCapabilityState1(), new TestCapabilityState2());
        }

        @NonNull
        @Override
        public List<CapabilityState> getSupportedCapabilities() {
            return Collections.singletonList(new TestCapabilityState1());
        }

        private class TestCapabilityState1 implements CapabilityState {
            @Override
            public boolean isDisabled() {
                return false;
            }

            @NonNull
            @Override
            public String getName() {
                return "Test1";
            }

            @NonNull
            @Override
            public Optional<String> getValue() {
                return Optional.empty();
            }

            @NonNull
            @Override
            public Client getClient() {
                return null;
            }

            @Override
            public long getCreationTime() {
                return 0;
            }
        }

        private class TestCapabilityState2 implements CapabilityState {
            @Override
            public boolean isDisabled() {
                return false;
            }

            @NonNull
            @Override
            public String getName() {
                return "Test2";
            }

            @NonNull
            @Override
            public Optional<String> getValue() {
                return Optional.empty();
            }

            @NonNull
            @Override
            public Client getClient() {
                return null;
            }

            @Override
            public long getCreationTime() {
                return 0;
            }
        }
    }
}
