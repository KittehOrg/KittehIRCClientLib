package org.kitteh.irc.client.library.implementation;

import org.junit.Assert;
import org.junit.Test;

public class KICLPropertiesTest {

    @Test
    public void testVersion() throws Exception {
        Assert.assertNotEquals(KICLProperties.DEFAULT_VERSION, KICLProperties.getVersion());
    }
}
