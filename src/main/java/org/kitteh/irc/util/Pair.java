package org.kitteh.irc.util;

/**
 * A pair of objects!
 *
 * @param <A> Type of the first object
 * @param <B> Type of the second object
 */
public final class Pair<A, B> {
    private final A a;
    private final B b;

    /**
     * Constructs a pair of objects
     *
     * @param a first object
     * @param b second object
     */
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Gets the first object of this pair.
     *
     * @return first object
     */
    public A getA() {
        return this.a;
    }

    /**
     * Gets the second object of this pair.
     *
     * @return second object
     */
    public B getB() {
        return this.b;
    }
}