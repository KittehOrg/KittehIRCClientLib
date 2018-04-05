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
package org.kitteh.irc.client.library.defaults;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.feature.DefaultActorTracker;
import org.kitteh.irc.client.library.defaults.feature.DefaultAuthManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultCapabilityManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultEventManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultISupportManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultMessageTagManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultServerInfo;
import org.kitteh.irc.client.library.defaults.listener.DefaultListeners;
import org.kitteh.irc.client.library.feature.EventListenerSupplier;
import org.kitteh.irc.client.library.feature.ActorTracker;
import org.kitteh.irc.client.library.feature.AuthManager;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.feature.ISupportManager;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageMap;
import org.kitteh.irc.client.library.feature.sending.MessageSendingQueue;
import org.kitteh.irc.client.library.feature.sending.SingleDelaySender;
import org.kitteh.irc.client.library.feature.sts.StsStorageManager;
import org.kitteh.irc.client.library.util.AcceptingTrustManagerFactory;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;
import org.kitteh.irc.client.library.util.Version;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.TrustManagerFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Default {@link Client.Builder}.
 */
public class DefaultBuilder implements Client.Builder {
    private static final int DEFAULT_SERVER_PORT = 6697;
    private static final String DEFAULT_SERVER_HOST = "localhost";

    @Nullable
    private String bindHost;
    private int bindPort;
    private String serverHost = DEFAULT_SERVER_HOST;
    private int serverPort = DEFAULT_SERVER_PORT;

    private String name = "Unnamed";
    @Nullable
    private DefaultMessageMap defaultMessageMap = null;
    private List<EventListenerSupplier> listenerSuppliers = Arrays.asList(DefaultListeners.values());
    @Nullable
    private Consumer<Exception> exceptionListener = Throwable::printStackTrace;
    @Nullable
    private Consumer<String> inputListener = null;
    @Nullable
    private Consumer<String> outputListener = null;
    private Function<Client.WithManagement, ? extends ActorTracker> actorTracker = DefaultActorTracker::new;
    private Function<Client.WithManagement, ? extends AuthManager> authManager = DefaultAuthManager::new;
    private Function<Client.WithManagement, ? extends CapabilityManager.WithManagement> capabilityManager = DefaultCapabilityManager::new;
    private Function<Client.WithManagement, ? extends EventManager> eventManager = DefaultEventManager::new;
    private Function<Client.WithManagement, ? extends ISupportManager> iSupportManager = DefaultISupportManager::new;
    private Function<Client.WithManagement, ? extends MessageSendingQueue> messageSendingQueue = SingleDelaySender.getSupplier(SingleDelaySender.DEFAULT_MESSAGE_DELAY);
    private Function<Client.WithManagement, ? extends MessageTagManager> messageTagManager = DefaultMessageTagManager::new;
    private String nick = "Kitteh";
    private String realName = "KICL " + Version.getVersion() + " - kitteh.org";
    private Function<Client.WithManagement, ? extends ServerInfo.WithManagement> serverInfo = DefaultServerInfo::new;
    @Nullable
    private String serverPassword = null;
    private boolean secure = true;
    @Nullable
    private Path secureKeyCertChain = null;
    @Nullable
    private Path secureKey = null;
    @Nullable
    private String secureKeyPassword = null;
    @Nullable
    private TrustManagerFactory secureTrustManagerFactory = null;
    @Nullable
    private StsStorageManager stsStorageManager = null;
    private String userString = "Kitteh";
    @Nullable
    private String webircHost = null;
    @Nullable
    private InetAddress webircIP = null;
    @Nullable
    private String webircPassword = null;
    @Nullable
    private String webircUser = null;

