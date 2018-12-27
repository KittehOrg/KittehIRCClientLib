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
import org.kitteh.irc.client.library.util.HostWithPort;
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
    private class BindImpl implements Bind {
        @Override
        public @NonNull Bind host(@Nullable String host) {
            DefaultBuilder.this.bindHost = host;
            return this;
        }

        @Override
        public @NonNull Bind port(int port) {
            DefaultBuilder.this.bindPort = DefaultBuilder.this.isValidPort(port);
            return this;
        }

        @Override
        public Client.@NonNull Builder then() {
            return DefaultBuilder.this;
        }
    }

    private class ServerImpl implements Server {
        @Override
        public @NonNull Server address(@NonNull HostWithPort hostWithPort) {
            DefaultBuilder.this.serverHostWithPort = Sanity.nullCheck(hostWithPort, "Host with port cannot be null");
            return this;
        }

        @Override
        public @NonNull Server host(@NonNull String host) {
            DefaultBuilder.this.serverHostWithPort = DefaultBuilder.this.serverHostWithPort.withHost(Sanity.nullCheck(host, "Host cannot be null"));
            return this;
        }

        @Override
        public @NonNull Server port(int port) {
            DefaultBuilder.this.serverHostWithPort = DefaultBuilder.this.serverHostWithPort.withPort(DefaultBuilder.this.isValidPort(port));
            return this;
        }

        @Override
        public @NonNull Server password(@Nullable String password) {
            DefaultBuilder.this.serverPassword = password;
            return this;
        }

        @Override
        public @NonNull Server secure(boolean secure) {
            DefaultBuilder.this.secure = secure;
            return this;
        }

        @Override
        public @NonNull Server secureKeyCertChain(@Nullable Path keyCertChainFile) {
            DefaultBuilder.this.secureKeyCertChain = keyCertChainFile;
            return this;
        }

        @Override
        public @NonNull Server secureKey(@Nullable Path keyFile) {
            DefaultBuilder.this.secureKey = keyFile;
            return this;
        }

        @Override
        public @NonNull Server secureKeyPassword(@Nullable String password) {
            DefaultBuilder.this.secureKeyPassword = password;
            return this;
        }

        @Override
        public @NonNull Server secureTrustManagerFactory(@Nullable TrustManagerFactory factory) {
            DefaultBuilder.this.secureTrustManagerFactory = factory;
            return this;
        }

        @Override
        public Client.@NonNull Builder then() {
            return DefaultBuilder.this;
        }
    }

    private class ListenersImpl implements Listeners {
        @Override
        public @NonNull Listeners input(@Nullable Consumer<String> listener) {
            DefaultBuilder.this.inputListener = listener;
            return this;
        }

        @Override
        public @NonNull Listeners output(@Nullable Consumer<String> listener) {
            DefaultBuilder.this.outputListener = listener;
            return this;
        }

        @Override
        public @NonNull Listeners exception(@Nullable Consumer<Exception> listener) {
            DefaultBuilder.this.exceptionListener = listener;
            return this;
        }

        @Override
        public Client.@NonNull Builder then() {
            return DefaultBuilder.this;
        }
    }

    private class ProxyImpl implements Proxy {
        @Override
        public @NonNull Proxy proxyHost(String host) {
            DefaultBuilder.this.proxyHost = host;
            return this;
        }

        @Override
        public @NonNull Proxy proxyPort(int port) {
            DefaultBuilder.this.proxyPort = DefaultBuilder.this.isValidPort(port);
            return this;
        }

        @Override
        public @NonNull Proxy proxyType(Client.ProxyType type) {
            DefaultBuilder.this.proxyType = type;
            return this;
        }

        @Override
        public Client.@NonNull Builder then() {
            return DefaultBuilder.this;
        }
    }

    private class WebIrcImpl implements WebIrc, WebIrc.Host, WebIrc.User, WebIrc.Password, WebIrc.Ip {
        @Override
        public @NonNull WebIrcImpl host(@Nullable String host) {
            if (host != null) {
                Sanity.safeMessageCheck(host, "Host");
                Sanity.noSpaces(host, "Host");
            }
            DefaultBuilder.this.webircHost = host;
            return this;
        }

        @Override
        public @NonNull WebIrcImpl user(@Nullable String user) {
            if (user != null) {
                Sanity.safeMessageCheck(user, "User");
                Sanity.noSpaces(user, "User");
            }
            DefaultBuilder.this.webircUser = user;
            return this;
        }

        @Override
        public @NonNull WebIrcImpl password(@Nullable String password) {
            if (password != null) {
                Sanity.safeMessageCheck(password, "Password");
                Sanity.noSpaces(password, "Password");
            }
            DefaultBuilder.this.webircPassword = password;
            return this;
        }

        @Override
        public Client.@NonNull Builder ip(@Nullable InetAddress ip) {
            DefaultBuilder.this.webircIP = ip;
            return DefaultBuilder.this;
        }
    }

    private class ManagementImpl implements Management {
        @Override
        public @NonNull Management actorTracker(@Nullable Function<Client.WithManagement, ? extends ActorTracker> supplier) {
            DefaultBuilder.this.actorTracker = (supplier != null) ? supplier : DEFAULT_ACTOR_TRACKER;
            return this;
        }

        @Override
        public @NonNull Management authManager(@Nullable Function<Client.WithManagement, ? extends AuthManager> supplier) {
            DefaultBuilder.this.authManager = (supplier != null) ? supplier : DEFAULT_AUTH_MANAGER;
            return this;
        }

        @Override
        public @NonNull Management capabilityManager(@Nullable Function<Client.WithManagement, ? extends CapabilityManager.WithManagement> supplier) {
            DefaultBuilder.this.capabilityManager = (supplier != null) ? supplier : DEFAULT_CAPABILITY_MANAGER;
            return this;
        }

        @Override
        public @NonNull Management defaultMessageMap(@Nullable DefaultMessageMap defaultMessageMap) {
            DefaultBuilder.this.defaultMessageMap = defaultMessageMap;
            return this;
        }

        @Override
        public @NonNull Management eventManager(@Nullable Function<Client.WithManagement, ? extends EventManager> supplier) {
            DefaultBuilder.this.eventManager = (supplier != null) ? supplier : DEFAULT_EVENT_MANAGER;
            return this;
        }

        @Override
        public @NonNull Management eventListeners(@Nullable List<EventListenerSupplier> listenerSuppliers) {
            DefaultBuilder.this.eventListeners = (listenerSuppliers != null) ? listenerSuppliers : DEFAULT_EVENT_LISTENERS;
            return this;
        }

        @Override
        public @NonNull Management iSupportManager(@Nullable Function<Client.WithManagement, ? extends ISupportManager> supplier) {
            DefaultBuilder.this.iSupportManager = (supplier != null) ? supplier : DEFAULT_ISUPPORT_MANAGER;
            return this;
        }

        @Override
        public @NonNull Management messageSendingQueueSupplier(@Nullable Function<Client.WithManagement, ? extends MessageSendingQueue> supplier) {
            DefaultBuilder.this.messageSendingQueue = Sanity.nullCheck(supplier, "Supplier cannot be null");
            return this;
        }

        @Override
        public @NonNull Management messageTagManager(@Nullable Function<Client.WithManagement, ? extends MessageTagManager> supplier) {
            DefaultBuilder.this.messageTagManager = (supplier != null) ? supplier : DEFAULT_MESSAGE_TAG_MANAGER;
            return this;
        }

        @Override
        public @NonNull Management serverInfo(@Nullable Function<Client.WithManagement, ? extends ServerInfo.WithManagement> supplier) {
            DefaultBuilder.this.serverInfo = (supplier != null) ? supplier : DEFAULT_SERVER_INFO;
            return this;
        }

        @Override
        public @NonNull Management stsStorageManager(@Nullable StsStorageManager storageManager) {
            DefaultBuilder.this.stsStorageManager = storageManager;
            return this;
        }

        @Override
        public Client.@NonNull Builder then() {
            return DefaultBuilder.this;
        }
    }

    private static final int DEFAULT_SERVER_PORT = 6697;
    private static final String DEFAULT_SERVER_HOST = "localhost";

    private static final Function<Client.WithManagement, ? extends ActorTracker> DEFAULT_ACTOR_TRACKER = DefaultActorTracker::new;
    private static final Function<Client.WithManagement, ? extends AuthManager> DEFAULT_AUTH_MANAGER = DefaultAuthManager::new;
    private static final Function<Client.WithManagement, ? extends CapabilityManager.WithManagement> DEFAULT_CAPABILITY_MANAGER = DefaultCapabilityManager::new;
    private static final Function<Client.WithManagement, ? extends EventManager> DEFAULT_EVENT_MANAGER = DefaultEventManager::new;
    private static final List<EventListenerSupplier> DEFAULT_EVENT_LISTENERS = Arrays.asList(DefaultListeners.values());
    private static final Function<Client.WithManagement, ? extends ISupportManager> DEFAULT_ISUPPORT_MANAGER = DefaultISupportManager::new;
    private static final Function<Client.WithManagement, ? extends MessageSendingQueue> DEFAULT_MESSAGE_SENDING_QUEUE = SingleDelaySender.getSupplier(SingleDelaySender.DEFAULT_MESSAGE_DELAY);
    private static final Function<Client.WithManagement, ? extends MessageTagManager> DEFAULT_MESSAGE_TAG_MANAGER = DefaultMessageTagManager::new;
    private static final Function<Client.WithManagement, ? extends ServerInfo.WithManagement> DEFAULT_SERVER_INFO = DefaultServerInfo::new;

    private String name = "Unnamed";

    private @Nullable String bindHost;
    private int bindPort;

    private HostWithPort serverHostWithPort = HostWithPort.of(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
    private @Nullable String serverPassword = null;
    private boolean secure = true;
    private @Nullable Path secureKeyCertChain = null;
    private @Nullable Path secureKey = null;
    private @Nullable String secureKeyPassword = null;
    private @Nullable TrustManagerFactory secureTrustManagerFactory = null;

    private String nick = "Kitteh";
    private String userString = "Kitteh";
    private String realName = "KICL " + Version.getVersion() + " - kitteh.org";

    // Listeners
    private @Nullable Consumer<Exception> exceptionListener = Throwable::printStackTrace;
    private @Nullable Consumer<String> inputListener = null;
    private @Nullable Consumer<String> outputListener = null;

    // Proxy
    private @Nullable String proxyHost;
    private int proxyPort;
    private Client.@Nullable ProxyType proxyType;

    // WebIRC
    private @Nullable String webircHost = null;
    private @Nullable InetAddress webircIP = null;
    private @Nullable String webircPassword = null;
    private @Nullable String webircUser = null;

    // Management
    private Function<Client.WithManagement, ? extends ActorTracker> actorTracker = DEFAULT_ACTOR_TRACKER;
    private Function<Client.WithManagement, ? extends AuthManager> authManager = DEFAULT_AUTH_MANAGER;
    private Function<Client.WithManagement, ? extends CapabilityManager.WithManagement> capabilityManager = DEFAULT_CAPABILITY_MANAGER;
    private @Nullable DefaultMessageMap defaultMessageMap = null;
    private Function<Client.WithManagement, ? extends EventManager> eventManager = DEFAULT_EVENT_MANAGER;
    private List<EventListenerSupplier> eventListeners = DEFAULT_EVENT_LISTENERS;
    private Function<Client.WithManagement, ? extends ISupportManager> iSupportManager = DEFAULT_ISUPPORT_MANAGER;
    private Function<Client.WithManagement, ? extends MessageSendingQueue> messageSendingQueue = DEFAULT_MESSAGE_SENDING_QUEUE;
    private Function<Client.WithManagement, ? extends MessageTagManager> messageTagManager = DEFAULT_MESSAGE_TAG_MANAGER;
    private Function<Client.WithManagement, ? extends ServerInfo.WithManagement> serverInfo = DEFAULT_SERVER_INFO;
    private @Nullable StsStorageManager stsStorageManager = null;

    @Override
    public @NonNull DefaultBuilder name(@NonNull String name) {
        this.name = Sanity.safeMessageCheck(name, "Name");
        return this;
    }

    @Override
    public @NonNull DefaultBuilder nick(@NonNull String nick) {
        Sanity.safeMessageCheck(nick, "Nick");
        Sanity.noSpaces(nick, "Nick");
        this.nick = nick;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder user(@NonNull String user) {
        Sanity.safeMessageCheck(user, "User");
        Sanity.noSpaces(user, "User");
        this.userString = user;
        return this;
    }

    @Override
    public @NonNull DefaultBuilder realName(@NonNull String name) {
        this.realName = Sanity.safeMessageCheck(name, "Real name");
        return this;
    }

    @Override
    public @NonNull Bind bind() {
        return new BindImpl();
    }

    @Override
    public @NonNull Server server() {
        return new ServerImpl();
    }

    @Override
    public @NonNull Listeners listeners() {
        return new ListenersImpl();
    }

    @Override
    public WebIrc.@NonNull Host webIrc() {
        return new WebIrcImpl();
    }

    @Override
    public @NonNull Management management() {
        return new ManagementImpl();
    }

    @Override
    public @NonNull Client build() {
        if (this.stsStorageManager != null) {
            Sanity.truthiness(!AcceptingTrustManagerFactory.isInsecure(this.secureTrustManagerFactory), "Cannot use STS with an insecure trust manager.");
        }

        HostWithPort proxyAddress = null;
        if ((this.proxyHost != null) && (this.proxyPort > 0)) {
            proxyAddress = HostWithPort.of(this.proxyHost, this.proxyPort);
        }
        Client.WithManagement client = new DefaultClient();
        client.initialize(this.name, this.serverHostWithPort, this.serverPassword,
                this.getInetSocketAddress(this.bindHost, this.bindPort),
                proxyAddress, this.proxyType,
                this.nick, this.userString, this.realName,
                this.actorTracker.apply(client),
                this.authManager.apply(client), this.capabilityManager.apply(client), this.eventManager.apply(client),
                this.eventListeners, this.messageTagManager.apply(client),
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
