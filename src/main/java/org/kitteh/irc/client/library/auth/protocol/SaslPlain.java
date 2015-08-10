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
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.auth.protocol.element.EventListening;
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesAcknowledgedEvent;
import org.kitteh.irc.client.library.event.capabilities.CapabilitiesSupportedListEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.helper.CapabilityNegotiationResponseEvent;
import org.kitteh.irc.client.library.util.CommandFilter;
import org.kitteh.irc.client.library.util.NumericFilter;

import javax.annotation.Nonnull;
import java.util.Base64;

/**
 * SASL PLAIN authentication.
 */
public class SaslPlain extends AbstractUserPassProtocol implements EventListening {
    private class Listener {
        @Handler(priority = 1)
        public void capList(CapabilitiesSupportedListEvent event) {
            if (event.getSupportedCapabilities().stream().filter(c -> c.getName().equalsIgnoreCase("sasl")).count() > 0) {
                new CapabilityRequestCommand(SaslPlain.this.getClient()).enable("sasl").execute();
            } else {
                SaslPlain.this.authenticating = false;
            }
        }

        @Handler
        public void capAck(CapabilitiesAcknowledgedEvent event) {
            if (event.getAcknowledgedCapabilities().stream().filter(c -> c.getName().equalsIgnoreCase("sasl")).count() > 0) {
                SaslPlain.this.startAuthentication();
            }
        }

        @Handler
        public void capEndable(CapabilityNegotiationResponseEvent event) {
            if (SaslPlain.this.authenticating) {
                event.setEndingNegotiation(false);
            }
        }

        @CommandFilter("AUTHENTICATE")
        @Handler(filters = @Filter(CommandFilter.Filter.class))
        public void authenticate(ClientReceiveCommandEvent event) {
            if (event.getArgs().length > 0) {
                String base64 = Base64.getEncoder().encodeToString((SaslPlain.this.getUsername() + '\u0000' + SaslPlain.this.getUsername() + '\u0000' + SaslPlain.this.getPassword()).getBytes());
                SaslPlain.this.getClient().sendRawLineImmediately("AUTHENTICATE " + base64);
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
            finish();
        }

        @NumericFilter(902)
        @NumericFilter(904)
        @NumericFilter(905)
        @NumericFilter(906)
        @NumericFilter(907)
        @NumericFilter(908)
        @Handler(filters = @Filter(NumericFilter.Filter.class))
        public void fail(ClientReceiveNumericEvent event) {
            finish();
        }

        private void finish() {
            SaslPlain.this.authenticating = false;
            SaslPlain.this.getClient().sendRawLineImmediately("CAP END"); // TODO event decision
        }
    }

    private final Listener listener = new Listener();
    private volatile boolean authenticating = false;

    /**
     * Creates an instance.
     *
     * @param client client
     * @param username username
     * @param password password
     */
    public SaslPlain(@Nonnull Client client, @Nonnull String username, @Nonnull String password) {
        super(client, username, password);
    }

    @Nonnull
    @Override
    protected String getAuthentication() {
        this.authenticating = true;
        return "AUTHENTICATE PLAIN";
    }

    @Nonnull
    @Override
    public Object getEventListener() {
        return this.listener;
    }
}