    @Nonnull
    @Override
    public Client.Builder actorTracker(@Nonnull Function<Client.WithManagement, ? extends ActorTracker> supplier) {
        this.actorTracker = Sanity.nullCheck(supplier, "Actor provider supplier cannot be null");
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder bindHost(@Nullable String host) {
        this.bindHost = host;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder bindPort(int port) {
        this.bindPort = this.isValidPort(port);
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder defaultMessageMap(@Nonnull DefaultMessageMap defaultMessageMap) {
        this.defaultMessageMap = defaultMessageMap;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder eventListeners(@Nonnull List<EventListenerSupplier> listenerSuppliers) {
        this.listenerSuppliers = listenerSuppliers;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder exceptionListener(@Nullable Consumer<Exception> listener) {
        this.exceptionListener = listener;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder inputListener(@Nullable Consumer<String> listener) {
        this.inputListener = listener;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder outputListener(@Nullable Consumer<String> listener) {
        this.outputListener = listener;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder messageSendingQueueSupplier(@Nonnull Function<Client.WithManagement, ? extends MessageSendingQueue> supplier) {
        this.messageSendingQueue = Sanity.nullCheck(supplier, "Supplier cannot be null");
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder messageTagManager(@Nonnull Function<Client.WithManagement, ? extends MessageTagManager> supplier) {
        this.messageTagManager = supplier;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder name(@Nonnull String name) {
        this.name = Sanity.safeMessageCheck(name, "Name");
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder nick(@Nonnull String nick) {
        Sanity.safeMessageCheck(nick, "Nick");
        Sanity.truthiness(!nick.contains(" "), "Nick cannot contain spaces");
        this.nick = nick;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder serverPassword(@Nullable String password) {
        this.serverPassword = password;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder realName(@Nonnull String name) {
        this.realName = Sanity.safeMessageCheck(name, "Real name");
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder secure(boolean secure) {
        this.secure = secure;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder secureKeyCertChain(@Nullable Path keyCertChainFile) {
        this.secureKeyCertChain = keyCertChainFile;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder secureKey(@Nullable Path keyFile) {
        this.secureKey = keyFile;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder secureKeyPassword(@Nullable String password) {
        this.secureKeyPassword = password;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder secureTrustManagerFactory(@Nullable TrustManagerFactory factory) {
        this.secureTrustManagerFactory = factory;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder serverHost(@Nonnull String host) {
        this.serverHost = Sanity.nullCheck(host, "Host cannot be null");
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder serverPort(int port) {
        this.serverPort = this.isValidPort(port);
        return this;
    }

    @Nonnull
    @Override
    public Client.Builder authManager(@Nonnull Function<Client.WithManagement, ? extends AuthManager> supplier) {
        this.authManager = Sanity.nullCheck(supplier, "Supplier cannot be null");
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder capabilityManager(@Nonnull Function<Client.WithManagement, ? extends CapabilityManager.WithManagement> supplier) {
        this.capabilityManager = Sanity.nullCheck(supplier, "Supplier cannot be null");
        return this;
    }

    @Nonnull
    @Override
    public Client.Builder eventManager(@Nonnull Function<Client.WithManagement, ? extends EventManager> supplier) {
        this.eventManager = Sanity.nullCheck(supplier, "Supplier cannot be null");
        return this;
    }

    @Nonnull
    @Override
    public Client.Builder iSupportManager(@Nonnull Function<Client.WithManagement, ? extends ISupportManager> supplier) {
        this.iSupportManager = Sanity.nullCheck(supplier, "Supplier cannot be null");
        return this;
    }

    @Nonnull
    @Override
    public Client.Builder serverInfo(@Nonnull Function<Client.WithManagement, ? extends ServerInfo.WithManagement> supplier) {
        this.serverInfo = Sanity.nullCheck(supplier, "Supplier cannot be null");
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder user(@Nonnull String user) {
        Sanity.safeMessageCheck(user, "User");
        Sanity.truthiness(!user.contains(" "), "User cannot contain spaces");
        this.userString = user;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder webirc(@Nonnull String password, @Nonnull String user, @Nonnull String host, @Nonnull InetAddress ip) {
        Sanity.safeMessageCheck(password, "Password");
        Sanity.safeMessageCheck(user, "User");
        Sanity.safeMessageCheck(host, "Host");
        Sanity.nullCheck(ip, "IP cannot be null");

        Sanity.truthiness(!password.contains(" "), "Password cannot contain spaces");
        Sanity.truthiness(!user.contains(" "), "User cannot contain spaces");
        Sanity.truthiness(!host.contains(" "), "Host cannot contain spaces");

        this.webircPassword = password;
        this.webircUser = user;
        this.webircHost = host;
        this.webircIP = ip;

        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder webircRemove() {
        this.webircPassword = null;
        this.webircUser = null;
        this.webircHost = null;
        this.webircIP = null;
        return this;
    }

    @Nonnull
    @Override
    public DefaultBuilder stsStorageManager(@Nullable StsStorageManager storageManager) {
        this.stsStorageManager = storageManager;
        return this;
    }

    @Nonnull
    @Override
    public Client build() {
        if (this.stsStorageManager != null) {
            Sanity.truthiness(!AcceptingTrustManagerFactory.isInsecure(this.secureTrustManagerFactory), "Cannot use STS with an insecure trust manager.");
        }

        Client.WithManagement client = new DefaultClient();
        client.initialize(this.name, this.getInetSocketAddress(this.serverHost, this.serverPort), this.serverPassword,
                this.getInetSocketAddress(this.bindHost, this.bindPort), this.nick, this.userString, this.realName,
                this.actorTracker.apply(client),
                this.authManager.apply(client), this.capabilityManager.apply(client), this.eventManager.apply(client),
                this.listenerSuppliers, this.messageTagManager.apply(client),
                this.iSupportManager.apply(client), this.defaultMessageMap, this.messageSendingQueue,
                this.serverInfo, this.exceptionListener, this.inputListener, this.outputListener, this.secure,
                this.secureKeyCertChain, this.secureKey, this.secureKeyPassword, this.secureTrustManagerFactory, this.stsStorageManager,
                this.webircHost, this.webircIP, this.webircPassword, this.webircUser
        );

        return client;
    }

    @Nonnull
    @Override
    public Client buildAndConnect() {
        final Client client = this.build();
        client.connect();
        return client;
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).toString();
    }

    private InetSocketAddress getInetSocketAddress(@Nullable String host, int port) {
        if (host != null) {
            return new InetSocketAddress(host, port);
        } else {
            return new InetSocketAddress(port);
        }
    }

    /**
     * Gets a valid port number from a potentially invalid port number.
     * <p>
     * Returns the valid port, or 0 if invalid.
     *
     * @param port port provided
     * @return valid port
     */
    private int isValidPort(int port) {
        return ((port > 65535) || (port < 0)) ? 0 : port;
    }
}
