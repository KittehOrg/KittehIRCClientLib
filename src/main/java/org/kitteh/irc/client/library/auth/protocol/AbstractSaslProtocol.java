/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.irc.client.library.auth.protocol;

import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.CapabilityManager;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.auth.protocol.element.EventListening;
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesAcknowledgedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesRejectedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.helper.CapabilityNegotiationResponseEvent;
import org.kitteh.irc.client.library.util.CommandFilter;
import org.kitteh.irc.client.library.util.NumericFilter;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * SASL authentication. Automatically attempts auth during connection.
 *
 * @param <AuthValue> authentication value type
 */
public abstract class AbstractSaslProtocol<AuthValue> extends AbstractUserProtocol implements EventListening {
    protected class Listener {
        @Handler(priority = 1)
        public void capList(CapabilitiesSupportedListEvent event) {
            if (event.isNegotiating() && !AbstractSaslProtocol.this.authenticating) {
                Optional<CapabilityState> state = event.getSupportedCapabilities().stream().filter(c -> c.getName().equalsIgnoreCase(CapabilityManager.Defaults.SASL)).findFirst();
                if (!state.isPresent()) {
                    return;
                }
                if (state.get().getValue().isPresent()) {
                    if (Arrays.stream(state.get().getValue().get().split(",")).filter(mechanism -> mechanism.equalsIgnoreCase(AbstractSaslProtocol.this.saslType)).count() == 0) {
                        return; // Don't bother if it doesn't support our type
                    }
                }
                new CapabilityRequestCommand(AbstractSaslProtocol.this.getClient()).enable(CapabilityManager.Defaults.SASL).execute();
                event.setEndingNegotiation(false);
                AbstractSaslProtocol.this.authenticating = true;
            }
        }

        @Handler(priority = 1)
        public void capAck(CapabilitiesAcknowledgedEvent event) {
            if (event.getAcknowledgedCapabilities().stream().filter(c -> c.getName().equalsIgnoreCase(CapabilityManager.Defaults.SASL)).count() > 0) {
                AbstractSaslProtocol.this.startAuthentication();
            }
        }

        @Handler(priority = 1)
        public void capNak(CapabilitiesRejectedEvent event) {
            if (event.getRejectedCapabilitiesRequest().stream().filter(c -> c.getName().equalsIgnoreCase(CapabilityManager.Defaults.SASL)).count() > 0) {
                AbstractSaslProtocol.this.authenticating = false;
            }
        }

        @Handler
        public void capEndable(CapabilityNegotiationResponseEvent event) {
            if (AbstractSaslProtocol.this.authenticating) {
                event.setEndingNegotiation(false);
            }
        }

        @CommandFilter("AUTHENTICATE")
        @Handler(filters = @Filter(CommandFilter.Filter.class))
        public void authenticate(ClientReceiveCommandEvent event) {
            if (!event.getParameters().isEmpty()) {
                String base64 = Base64.getEncoder().encodeToString(AbstractSaslProtocol.this.getAuthLine().getBytes());
                AbstractSaslProtocol.this.getClient().sendRawLineImmediately("AUTHENTICATE " + base64);
            }
        }

        @NumericFilter(900)
        @Handler(filters = @Filter(NumericFilter.Filter.class))
        public void loggedIn(ClientReceiveNumericEvent event) {
            // TODO store account
        }

        @NumericFilter(903)
        @Handler(filters = @Filter(NumericFilter.Filter.class))
        public void success(ClientReceiveNumericEvent event) {
            this.finish();
        }

        @NumericFilter(902)
        @NumericFilter(904)
        @NumericFilter(905)
        @NumericFilter(906)
        @NumericFilter(907)
        @NumericFilter(908)
        @Handler(filters = @Filter(NumericFilter.Filter.class))
        public void fail(ClientReceiveNumericEvent event) {
            this.finish();
        }

        private void finish() {
            AbstractSaslProtocol.this.authenticating = false;
            AbstractSaslProtocol.this.getClient().sendRawLineImmediately("CAP END"); // TODO event decision
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).toString();
        }
    }

    private Listener listener;
    private final String saslType;
    private AuthValue authValue;
    private volatile boolean authenticating = false;

    /**
     * Creates an instance.
     *
     * @param client client
     * @param username username
     * @param authValue authentication value
     * @param saslType type of SASL auth
     */
    protected AbstractSaslProtocol(@Nonnull Client client, @Nonnull String username, @Nonnull AuthValue authValue, @Nonnull String saslType) {
        super(client, username);
        this.saslType = saslType;
        this.authValue = authValue;
    }

    @Nonnull
    @Override
    protected final String getAuthentication() {
        return "AUTHENTICATE " + this.saslType;
    }

    @Nonnull
    @Override
    public Object getEventListener() {
        return (this.listener == null) ? (this.listener = new Listener()) : this.listener;
    }

    /**
     * Gets the authentication value, be it a password or key.
     *
     * @return the authentication value
     */
    protected final AuthValue getAuthValue() {
        return this.authValue;
    }

    /**
     * Sets the authentication value.
     *
     * @param authValue the authentication value
     * @throws IllegalArgumentException if the value is null
     */
    protected final void setAuthValue(@Nonnull AuthValue authValue) {
        Sanity.nullCheck(authValue, "Auth value cannot be null");
        this.authValue = authValue;
    }

    /**
     * Gets the info to base64 encodein the first AUTHENTICATE message.
     *
     * @return value to encode
     */
    protected abstract String getAuthLine();
}
