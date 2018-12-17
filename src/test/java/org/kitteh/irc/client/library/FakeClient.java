package org.kitteh.irc.client.library;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.defaults.feature.DefaultAuthManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultCapabilityManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultEventManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultISupportManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultMessageTagManager;
import org.kitteh.irc.client.library.defaults.feature.DefaultServerInfo;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.MessageReceiver;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.feature.ActorTracker;
import org.kitteh.irc.client.library.feature.AuthManager;
import org.kitteh.irc.client.library.feature.CapabilityManager;
import org.kitteh.irc.client.library.feature.EventListenerSupplier;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.feature.ISupportManager;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.kitteh.irc.client.library.feature.defaultmessage.DefaultMessageMap;
import org.kitteh.irc.client.library.feature.defaultmessage.SimpleDefaultMessageMap;
import org.kitteh.irc.client.library.feature.sending.MessageSendingQueue;
import org.kitteh.irc.client.library.feature.sts.StsMachine;
import org.kitteh.irc.client.library.feature.sts.StsStorageManager;
import org.kitteh.irc.client.library.util.Cutter;
import org.kitteh.irc.client.library.util.Listener;
import org.kitteh.irc.client.library.util.Pair;

import javax.net.ssl.TrustManagerFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class FakeClient implements Client.WithManagement {
    private final AuthManager authManager = new DefaultAuthManager(this);
    private final DefaultCapabilityManager capabilityManager = new DefaultCapabilityManager(this);
    private final EventManager eventManager = new DefaultEventManager(this);
    private final Listener<Exception> listenerException = new Listener<>(this, null);
    private final Listener<String> listenerInput = new Listener<>(this, null);
    private final Listener<String> listenerOutput = new Listener<>(this, null);
    private Cutter messageCutter = new Cutter.DefaultWordCutter();
    private final DefaultMessageTagManager messageTagManager = new DefaultMessageTagManager(this);
    private final DefaultServerInfo serverInfo = new DefaultServerInfo(this);
    private DefaultMessageMap defaultMessageMap = new SimpleDefaultMessageMap(null);
    private boolean secure = true;

    @Override
    public void processLine(@NonNull String line) {

    }

    @Override
    public void sendNickChange(@NonNull String newNick) {

    }

    @Override
    public void setCurrentNick(@NonNull String nick) {

    }

    @Override
    public void setServerAddress(@NonNull InetSocketAddress address) {

    }

    @Override
    public void initialize(@NonNull String name, @NonNull InetSocketAddress serverAddress, @Nullable String serverPassword, @Nullable InetSocketAddress bindAddress, @Nullable InetSocketAddress proxyAddress, @Nullable ProxyType proxyType, @NonNull String nick, @NonNull String userString, @NonNull String realName, @NonNull ActorTracker actorTracker, @NonNull AuthManager authManager, CapabilityManager.@NonNull WithManagement capabilityManager, @NonNull EventManager eventManager, @NonNull List<EventListenerSupplier> listenerSuppliers, @NonNull MessageTagManager messageTagManager, @NonNull ISupportManager iSupportManager, @Nullable DefaultMessageMap defaultMessageMap, @NonNull Function<WithManagement, ? extends MessageSendingQueue> messageSendingQueue, @NonNull Function<WithManagement, ? extends ServerInfo.WithManagement> serverInfo, @Nullable Consumer<Exception> exceptionListener, @Nullable Consumer<String> inputListener, @Nullable Consumer<String> outputListener, boolean secure, @Nullable Path secureKeyCertChain, @Nullable Path secureKey, @Nullable String secureKeyPassword, @Nullable TrustManagerFactory trustManagerFactory, @Nullable StsStorageManager stsStorageManager, @Nullable String webircHost, @Nullable InetAddress webircIP, @Nullable String webircPassword, @Nullable String webircUser) {

    }

    @Override
    public void setUserModes(@NonNull ModeStatusList<UserMode> userModes) {

    }

    @Override
    public void startSending() {

    }

    @Override
    public void updateUserModes(@NonNull ModeStatusList<UserMode> userModes) {

    }

    @Override
    public void reconnect() {

    }

    @Override
    public void reconnect(@Nullable String reason) {

    }

    @Override
    public boolean isSecureConnection() {
        return this.secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public void setDefaultMessageMap(@NonNull DefaultMessageMap defaults) {
        this.defaultMessageMap = defaults;
    }

    @Override
    public @NonNull Listener<Exception> getExceptionListener() {
        return this.listenerException;
    }

    @Override
    public void setExceptionListener(@Nullable Consumer<Exception> listener) {

    }

    @Override
    public @NonNull Listener<String> getInputListener() {
        return this.listenerInput;
    }

    @Override
    public void setInputListener(@Nullable Consumer<String> listener) {

    }

    @Override
    public void setMessageCutter(@NonNull Cutter cutter) {
        this.messageCutter = cutter;
    }

    @Override
    public void setMessageSendingQueueSupplier(@NonNull Function<WithManagement, ? extends MessageSendingQueue> supplier) {

    }

    @Override
    public @NonNull Set<String> getIntendedChannels() {
        return new HashSet<>();
    }

    @Override
    public @NonNull DefaultISupportManager getISupportManager() {
        return null;
    }

    @Override
    public @NonNull Optional<StsMachine> getStsMachine() {
        return Optional.empty(); // No STS in FakeClient for testing
    }

    @Override
    public @NonNull Cutter getMessageCutter() {
        return this.messageCutter;
    }

    @Override
    public @NonNull Function<Client.WithManagement, ? extends MessageSendingQueue> getMessageSendingQueueSupplier() {
        return null;
    }

    @Override
    public @NonNull Listener<String> getOutputListener() {
        return this.listenerOutput;
    }

    @Override
    public void setOutputListener(@Nullable Consumer<String> listener) {

    }

    @Override
    public @NonNull String getRequestedNick() {
        return "";
    }

    @Override
    public @Nullable Path getSecureKey() {
        return null;
    }

    @Override
    public @Nullable Path getSecureKeyCertChain() {
        return null;
    }

    @Override
    public @Nullable String getSecureKeyPassword() {
        return null;
    }

    @Override
    public @Nullable TrustManagerFactory getSecureTrustManagerFactory() {
        return null;
    }

    @Override
    public @NonNull InetSocketAddress getServerAddress() {
        return null;
    }

    @Override
    public @NonNull Optional<ProxyType> getProxyType() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<InetSocketAddress> getProxyAddress() {
        return Optional.empty();
    }

    @Override
    public void connect() {

    }

    @Override
    public void beginMessageSendingImmediate(@NonNull Consumer<String> consumer) {

    }

    @Override
    public @NonNull ActorTracker getActorTracker() {
        return null;
    }

    @Override
    public @NonNull InetSocketAddress getBindAddress() {
        return null;
    }

    @Override
    public void ping() {

    }

    @Override
    public void addChannel(@NonNull String... channel) {

    }

    @Override
    public void addKeyProtectedChannel(@NonNull String channel, @NonNull String key) {

    }

    @Override
    public void addKeyProtectedChannel(@NonNull Pair<String, String>... channelsAndKeys) {

    }

    @Override
    public @NonNull AuthManager getAuthManager() {
        return this.authManager;
    }

    @Override
    public @NonNull DefaultCapabilityManager getCapabilityManager() {
        return this.capabilityManager;
    }

    @Override
    public @NonNull Optional<Channel> getChannel(@NonNull String name) {
        return Optional.empty();
    }

    @Override
    public @NonNull Set<Channel> getChannels() {
        return Collections.emptySet();
    }

    @Override
    public @NonNull Set<Channel> getChannels(@NonNull Collection<String> channels) {
        return null;
    }

    @Override
    public @NonNull DefaultMessageMap getDefaultMessageMap() {
        return this.defaultMessageMap;
    }

    @Override
    public @NonNull EventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public @NonNull String getIntendedNick() {
        return null;
    }

    @Override
    public @NonNull DefaultMessageTagManager getMessageTagManager() {
        return this.messageTagManager;
    }

    @Override
    public @NonNull String getName() {
        return null;
    }

    @Override
    public @NonNull String getNick() {
        return null;
    }

    @Override
    public void setNick(@NonNull String nick) {

    }

    @Override
    public @NonNull DefaultServerInfo getServerInfo() {
        return this.serverInfo;
    }

    @Override
    public void pauseMessageSending() {

    }

    @Override
    public @NonNull Optional<User> getUser() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<ModeStatusList<UserMode>> getUserModes() {
        return Optional.empty();
    }

    @Override
    public void knockChannel(@NonNull String channelName) {

    }

    @Override
    public void removeChannel(@NonNull String channel) {

    }

    @Override
    public void removeChannel(@NonNull String channel, @Nullable String reason) {

    }

    @Override
    public void sendCtcpMessage(@NonNull String target, @NonNull String message) {

    }

    @Override
    public void sendCtcpMessage(@NonNull MessageReceiver target, @NonNull String message) {

    }

    @Override
    public void sendCtcpReply(@NonNull String target, @NonNull String message) {

    }

    @Override
    public void sendMessage(@NonNull String target, @NonNull String message) {

    }

    @Override
    public void sendMessage(@NonNull MessageReceiver target, @NonNull String message) {

    }

    @Override
    public void sendNotice(@NonNull String target, @NonNull String message) {

    }

    @Override
    public void sendNotice(@NonNull MessageReceiver target, @NonNull String message) {

    }

    @Override
    public void sendMultiLineMessage(@NonNull String target, @NonNull String message, @NonNull Cutter cutter) {

    }

    @Override
    public void sendMultiLineNotice(@NonNull String target, @NonNull String message, @NonNull Cutter cutter) {

    }

    @Override
    public void sendRawLine(@NonNull String message) {

    }

    @Override
    public void sendRawLineAvoidingDuplication(@NonNull String message) {

    }

    @Override
    public void sendRawLineImmediately(@NonNull String message) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void shutdown(@Nullable String reason) {

    }

    @Override
    public @NonNull Commands commands() {
        return null;
    }
}
