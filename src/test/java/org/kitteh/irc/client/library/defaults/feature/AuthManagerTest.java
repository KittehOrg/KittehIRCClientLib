package org.kitteh.irc.client.library.defaults.feature;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.FakeClient;
import org.kitteh.irc.client.library.feature.auth.AuthProtocol;
import org.kitteh.irc.client.library.feature.auth.element.EventListening;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * Tests the auth manager implementation.
 */
public class AuthManagerTest {
    /**
     * Tests a null protocol failure.
     */
    @Test
    public void testFailureWithNullProtocol() {
        Client client = new FakeClient();
        DefaultAuthManager sut = new DefaultAuthManager(client);
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.addProtocol(null));
    }

    /**
     * Tests protocol adding.
     */
    @Test
    public void testAddProtocolManagementMethods() {
        final Client client = new FakeClient();
        DefaultAuthManager sut = new DefaultAuthManager(client);
        final AuthProtocol ap = new AuthProtocol() {
            @Override
            public @NonNull Client getClient() {
                return client;
            }

            @Override
            public void startAuthentication() {
                // do nothing
            }
        };
        sut.addProtocol(ap);
        Assertions.assertEquals(1, sut.getProtocols().size());
        Assertions.assertEquals(ap, sut.getProtocols().iterator().next());
        Assertions.assertFalse(sut.toString().isEmpty());
        sut.removeProtocol(ap);
        Assertions.assertEquals(0, sut.getProtocols().size());
        Assertions.assertFalse(sut.getProtocols().iterator().hasNext());
    }

    /**
     * Tests protocol adding multiple.
     */
    @Test
    public void testAddProtocolOfSameTypeMethods() {
        final Client client = new FakeClient();
        DefaultAuthManager sut = new DefaultAuthManager(client);
        StubAuthProtocol stub1 = new StubAuthProtocol();
        sut.addProtocol(stub1);
        StubAuthProtocol stub2 = new StubAuthProtocol();
        final Optional<AuthProtocol> ret = sut.addProtocol(stub2);
        Assertions.assertTrue(ret.isPresent());
        Assertions.assertSame(ret.get(), stub1);
        Optional<AuthProtocol> removed = sut.addProtocol(new AuthProtocol() {
            @Override
            public @NonNull Client getClient() {
                return client;
            }

            @Override
            public void startAuthentication() {
                // do nothing
            }
        });
        Assertions.assertFalse(removed.isPresent());
    }

    /**
     * Tests listener addition.
     */
    @Test
    public void testEventListeningAuthProtocol() {
        final Client client = new FakeClient();
        DefaultAuthManager sut = new DefaultAuthManager(client);
        final StubAuthProtocol stub = new StubAuthProtocol();
        sut.addProtocol(stub);
        sut.removeProtocol(stub);
        Assertions.assertTrue(stub.wasTripped());
    }

    private static class StubAuthProtocol implements AuthProtocol, EventListening {
        private final Client.WithManagement client;
        private boolean tripped = false;

        StubAuthProtocol() {
            this.client = Mockito.mock(Client.WithManagement.class);
        }

        @Override
        public @NonNull Client getClient() {
            return this.client;
        }

        @Override
        public void startAuthentication() {

        }

        @Override
        public @NonNull Object getEventListener() {
            this.tripped = true;
            return new Object();
        }

        public boolean wasTripped() {
            return this.tripped;
        }
    }
}
