/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.implementation;

import java.io.IOException;
import java.util.Properties;

/**
 * A POJO representation of the bundled kicl.properties file. 
 */
public class KICLProperties {

    public static final String DEFAULT_VERSION = "unknown";

    private static Properties properties;

    static {
        try {
            properties = new Properties(createDefaults());
            properties.load(KICLProperties.class.getResourceAsStream("/kicl.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Properties createDefaults() {
        Properties props = new Properties();
        props.put("version", DEFAULT_VERSION);
        return props;
    }

    /**
     * Returns the KittehIRCClientLib version during the compile-time. If the metadata file is not present,
     * {@link #DEFAULT_VERSION} is returned instead.
     *
     * @return version as string
     */
    public static String getVersion() {
        return properties.getProperty("version");
    }
}
