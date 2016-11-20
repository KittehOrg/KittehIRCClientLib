package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.MessageReceiver;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.feature.AuthManager;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.util.Cutter;
import org.kitteh.irc.client.library.util.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

class FakeClient extends InternalClient {
    private final AuthManager authManager = new ManagerAuth(this);
    private final ManagerCapability capabilityManager = new ManagerCapability(this);
    private final Config config = new Config();
    private final EventManager eventManager = new ManagerEvent(this);
    private final Listener<Exception> listenerException = new Listener<>("Test", null);
    private final Listener<String> listenerInput = new Listener<>("Test", null);
    private final Listener<String> listenerOutput = new Listener<>("Test", null);
    private Cutter messageCutter = new Cutter.DefaultWordCutter();
    private final ManagerMessageTag messageTagManager = new ManagerMessageTag(this);
    private final IRCServerInfo serverInfo = new IRCServerInfo(this);

    @Override
    void processLine(@Nonnull String line) {

    }

    @Override
    void resetServerInfo() {

    }

    @Override
    void sendNickChange(@Nonnull String newNick) {

    }

    @Override
    void setCurrentNick(@Nonnull String nick) {

    }

    @Override
    void setUserModes(@Nonnull ModeStatusList<UserMode> userModes) {

    }

    @Override
    void startSending() {

    }

    @Override
    void updateUserModes(@Nonnull ModeStatusList<UserMode> userModes) {

    }

    @Nonnull
    @Override
    Config getConfig() {
        return this.config;
    }

    @Nonnull
    @Override
    Listener<Exception> getExceptionListener() {
        return this.listenerException;
    }

    @Override
    public void setExceptionListener(@Nullable Consumer<Exception> listener) {

    }

    @Nonnull
    @Override
    Listener<String> getInputListener() {
        return this.listenerInput;
    }

    @Override
    public void setInputListener(@Nullable Consumer<String> listener) {

    }

    @Override
    public void setMessageCutter(@Nonnull Cutter cutter) {
        this.messageCutter = cutter;
    }

    @Nonnull
    @Override
    Set<String> getIntendedChannels() {
        return new HashSet<>();
    }

    @Nonnull
    @Override
    public ManagerISupport getISupportManager() {
        return null;
    }

    @Nonnull
    @Override
    public Cutter getMessageCutter() {
        return this.messageCutter;
    }

    @Nonnull
    @Override
    Listener<String> getOutputListener() {
        return this.listenerOutput;
    }

    @Override
    public void setOutputListener(@Nullable Consumer<String> listener) {

    }

    @Nonnull
    @Override
    String getRequestedNick() {
        return "";
    }

    @Override
    void connect() {

    }

    @Nonnull
    @Override
    ActorProvider getActorProvider() {
        return null;
    }

    @Override
    void ping() {

    }

    @Override
    public void addChannel(@Nonnull String... channel) {

    }

    @Override
    public void addKeyProtectedChannel(@Nonnull String channel, @Nonnull String key) {

    }

    @Override
    public void addKeyProtectedChannel(@Nonnull Pair<String, String>... channelsAndKeys) {

    }

    @Nonnull
    @Override
    public AuthManager getAuthManager() {
        return this.authManager;
    }

    @Nonnull
    @Override
    public ManagerCapability getCapabilityManager() {
        return this.capabilityManager;
    }

    @Nonnull
    @Override
    public Optional<Channel> getChannel(@Nonnull String name) {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Set<Channel> getChannels() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public EventManager getEventManager() {
        return this.eventManager;
    }

    @Nonnull
    @Override
    public String getIntendedNick() {
        return this.config.getNotNull(Config.NICK);
    }

    @Override
    public int getMessageDelay() {
        return 0;
    }

    @Override
    public void setMessageDelay(int delay) {

    }

    @Nonnull
    @Override
    public ManagerMessageTag getMessageTagManager() {
        return this.messageTagManager;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.config.getNotNull(Config.NAME);
    }

    @Nonnull
    @Override
    public String getNick() {
        return this.config.getNotNull(Config.NICK);
    }

    @Override
    public void setNick(@Nonnull String nick) {

    }

    @Nonnull
    @Override
    public IRCServerInfo getServerInfo() {
        return this.serverInfo;
    }

    @Nonnull
    @Override
    public Optional<User> getUser() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<ModeStatusList<UserMode>> getUserModes() {
        return null;
    }

    @Override
    public void knockChannel(@Nonnull String channelName) {

    }

    @Override
    public void removeChannel(@Nonnull String channel) {

    }

    @Override
    public void removeChannel(@Nonnull String channel, @Nullable String reason) {

    }

    @Override
    public void sendCTCPMessage(@Nonnull String target, @Nonnull String message) {

    }

    @Override
    public void sendCTCPMessage(@Nonnull MessageReceiver target, @Nonnull String message) {

    }

    @Override
    public void sendMessage(@Nonnull String target, @Nonnull String message) {

    }

    @Override
    public void sendMessage(@Nonnull MessageReceiver target, @Nonnull String message) {

    }

    @Override
    public void sendNotice(@Nonnull String target, @Nonnull String message) {

    }

    @Override
    public void sendNotice(@Nonnull MessageReceiver target, @Nonnull String message) {

    }

    @Override
    public void sendMultiLineMessage(@Nonnull String target, @Nonnull String message, @Nonnull Cutter cutter) {

    }

    @Override
    public void sendMultiLineNotice(@Nonnull String target, @Nonnull String message, @Nonnull Cutter cutter) {

    }

    @Override
    public void sendRawLine(@Nonnull String message) {

    }

    @Override
    public void sendRawLineAvoidingDuplication(@Nonnull String message) {

    }

    @Override
    public void sendRawLineImmediately(@Nonnull String message) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void shutdown(@Nullable String reason) {

    }

    @Override
    public Commands commands() {
        return null;    
    }
}
