/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.defaults.listener;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.DefaultEventListener;
import org.kitteh.irc.client.library.feature.EventListenerSupplier;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Default event listeners registered by the client.
 */
public enum DefaultListeners implements EventListenerSupplier {
    /**
     * I'M FULL OF THINGS THAT HAVEN'T BEEN ISOLATED YET!
     *
     * @see DefaultEventListener
     */
    DEFAULT(DefaultEventListener::new), // TODO get rid of this one
    /**
     * WHO handling.
     *
     * @see DefaultWhoListener
     */
    WHO(DefaultWhoListener::new),
    /**
     * WHOIS handling.
     *
     * @see DefaultWhoisListener
     */
    WHOIS(DefaultWhoisListener::new);

    private final Function<Client.WithManagement, Object> function;

    DefaultListeners(@Nonnull Function<Client.WithManagement, Object> function) {
        this.function = function;
    }

    @Override
    public Function<Client.WithManagement, Object> getConstructingFunction() {
        return this.function;
    }
}
