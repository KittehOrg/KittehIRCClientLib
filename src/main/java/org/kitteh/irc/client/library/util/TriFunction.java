/*
 * * Copyright (C) 2013-2018 Matt Baxter http://kitteh.org
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

import javax.annotation.Nullable;

/**
 * Represents a function that accepts three arguments and produces a result.
 *
 * @param <First> the type of the first argument to the function
 * @param <Second> the type of the second argument to the function
 * @param <Third> the type of the second argument to the function
 * @param <Result> the type of the result of the function
 */
@FunctionalInterface
public interface TriFunction<First, Second, Third, Result> {
    /**
     * Applies this function to the given arguments.
     *
     * @param first the first function argument
     * @param second the second function argument
     * @param third the third function argument
     * @return the function result
     */
    @Nullable
    Result apply(@Nullable First first, @Nullable Second second, @Nullable Third third);
}
