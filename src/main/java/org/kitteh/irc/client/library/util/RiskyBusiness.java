/*
 * * Copyright (C) 2013-2023 Matt Baxter https://kitteh.org
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * It's not personal, Kitteh. It's strictly business.
 */
public final class RiskyBusiness {
    private RiskyBusiness() {
    }

    /**
     * A function that needs catching.
     *
     * @param <Input> input type
     * @param <Output> output type
     */
    @FunctionalInterface
    public interface CheckedFunction<Input, Output> {
        /**
         * Takes input, gives output.
         *
         * @param input input
         * @return output
         * @throws Exception if something goes wrong
         */
        @Nullable Output apply(@Nullable Input input) throws Exception;
    }

    /**
     * Throws an assertion error if the function throws an exception. Don't
     * use unless you are 100% certain it'll succeed 100% of the time.
     *
     * @param function function to execute
     * @param input input to process
     * @param <Input> input type
     * @param <Output> output type
     * @return output of the function
     */
    public static <Input, Output> @Nullable Output assertSafe(@NonNull CheckedFunction<Input, Output> function, @Nullable Input input) {
        Sanity.nullCheck(function, "Function");
        try {
            return function.apply(input);
        } catch (Exception everythingYouKnowIsWrong) {
            throw new AssertionError(everythingYouKnowIsWrong);
        }
    }
}
