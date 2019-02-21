package org.kitteh.irc.client.library.util;

import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.TrustManagerFactory;

/**
 * Tests the SSLUtil class.
 */
public class SslUtilTest {
    /**
     * Test the isInsecure function.
     */
    @Test
    public void testSecure1() {
        Assert.assertTrue(SslUtil.isInsecure(InsecureTrustManagerFactory.INSTANCE));
    }

    /**
     * Test the isInsecure function.
     *
     * @throws Exception Oh no
     */
    @Test
    public void testSecure3() throws Exception {
        Assert.assertFalse(SslUtil.isInsecure(TrustManagerFactory.getInstance("PKIX")));
    }
}
