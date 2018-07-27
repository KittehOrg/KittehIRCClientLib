/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.util;

import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SimpleTrustManagerFactory;
import io.netty.util.internal.EmptyArrays;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * A very friendly, accepting trust manager factory. Allows anything through.
 *
 * @see Client.Builder#secureTrustManagerFactory(TrustManagerFactory)
 */
public final class AcceptingTrustManagerFactory extends SimpleTrustManagerFactory {
    private class TrustingManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String s) {
            // NOOP
        }

        @Override
        public void checkServerTrusted(@NonNull X509Certificate[] chain, @NonNull String authType) throws CertificateException {
            // NOOP
            // Allow ALL the things!
        }

        @NonNull
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return EmptyArrays.EMPTY_X509_CERTIFICATES;
        }

        @NonNull
        @Override
        public String toString() {
            return new ToStringer(this).toString();
        }
    }

    private final TrustManager trustManager = new TrustingManager();

    @NonNull
    @Override
    protected TrustManager[] engineGetTrustManagers() {
        return new TrustManager[]{this.trustManager};
    }

    @Override
    protected void engineInit(KeyStore keyStore) throws Exception {
        // NOOP
    }

    @Override
    protected void engineInit(ManagerFactoryParameters managerFactoryParameters) throws Exception {
        // NOOP
    }

    /**
     * Tests if {@code factory} produces instances of a known
     * insecure (accepting) trust manager.
     *
     * @param factory the factory
     * @return true if the factory is insecure, false if not
     */
    public static boolean isInsecure(TrustManagerFactory factory) {
        return (factory instanceof AcceptingTrustManagerFactory) || (factory instanceof InsecureTrustManagerFactory);
    }
}
