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
package org.kitteh.irc.client.library.event.client;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.ClientEvent;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * Fires when connecting to a server via SSL. By default approves any certs.
 * Basically we're just letting you decide on {@link
 * X509TrustManager#checkServerTrusted}. Instead of throwing an exception,
 * just use {@link #setDenied} and one will be thrown for you.
 */
public class SSLCertificateAcceptEvent extends ClientEvent {
    private final String authType;
    private final X509Certificate[] chain;
    private boolean denied = false;

    /**
     * Constructs the event.
     *
     * @param client client for which this is occurring
     * @param authType auth type
     * @param chain certificate chain
     */
    public SSLCertificateAcceptEvent(Client client, String authType, X509Certificate[] chain) {
        super(client);
        this.authType = authType;
        this.chain = chain;
    }

    /**
     * Gets the auth type.
     *
     * @return auth type
     */
    public String getAuthType() {
        return authType;
    }

    /**
     * Gets the certificate chain.
     *
     * @return the cert chain
     */
    public X509Certificate[] getChain() {
        return chain.clone();
    }

    /**
     * Gets if this connection is being denied.
     *
     * @return true if set to deny connection
     */
    public boolean isDenied() {
        return denied;
    }

    /**
     * Sets this connection as denied.
     *
     * @param denied true if denying
     */
    public void setDenied(boolean denied) {
        this.denied = denied;
    }
}