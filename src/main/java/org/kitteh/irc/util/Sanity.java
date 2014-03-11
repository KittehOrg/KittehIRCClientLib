package org.kitteh.irc.util;

/**
 * .
 */
public final class Sanity {
    public static void nullCheck(Object o, String failMessage) {
        if (o == null) {
            throw new IllegalArgumentException(failMessage);
        }
    }

    public static void nullCheck(Object[] o, String failMessage) {
        Sanity.nullCheck((Object) o, failMessage);
        for (Object element : o) {
            Sanity.nullCheck(element, failMessage);
        }
    }

    public static void truthiness(boolean bool, String failMessage) {
        if (!bool) {
            throw new IllegalArgumentException(failMessage);
        }
    }
}