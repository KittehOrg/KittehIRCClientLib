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
package org.kitteh.irc.client.library.auth.protocol;

import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.util.CommandFilter;

import javax.annotation.Nonnull;

/**
 * SASL EXTERNAL authentication. Automatically attempts auth during connection.
 */
public class SaslExternal extends AbstractSaslProtocol<Object> {
    private class Listener extends AbstractSaslProtocol<Object>.Listener {
        @CommandFilter("AUTHENTICATE")
        @Handler(filters = @Filter(CommandFilter.Filter.class))
        @Override
        public void authenticate(ClientReceiveCommandEvent event) {
            SaslExternal.this.getClient().sendRawLineImmediately("AUTHENTICATE +");
        }
    }

    private Listener listener;

    /**
     * Creates an instance.
     *
     * @param client client
     */
    public SaslExternal(@Nonnull Client client) {
        super(client, "", new Object(), "EXTERNAL"); // hacky?
    }

    @Nonnull
    @Override
    protected String getAuthLine() {
        return "";
    }

    @Nonnull
    @Override
    public Object getEventListener() {
        return (this.listener == null) ? (this.listener = new Listener()) : this.listener;
    }
}
