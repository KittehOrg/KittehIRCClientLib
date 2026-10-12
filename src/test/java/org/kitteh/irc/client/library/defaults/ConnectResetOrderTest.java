package org.kitteh.irc.client.library.defaults;

import net.engio.mbassy.listener.Handler;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageType;
import org.kitteh.irc.client.library.feature.network.ClientConnection;
import org.kitteh.irc.client.library.feature.network.NetworkHandler;
import org.kitteh.irc.client.library.feature.network.Resolver;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests that the per-connection state reset on connect is processed before
 * any line from the new connection, even one arriving before connect returns.
 */
public class ConnectResetOrderTest {
    private Client client;

    @BeforeEach
    public void setUp() {
        NetworkHandler handler = new NetworkHandler() {
            @Override
            public @NonNull ClientConnection connect(Client.@NonNull WithManagement client) {
                // A server reply that beats connect() returning
                client.processLine(":fake.srv 005 Kitteh NETWORK=New :are supported by this server");
                return new ClientConnection() {
                    @Override
                    public boolean isAlive() {
                        return true;
                    }

                    @Override
                    public void startPing() {
                    }

                    @Override
                    public void shutdown(DefaultMessageType messageType, boolean reconnect) {
                    }

                    @Override
                    public void shutdown(@Nullable String message, boolean reconnect) {
                    }
                };
            }

            @Override
            public @NonNull Resolver getResolver() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setResolver(@NonNull Resolver resolver) {
            }
        };
        this.client = Client.builder().management().networkHandler(handler).then().build();
    }

    @AfterEach
    public void tearDown() {
        this.client.shutdown();
    }

    public static class Sync {
        private final CountDownLatch latch = new CountDownLatch(1);

        @Handler
        public void command(ClientReceiveCommandEvent event) {
            if ("NOTICE".equals(event.getCommand())) {
                this.latch.countDown();
            }
        }
    }

    @Test
    public void testResetBeforeNewConnectionLines() throws InterruptedException {
        Sync sync = new Sync();
        this.client.getEventManager().registerEventListener(sync);
        this.client.connect();
        // Input is processed in order, so once this is handled the 005 has been too
        ((Client.WithManagement) this.client).processLine(":fake.srv NOTICE Kitteh :sync");
        Assertions.assertTrue(sync.latch.await(5, TimeUnit.SECONDS));
        Assertions.assertEquals(Optional.of("New"), this.client.getServerInfo().getNetworkName());
    }
}
