/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.auth.protocol.element;

import org.kitteh.irc.client.library.auth.protocol.AuthProtocol;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;

/**
 * Support for reclaiming a nickname.
 */
public interface NickReclamation extends AuthProtocol {
    /**
     * Forcibly taking back a nickname.
     *
     * @param nick nickname to ghost
     */
    default void ghostNick(@Nonnull String nick) {
        Sanity.safeMessageCheck(nick, "Nick");
        this.getClient().sendRawLine("NickServ :GHOST " + nick);
    }

    /**
     * Regaining a nickname.
     *
     * @param nick nickname to regain
     */
    default void regainNick(@Nonnull String nick) {
        Sanity.safeMessageCheck(nick, "Nick");
        this.getClient().sendRawLine("NickServ :REGAIN " + nick);
    }
}