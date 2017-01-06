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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public final class IPUtil {
    private static final BigInteger X_FFFF = BigInteger.valueOf(0xFFFF);
    // Length of (2**128) - 1 -- IPv6 has up to 2**128 addresses
    private static final int LENGTH_OF_LARGEST_IPV6_INTEGER = 39;

    private IPUtil() {
    }

    /**
     * Takes a string containing an integer (potentially larger than 32-bits) and converts it to the appropriate IP address class.
     *
     * @param ipInteger the IP integer string
     * @return the corresponding IP address as an InetAddress instance
     */
    public static InetAddress getInetAdressFromIntString(String ipInteger) {
        if (ipInteger.length() > LENGTH_OF_LARGEST_IPV6_INTEGER) {
            // prevent attacks with large strings to allocate large BigIntegers
            throw new IllegalArgumentException("IP " + ipInteger + " is too big to be an address");
        }
        BigInteger ipInt;
        try {
            ipInt = new BigInteger(ipInteger);
        } catch (NumberFormatException invalidInt) {
            // try returning it as a raw IP -- some clients send like this
            try {
                // Prevent hostname lookup by filtering for literals
                // IPv4 addresses have 3 dots (0.0.0.0) which means 4 split sections
                // IPv6 literals must start with a [
                String[] ipv4Split = ipInteger.split("\\.");
                boolean isIpv4 = ipv4Split.length == 4;
                boolean isIpv6 = ipInteger.startsWith("[");
                if (!(isIpv4 || isIpv6)) {
                    throw invalidInt;
                }
                if (isIpv4) {
                    // Check all the IPv4 parts are just numbers
                    for (String ipv4Part : ipv4Split) {
                        if (!ipv4Part.codePoints().allMatch(Character::isDigit)) {
                            throw invalidInt;
                        }
                    }
                }
                return InetAddress.getByName(ipInteger);
            } catch (UnknownHostException invalidAddress) {
                throw new IllegalArgumentException(invalidAddress);
            }
        }
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
            InetAddress address = InetAddress.getByName(ip);
            if (address instanceof Inet6Address
                    && ((Inet6Address) address).isIPv4CompatibleAddress()) {
                // Un-wrap IPv6 -> IPv4 address (some systems are still IPv4 only and this helps compatibility)
                // To do so, just copy the last 4 parts of the IPv6 address
                return InetAddress.getByAddress(Arrays.copyOfRange(address.getAddress(), 12, 16));
            }
            return address;
        } catch (UnknownHostException e) {
            // should never happen, as we pass an IP
            throw new AssertionError("invalid IP " + ip, e);
        }
    }

    /**
     * Takes a InetAddress and converts it into a String containing an integer (potentially larger than 32-bits).
     *
     * @param address the IP address
     * @return the IP integer as a string
     */
    public static String getIntStringFromInetAddress(InetAddress address) {
        BigInteger addressLong = BigInteger.ZERO;
        byte[] bytes = address.getAddress();
        for (byte b : bytes) {
            addressLong = addressLong.shiftLeft(8).or(BigInteger.valueOf(b & 0xFF));
        }
        return addressLong.toString();
    }

    private static String numberToIPv6(BigInteger ipNumber) {
        // Open with [ for IPv6 literal
        StringBuilder ipString = new StringBuilder("[");

        for (int i = 0; i < 8; i++) {
            ipString.insert(1, ":");
            ipString.insert(1, ipNumber.and(X_FFFF).toString(16));

            ipNumber = ipNumber.shiftRight(16);
        }

        // replace last : with ] to close IPv6 literal
        ipString.setCharAt(ipString.length() - 1, ']');
        return ipString.toString();

    }

}
