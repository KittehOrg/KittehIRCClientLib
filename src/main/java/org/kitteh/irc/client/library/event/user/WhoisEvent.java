/*
 * * Copyright (C) 2013-2019 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.event.user;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.WhoisData;
import org.kitteh.irc.client.library.event.abstractbase.ClientEventBase;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

/**
 * The server has sent WHOIS data on a user.
 */
public class WhoisEvent extends ClientEventBase {
    private final WhoisData whoisData;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param whoisData the WHOIS data
     */
    public WhoisEvent(@NonNull Client client, @NonNull WhoisData whoisData) {
        super(client);
        this.whoisData = Sanity.nullCheck(whoisData, "Whois data cannot be null");
    }

    /**
     * Gets the provided WHOIS data.
     *
     * @return the WHOIS data
     */
    public @NonNull WhoisData getWhoisData() {
        return this.whoisData;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("whoisData", this.whoisData);
    }
}
