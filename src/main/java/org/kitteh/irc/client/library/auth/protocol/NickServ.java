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
import org.kitteh.irc.client.library.IRCFormat;
import org.kitteh.irc.client.library.auth.protocol.element.EventListening;
import org.kitteh.irc.client.library.auth.protocol.element.NickReclamation;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.user.PrivateNoticeEvent;
import org.kitteh.irc.client.library.util.NumericFilter;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;

/**
 * NickServ protocol. Automatically attempts to identify upon connection.
 */
public class NickServ extends AbstractUserPassProtocol implements EventListening, NickReclamation {
    private class Listener {
        @NumericFilter(4)
        @Handler(filters = @Filter(NumericFilter.Filter.class))
        public void listenVersion(ClientReceiveNumericEvent event) {
            NickServ.this.startAuthentication();
        }

        @Handler
        public void listenSuccess(PrivateNoticeEvent event) {
            if (event.getActor().getNick().equals(NickServ.this.getNickServNick())) {
                if (event.getMessage().startsWith("You are now identified")) {
                    int first;
                    String username = event.getMessage().substring((first = event.getMessage().indexOf(IRCFormat.BOLD.toString()) + 1), event.getMessage().indexOf(IRCFormat.BOLD.toString(), first));
                    // TODO do something with this information
                }
            }
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).toString();
        }
    }

    private final Listener listener = new Listener();

    /**
     * Creates a NickServ authentication protocol instance.
     *
     * @param client client for which this will be used
     * @param username username
     * @param password password
     */
    public NickServ(@Nonnull Client client, @Nonnull String username, @Nonnull String password) {
        super(client, username, password);
    }

    @Nonnull
    @Override
    protected String getAuthentication() {
        return "PRIVMSG " + this.getNickServNick() + " :IDENTIFY " + this.getUsername() + ' ' + this.getPassword();
    }

    @Nonnull
    @Override
    public Object getEventListener() {
        return this.listener;
    }

    /**
     * Gets the expected NickServ nickname.
     *
     * @return nick of NickServ
     */
    @Nonnull
    protected String getNickServNick() {
        return "NickServ";
    }
}
