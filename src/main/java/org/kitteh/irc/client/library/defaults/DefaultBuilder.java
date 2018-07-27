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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.feature.DefaultActorTracker;
import org.kitteh.irc.client.library.defaults.feature.DefaultAuthManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultCapabilityManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultEventManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultISupportManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultMessageTagManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultServerInfo;
import org.kitteh.irc.client.library.defaults.listener.DefaultListeners;
import org.kitteh.irc.client.library.feature.ActorTracker;
import org.kitteh.irc.client.library.feature.AuthManager;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.feature.EventListenerSupplier;
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

    private @Nullable String bindHost;
    private int bindPort;
    private String serverHost = DEFAULT_SERVER_HOST;
    private int serverPort = DEFAULT_SERVER_PORT;

    private String name = "Unnamed";
    private @Nullable DefaultMessageMap defaultMessageMap = null;
    private List<EventListenerSupplier> listenerSuppliers = Arrays.asList(DefaultListeners.values());
    private @Nullable Consumer<Exception> exceptionListener = Throwable::printStackTrace;
    private @Nullable Consumer<String> inputListener = null;
    private @Nullable Consumer<String> outputListener = null;
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
    private @Nullable String serverPassword = null;
    private boolean secure = true;
    private @Nullable Path secureKeyCertChain = null;
    private @Nullable Path secureKey = null;
    private @Nullable String secureKeyPassword = null;
    private @Nullable TrustManagerFactory secureTrustManagerFactory = null;
    private @Nullable StsStorageManager stsStorageManager = null;
    private String userString = "Kitteh";
    private @Nullable String webircHost = null;
    private @Nullable InetAddress webircIP = null;
    private @Nullable String webircPassword = null;
    private @Nullable String webircUser = null;

    @Override
    public Client.@NonNull Builder actorTracker(@NonNull Function<Client.WithManagement, ? extends ActorTracker> supplier) {
        this.actorTracker = Sanity.nullCheck(supplier, "Actor provider supplier cannot be null");
        return this;
    }

    @Override
    public @NonNull DefaultBuilder bindHost(@Nullable String host) {
        this.bindHost = host;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder bindPort(int port) {
        this.bindPort = this.isValidPort(port);
        return this;
    }

    @Override
    public @NonNull DefaultBuilder defaultMessageMap(@NonNull DefaultMessageMap defaultMessageMap) {
        this.defaultMessageMap = defaultMessageMap;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder eventListeners(@NonNull List<EventListenerSupplier> listenerSuppliers) {
        this.listenerSuppliers = listenerSuppliers;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder exceptionListener(@Nullable Consumer<Exception> listener) {
        this.exceptionListener = listener;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder inputListener(@Nullable Consumer<String> listener) {
        this.inputListener = listener;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder outputListener(@Nullable Consumer<String> listener) {
        this.outputListener = listener;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder messageSendingQueueSupplier(@NonNull Function<Client.WithManagement, ? extends MessageSendingQueue> supplier) {
        this.messageSendingQueue = Sanity.nullCheck(supplier, "Supplier cannot be null");
        return this;
    }

    @Override
    public @NonNull DefaultBuilder messageTagManager(@NonNull Function<Client.WithManagement, ? extends MessageTagManager> supplier) {
        this.messageTagManager = supplier;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder name(@NonNull String name) {
        this.name = Sanity.safeMessageCheck(name, "Name");
        return this;
    }

    @Override
    public @NonNull DefaultBuilder nick(@NonNull String nick) {
        Sanity.safeMessageCheck(nick, "Nick");
        Sanity.truthiness(!nick.contains(" "), "Nick cannot contain spaces");
        this.nick = nick;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder serverPassword(@Nullable String password) {
        this.serverPassword = password;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder realName(@NonNull String name) {
        this.realName = Sanity.safeMessageCheck(name, "Real name");
        return this;
    }

    @Override
    public @NonNull DefaultBuilder secure(boolean secure) {
        this.secure = secure;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder secureKeyCertChain(@Nullable Path keyCertChainFile) {
        this.secureKeyCertChain = keyCertChainFile;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder secureKey(@Nullable Path keyFile) {
        this.secureKey = keyFile;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder secureKeyPassword(@Nullable String password) {
        this.secureKeyPassword = password;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder secureTrustManagerFactory(@Nullable TrustManagerFactory factory) {
        this.secureTrustManagerFactory = factory;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder serverHost(@NonNull String host) {
        this.serverHost = Sanity.nullCheck(host, "Host cannot be null");
        return this;
    }

    @Override
    public @NonNull DefaultBuilder serverPort(int port) {
        this.serverPort = this.isValidPort(port);
        return this;
    }

    @Override
    public Client.@NonNull Builder authManager(@NonNull Function<Client.WithManagement, ? extends AuthManager> supplier) {
        this.authManager = Sanity.nullCheck(supplier, "Supplier cannot be null");
        return this;
    }

    @Override
    public @NonNull DefaultBuilder capabilityManager(@NonNull Function<Client.WithManagement, ? extends CapabilityManager.WithManagement> supplier) {
        this.capabilityManager = Sanity.nullCheck(supplier, "Supplier cannot be null");
        return this;
    }

    @Override
    public Client.@NonNull Builder eventManager(@NonNull Function<Client.WithManagement, ? extends EventManager> supplier) {
        this.eventManager = Sanity.nullCheck(supplier, "Supplier cannot be null");
        return this;
    }

    @Override
    public Client.@NonNull Builder iSupportManager(@NonNull Function<Client.WithManagement, ? extends ISupportManager> supplier) {
        this.iSupportManager = Sanity.nullCheck(supplier, "Supplier cannot be null");
        return this;
    }

    @Override
    public Client.@NonNull Builder serverInfo(@NonNull Function<Client.WithManagement, ? extends ServerInfo.WithManagement> supplier) {
        this.serverInfo = Sanity.nullCheck(supplier, "Supplier cannot be null");
        return this;
    }

    @Override
    public @NonNull DefaultBuilder user(@NonNull String user) {
        Sanity.safeMessageCheck(user, "User");
        Sanity.truthiness(!user.contains(" "), "User cannot contain spaces");
        this.userString = user;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder webirc(@NonNull String password, @NonNull String user, @NonNull String host, @NonNull InetAddress ip) {
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

    @Override
    public @NonNull DefaultBuilder webircRemove() {
        this.webircPassword = null;
        this.webircUser = null;
        this.webircHost = null;
        this.webircIP = null;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder stsStorageManager(@Nullable StsStorageManager storageManager) {
        this.stsStorageManager = storageManager;
        return this;
    }

    @Override
    public @NonNull Client build() {
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

    @Override
    public @NonNull Client buildAndConnect() {
        final Client client = this.build();
        client.connect();
        return client;
    }

    @Override
    public @NonNull String toString() {
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
