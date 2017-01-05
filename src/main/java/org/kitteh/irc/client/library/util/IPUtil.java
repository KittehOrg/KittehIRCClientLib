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

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class IPUtil {
    private IPUtil() {
    }

    /**
     * Takes a string containing an integer (potentially larger than 32-bits) and converts it to the appropriate IP address class.
     *
     * @param ipInteger the IP integer string
     * @return the corresponding IP address as an InetAddress instance
     */
    public static InetAddress getInetAdressFromIntString(String ipInteger) {
        BigInteger ipInt = new BigInteger(ipInteger);
        String ip;
        if (ipInt.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) < 0) {
            int ipv4Int = ipInt.intValueExact();
            // IPv4
            ip = String.format("%d.%d.%d.%d", (ipv4Int >> 24 & 0xff), (ipv4Int >> 16 & 0xff), (ipv4Int >> 8 & 0xff), (ipv4Int & 0xff));
        } else {
            // IPv6
            ip = numberToIPv6(ipInt);
        }
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            // should never happen, as we pass an IP
            throw new AssertionError("invalid IP " + ip, e);
        }
    }

    private static final BigInteger X_FFFF = BigInteger.valueOf(0xFFFF);

    private static String numberToIPv6(BigInteger ipNumber) {
        // Open with [ for IPv6 literal
        StringBuilder ipString = new StringBuilder("[");

        for (int i = 0; i < 8; i++) {
            ipString.insert(0, ":");
            ipString.insert(0, ipNumber.and(X_FFFF).toString(16));

            ipNumber = ipNumber.shiftRight(16);
        }

        // replace last : with ] to close IPv6 literal
        ipString.setCharAt(ipString.length(), ']');
        return ipString.toString();

    }

}
