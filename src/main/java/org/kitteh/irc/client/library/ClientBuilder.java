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
package org.kitteh.irc.client.library;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.InetAddress;
import java.util.function.Consumer;

// TODO Optional
/**
 * Builds {@link Client}s. Create a builder with {@link Client#builder()}.
 */
public interface ClientBuilder extends Cloneable {
    /**
     * Sets values for authentication with services on the server.
     *
     * @param authType type of authentication (See {@link AuthType})
     * @param username username
     * @param password password
     * @return this builder
     * @throws IllegalArgumentException for null parameters
     * @see AuthManager for managing authentication later
     */
    @Nonnull
    ClientBuilder auth(@Nonnull AuthType authType, @Nonnull String username, @Nonnull String password);

    /**
     * Binds the client to a host or IP locally. Null for wildcard binding.
     * <p>
     * By default, the host is null for wildcard binding.
     *
     * @param host host to bind to or null for wildcard
     * @return this builder
     */
    @Nonnull
    ClientBuilder bind(@Nullable String host);

    /**
     * Binds the client to the specified port. Invalid ports are set to 0.
     * <p>
     * By default, the port is 0.
     *
     * @param port port to bind to
     * @return this builder
     */
    @Nonnull
    ClientBuilder bind(int port);

    /**
     * Sets a listener for all thrown exceptions on this client.
     * <p>
     * All exceptions are passed from a single, separate thread.
     *
     * @param listener catcher of throwable objects
     * @return this builder
     */
    @Nonnull
    ClientBuilder listenException(@Nullable Consumer<Exception> listener);

    /**
     * Sets a listener for all incoming messages from the server.
     * <p>
     * All messages are passed from a single, separate thread.
     *
     * @param listener input listener
     * @return this builder
     */
    @Nonnull
    ClientBuilder listenInput(@Nullable Consumer<String> listener);

    /**
     * Sets a listener for all outgoing messages to the server.
     * <p>
     * All messages are passed from a single, separate thread.
     *
     * @param listener output listener
     * @return this builder
     */
    @Nonnull
    ClientBuilder listenOutput(@Nullable Consumer<String> listener);

    /**
     * Names the client, for internal labeling.
     *
     * @param name a name to label the client internally
     * @return this builder
     * @throws IllegalArgumentException if name is null
     */
    @Nonnull
    ClientBuilder name(@Nonnull String name);

    /**
     * Sets the client's nick.
     * <p>
     * By default, the nick is Kitteh.
     *
     * @param nick nick for the client to use
     * @return this builder
     * @throws IllegalArgumentException if nick is null
     */
    @Nonnull
    ClientBuilder nick(@Nonnull String nick);

    /**
     * Sets the server password.
     * <p>
     * If not set, no password is sent
     *
     * @param password server password
     * @return this builder
     */
    @Nonnull
    ClientBuilder serverPassword(@Nullable String password);

    /**
     * Sets the realname the client uses.
     * <p>
     * By default, the realname is Kitteh.
     *
     * @param name realname to use
     * @return this builder
     * @throws IllegalArgumentException for null realname
     */
    @Nonnull
    ClientBuilder realName(@Nonnull String name);

    /**
     * Sets whether the client connects via SSL.
     *
     * @param ssl true for ssl
     * @return this builder
     */
    @Nonnull
    ClientBuilder secure(boolean ssl);

    /**
     * Sets the key for SSL connection.
     *
     * @param keyCertChainFile X.509 certificate chain file in PEM format
     * @return this builder
     * @see #secure(boolean)
     */
    @Nonnull
    ClientBuilder secureKeyCertChain(@Nullable File keyCertChainFile);

    /**
     * Sets the private key for SSL connection.
     *
     * @param keyFile PKCS#8 private key file in PEM format
     * @return this builder
     * @see #secure(boolean)
     */
    @Nonnull
    ClientBuilder secureKey(@Nullable File keyFile);

    /**
     * Sets the private key password for SSL connection.
     *
     * @param password password for private key
     * @return this builder
     * @see #secure(boolean)
     */
    @Nonnull
    ClientBuilder secureKeyPassword(@Nullable String password);

    /**
     * Sets the delay between messages being sent to the server
     *
     * @param delay the delay in milliseconds
     * @return this builder
     */
    @Nonnull
    ClientBuilder messageDelay(int delay);

    /**
     * Sets the server IP to which the client will connect.
     * <p>
     * By default, the port is 6667.
     *
     * @param port IRC server port
     * @return this builder
     */
    @Nonnull
    ClientBuilder server(int port);

    /**
     * Sets the server host to which the client will connect.
     * <p>
     * By default, the host is localhost.
     *
     * @param host IRC server host
     * @return this builder
     * @throws IllegalArgumentException for null host
     */
    @Nonnull
    ClientBuilder server(@Nonnull String host);

    /**
     * Sets the user the client connects as.
     * <p>
     * By default, the user is Kitteh.
     *
     * @param user user to connect as
     * @return this builder
     * @throws IllegalArgumentException for null user
     */
    @Nonnull
    ClientBuilder user(@Nonnull String user);

    /**
     * Sets all the information for, and enables, WebIRC.
     * <p>
     * By default, WebIRC is disabled.
     *
     * @param password password as defined in the IRCd config
     * @param user username part of the client's address
     * @param host hostname part of the client's address
     * @param ip client's IP address
     * @return this builder
     * @throws IllegalArgumentException for any null parameters
     * @see #webircDisable()
     */
    @Nonnull
    ClientBuilder webirc(@Nonnull String password, @Nonnull String user, @Nonnull String host, @Nonnull InetAddress ip);

    /**
     * Disables WebIRC.
     *
     * @return this builder
     * @see #webirc(String, String, String, InetAddress)
     */
    @Nonnull
    ClientBuilder webircDisable();

    /**
     * Clientmaker, clientmaker, make me a client!
     *
     * @return a client designed to your liking
     */
    @Nonnull
    Client build();
}