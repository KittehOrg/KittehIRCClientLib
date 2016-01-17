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
package org.kitteh.irc.client.library;

import javax.annotation.Nonnull;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.net.InetAddress;
import java.util.function.Consumer;

/**
 * Builds {@link Client}s. Create a builder with {@link Client#builder()}.
 * <p>
 * The default built client connects securely via port 6697. See {@link
 * #secure(boolean)} to disable, or the other secure* methods in this builder
 * to fully utilize the feature. Note that the default TrustManagerFactory
 * does not support many certificate types (including StartCom) as Java does
 * not consider them valid and does not accept self-signed.
 */
public interface ClientBuilder extends Cloneable {
    /**
     * Removes the Consumer set to fire after the client is built.
     *
     * @return this builder
     */
    @Nonnull
    ClientBuilder afterBuildConsumerRemove();

    /**
     * Sets up a Consumer to fire on the newly created client after it is
     * built, prior to connection.
     *
     * @param consumer consumer
     * @return this builder
     */
    @Nonnull
    ClientBuilder afterBuildConsumer(@Nonnull Consumer<Client> consumer);

    /**
     * Binds the client to no specific host.
     *
     * @return this builder
     */
    @Nonnull
    ClientBuilder bindHostRemove();

    /**
     * Binds the client to a host or IP locally.
     * <p>
     * By default, the host is not set which results in wildcard binding.
     *
     * @param host host to bind to
     * @return this builder
     */
    @Nonnull
    ClientBuilder bindHost(@Nonnull String host);

    /**
     * Binds the client to the specified port. Invalid ports are set to 0.
     * <p>
     * By default, the port is 0.
     *
     * @param port port to bind to
     * @return this builder
     */
    @Nonnull
    ClientBuilder bindPort(int port);

    /**
     * Removes the exception listener from this builder.
     *
     * @return this builder
     */
    @Nonnull
    ClientBuilder listenExceptionRemove();

    /**
     * Sets a listener for all thrown exceptions on this client.
     * <p>
     * All exceptions are passed from a single, separate thread.
     *
     * @param listener catcher of throwable objects
     * @return this builder
     */
    @Nonnull
    ClientBuilder listenException(@Nonnull Consumer<Exception> listener);

    /**
     * Removes the input listener from this builder.
     *
     * @return this builder
     */
    @Nonnull
    ClientBuilder listenInputRemove();

    /**
     * Sets a listener for all incoming messages from the server.
     * <p>
     * All messages are passed from a single, separate thread.
     *
     * @param listener input listener
     * @return this builder
     */
    @Nonnull
    ClientBuilder listenInput(@Nonnull Consumer<String> listener);

    /**
     * Removes the output listener from this builder.
     *
     * @return this builder
     */
    @Nonnull
    ClientBuilder listenOutputRemove();

    /**
     * Sets a listener for all outgoing messages to the server.
     * <p>
     * All messages are passed from a single, separate thread.
     *
     * @param listener output listener
     * @return this builder
     */
    @Nonnull
    ClientBuilder listenOutput(@Nonnull Consumer<String> listener);

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
     * Removes the server password.
     * <p>
     * If not set, no password is sent
     *
     * @return this builder
     */
    @Nonnull
    ClientBuilder serverPasswordRemove();

    /**
     * Sets the server password.
     * <p>
     * If not set, no password is sent
     *
     * @param password server password
     * @return this builder
     */
    @Nonnull
    ClientBuilder serverPassword(@Nonnull String password);

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
     * <p>
     * Note that by default the TrustManager used does not accept the
     * certificates of many popular networks. You must use {@link
     * #secureTrustManagerFactory(TrustManagerFactory)} to set your own
     * TrustManagerFactory.
     *
     * @param ssl true for ssl
     * @return this builder
     */
    @Nonnull
    ClientBuilder secure(boolean ssl);

    /**
     * Removes the key for SSL connection.
     *
     * @return this builder
     */
    @Nonnull
    ClientBuilder secureKeyCertChainRemove();

    /**
     * Sets the key for SSL connection.
     *
     * @param keyCertChainFile X.509 certificate chain file in PEM format
     * @return this builder
     * @see #secure(boolean)
     */
    @Nonnull
    ClientBuilder secureKeyCertChain(@Nonnull File keyCertChainFile);

    /**
     * Removes the private key for SSL connection.
     *
     * @return this builder
     */
    @Nonnull
    ClientBuilder secureKeyRemove();

    /**
     * Sets the private key for SSL connection.
     *
     * @param keyFile PKCS#8 private key file in PEM format
     * @return this builder
     * @see #secure(boolean)
     */
    @Nonnull
    ClientBuilder secureKey(@Nonnull File keyFile);

    /**
     * Removes the private key password for SSL connection.
     *
     * @return this builder
     */
    @Nonnull
    ClientBuilder secureKeyPasswordRemove();

    /**
     * Sets the private key password for SSL connection.
     *
     * @param password password for private key
     * @return this builder
     * @see #secure(boolean)
     */
    @Nonnull
    ClientBuilder secureKeyPassword(@Nonnull String password);

    /**
     * Removes the {@link TrustManagerFactory} for SSL connection, defaulting
     * to the JRE factory.
     *
     * @return this builder
     */
    @Nonnull
    ClientBuilder secureTrustManagerFactoryRemove();

    /**
     * Sets the {@link TrustManagerFactory} for SSL connection.
     *
     * @param factory trust manager supplier
     * @return this builder
     * @see #secure(boolean)
     */
    @Nonnull
    ClientBuilder secureTrustManagerFactory(@Nonnull TrustManagerFactory factory);

    /**
     * Sets the delay between messages being sent to the server
     *
     * @param delay the delay in milliseconds
     * @return this builder
     */
    @Nonnull
    ClientBuilder messageDelay(int delay);

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
    ClientBuilder serverHost(@Nonnull String host);

    /**
     * Sets the server port to which the client will connect.
     * <p>
     * By default, the port is 6667.
     *
     * @param port IRC server port
     * @return this builder
     */
    @Nonnull
    ClientBuilder serverPort(int port);

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
     * @see #webircRemove()
     */
    @Nonnull
    ClientBuilder webirc(@Nonnull String password, @Nonnull String user, @Nonnull String host, @Nonnull InetAddress ip);

    /**
     * Removes WEBIRC settings from this builder.
     *
     * @return this builder
     * @see #webirc(String, String, String, InetAddress)
     */
    @Nonnull
    ClientBuilder webircRemove();

    /**
     * Clientmaker, clientmaker, make me a client!
     *
     * @return a client designed to your liking
     */
    @Nonnull
    Client build();
}
