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
package org.kitteh.irc.client.library.util;

import org.kitteh.irc.client.library.exception.KittehSTSException;
import org.kitteh.irc.client.library.feature.sts.STSPropertiesStorageManager;
import org.kitteh.irc.client.library.feature.sts.STSStorageManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility methods for dealing with STS.
 */
public final class STSUtil {

    /**
     * Default filename to use for the properties file.
     */
    public static final String DEFAULT_FILENAME = "kicl_sts.properties";

    private STSUtil() {
    }

    /**
     * Gets the default storage implementation using a file in the home directory.
     *
     * @return an STSStorageManager implementer
     */
    public static STSStorageManager getDefaultStorageManager() {
        return STSUtil.getDefaultStorageManager(new File(System.getProperty("user.home"), DEFAULT_FILENAME));
    }

    /**
     * Gets the default storage implementation using an alternative file.
     *
     * @param stsFile File instance
     * @return an STSStorageManager implementer
     */
    public static STSStorageManager getDefaultStorageManager(File stsFile) {
        try {
            FileWriter fileWriter = new FileWriter(stsFile);
            FileReader fileReader = new FileReader(stsFile);
            return new STSPropertiesStorageManager(fileWriter, fileReader);
        } catch (IOException e) {
            throw new KittehSTSException(e.getMessage());
        }
    }

}
