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
    boolean secure = true;

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
    public void initialize(@NonNull String name, @NonNull InetSocketAddress serverAddress, @Nullable String serverPassword,
                           @Nullable InetSocketAddress bindAddress,
                           @NonNull String nick, @NonNull String userString, @NonNull String realName, @NonNull ActorTracker actorTracker,
                           @NonNull AuthManager authManager, CapabilityManager.@NonNull WithManagement capabilityManager,
                           @NonNull EventManager eventManager, @NonNull List<EventListenerSupplier> listenerSuppliers,
                           @NonNull MessageTagManager messageTagManager,
                           @NonNull ISupportManager iSupportManager, @Nullable DefaultMessageMap defaultMessageMap,
                           @NonNull Function<Client.WithManagement, ? extends MessageSendingQueue> messageSendingQueue,
                           @NonNull Function<Client.WithManagement, ? extends ServerInfo.WithManagement> serverInfo,
                           @Nullable Consumer<Exception> exceptionListener, @Nullable Consumer<String> inputListener,
                           @Nullable Consumer<String> outputListener, boolean secure, @Nullable Path secureKeyCertChain,
                           @Nullable Path secureKey, @Nullable String secureKeyPassword, @Nullable TrustManagerFactory trustManagerFactory,
                           @Nullable StsStorageManager stsStorageManager, @Nullable String webircHost,
                           @Nullable InetAddress webircIP, @Nullable String webircPassword, @Nullable String webircUser) {

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

    @NonNull
    @Override
    public Listener<Exception> getExceptionListener() {
        return this.listenerException;
    }

    @Override
    public void setExceptionListener(@Nullable Consumer<Exception> listener) {

    }

    @NonNull
    @Override
    public Listener<String> getInputListener() {
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

    @NonNull
    @Override
    public Set<String> getIntendedChannels() {
        return new HashSet<>();
    }

    @NonNull
    @Override
    public DefaultISupportManager getISupportManager() {
        return null;
    }

    @NonNull
    @Override
    public Optional<StsMachine> getStsMachine() {
        return Optional.empty(); // No STS in FakeClient for testing
    }

    @NonNull
    @Override
    public Cutter getMessageCutter() {
        return this.messageCutter;
    }

    @NonNull
    @Override
    public Function<Client.WithManagement, ? extends MessageSendingQueue> getMessageSendingQueueSupplier() {
        return null;
    }

    @NonNull
    @Override
    public Listener<String> getOutputListener() {
        return this.listenerOutput;
    }

    @Override
    public void setOutputListener(@Nullable Consumer<String> listener) {

    }

    @NonNull
    @Override
    public String getRequestedNick() {
        return "";
    }

    @Nullable
    @Override
    public Path getSecureKey() {
        return null;
    }

    @Nullable
    @Override
    public Path getSecureKeyCertChain() {
        return null;
    }

    @Nullable
    @Override
    public String getSecureKeyPassword() {
        return null;
    }

    @Nullable
    @Override
    public TrustManagerFactory getSecureTrustManagerFactory() {
        return null;
    }

    @NonNull
    @Override
    public InetSocketAddress getServerAddress() {
        return null;
    }

    @Override
    public void connect() {

    }

    @Override
    public void beginMessageSendingImmediate(@NonNull Consumer<String> consumer) {

    }

    @NonNull
    @Override
    public ActorTracker getActorTracker() {
        return null;
    }

    @NonNull
    @Override
    public InetSocketAddress getBindAddress() {
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

    @NonNull
    @Override
    public AuthManager getAuthManager() {
        return this.authManager;
    }

    @NonNull
    @Override
    public DefaultCapabilityManager getCapabilityManager() {
        return this.capabilityManager;
    }

    @NonNull
    @Override
    public Optional<Channel> getChannel(@NonNull String name) {
        return Optional.empty();
    }

    @NonNull
    @Override
    public Set<Channel> getChannels() {
        return Collections.emptySet();
    }

    @NonNull
    @Override
    public Set<Channel> getChannels(@NonNull Collection<String> channels) {
        return null;
    }

    @NonNull
    @Override
    public DefaultMessageMap getDefaultMessageMap() {
        return this.defaultMessageMap;
    }

    @NonNull
    @Override
    public EventManager getEventManager() {
        return this.eventManager;
    }

    @NonNull
    @Override
    public String getIntendedNick() {
        return null;
    }

    @NonNull
    @Override
    public DefaultMessageTagManager getMessageTagManager() {
        return this.messageTagManager;
    }

    @NonNull
    @Override
    public String getName() {
        return null;
    }

    @NonNull
    @Override
    public String getNick() {
        return null;
    }

    @Override
    public void setNick(@NonNull String nick) {

    }

    @NonNull
    @Override
    public DefaultServerInfo getServerInfo() {
        return this.serverInfo;
    }

    @Override
    public void pauseMessageSending() {

    }

    @NonNull
    @Override
    public Optional<User> getUser() {
        return Optional.empty();
    }

    @NonNull
    @Override
    public Optional<ModeStatusList<UserMode>> getUserModes() {
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

    @NonNull
    @Override
    public Commands commands() {
        return null;
    }
}
