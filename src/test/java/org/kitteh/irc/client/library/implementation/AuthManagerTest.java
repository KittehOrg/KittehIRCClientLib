package org.kitteh.irc.client.library.implementation;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.auth.AuthProtocol;
import org.kitteh.irc.client.library.feature.auth.element.EventListening;
import org.kitteh.irc.client.library.feature.defaultmanager.DefaultAuthManager;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Tests the auth manager implementation.
 */
public class AuthManagerTest {
    /**
     * Tests a null protocol failure.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFailureWithNullProtocol() {
        Client client = new FakeClient();
        DefaultAuthManager sut = new DefaultAuthManager(client);
        sut.addProtocol(null);
    }

    /**
     * Tests protocol adding.
     */
    @Test
    public void testAddProtocolManagementMethods() {
        final Client client = new FakeClient();
        DefaultAuthManager sut = new DefaultAuthManager(client);
        final AuthProtocol ap = new AuthProtocol() {
            @Nonnull
            @Override
            public Client getClient() {
                return client;
            }

            @Override
            public void startAuthentication() {
                // do nothing
            }
        };
        sut.addProtocol(ap);
        Assert.assertEquals(1, sut.getProtocols().size());
        Assert.assertEquals(ap, sut.getProtocols().iterator().next());
        Assert.assertFalse(sut.toString().isEmpty());
        sut.removeProtocol(ap);
        Assert.assertEquals(0, sut.getProtocols().size());
        Assert.assertFalse(sut.getProtocols().iterator().hasNext());
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
        Assert.assertTrue(ret.isPresent());
        Assert.assertTrue(ret.get() == stub1);
        Optional<AuthProtocol> removed = sut.addProtocol(new AuthProtocol() {
            @Nonnull
            @Override
            public Client getClient() {
                return client;
            }

            @Override
            public void startAuthentication() {
                // do nothing
            }
        });
        Assert.assertFalse(removed.isPresent());
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
        Assert.assertTrue(stub.wasTripped());
    }

    private class StubAuthProtocol implements AuthProtocol, EventListening {
        private final InternalClient client;
        private boolean tripped = false;

        StubAuthProtocol() {
            this.client = Mockito.mock(InternalClient.class);
        }

        @Nonnull
        @Override
        public Client getClient() {
            return this.client;
        }

        @Override
        public void startAuthentication() {

        }

        @Nonnull
        @Override
        public Object getEventListener() {
            this.tripped = true;
            return new EventListener(this.client);
        }

        public boolean wasTripped() {
            return this.tripped;
        }
    }
}
