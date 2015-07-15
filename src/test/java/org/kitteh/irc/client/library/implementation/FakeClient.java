package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.AuthManager;
import org.kitteh.irc.client.library.EventManager;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.MessageReceiver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

class FakeClient extends InternalClient {
    private final AuthManager authManager = new IRCAuthManager(this);
    private final IRCCapabilityManager capabilityManager = new IRCCapabilityManager();
    private final Config config = new Config();
    private final EventManager eventManager = new IRCEventManager(this);
    private final Listener<Exception> listenerException = new Listener<>("Test", null);
    private final Listener<String> listenerInput = new Listener<>("Test", null);
    private final Listener<String> listenerOutput = new Listener<>("Test", null);
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
    void startSending() {

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

    @Nonnull
    @Override
    Listener<String> getInputListener() {
        return this.listenerInput;
    }

    @Nonnull
    @Override
    Set<String> getIntendedChannels() {
        return new HashSet<>();
    }

    @Nonnull
    @Override
    Listener<String> getOutputListener() {
        return this.listenerOutput;
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
    public void addChannel(@Nonnull Channel... channel) {

    }

    @Nonnull
    @Override
    public AuthManager getAuthManager() {
        return this.authManager;
    }

    @Nonnull
    @Override
    public IRCCapabilityManager getCapabilityManager() {
        return this.capabilityManager;
    }

    @Nullable
    @Override
    public Channel getChannel(@Nonnull String name) {
        return null;
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

    @Nonnull
    @Override
    public IRCServerInfo getServerInfo() {
        return this.serverInfo;
    }

    @Override
    public void removeChannel(@Nonnull String channel, @Nullable String reason) {

    }

    @Override
    public void removeChannel(@Nonnull Channel channel, @Nullable String reason) {

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
    public void sendRawLine(@Nonnull String message) {

    }

    @Override
    public void sendRawLineAvoidingDuplication(@Nonnull String message) {

    }

    @Override
    public void sendRawLineImmediately(@Nonnull String message) {

    }

    @Override
    public void setExceptionListener(@Nullable Consumer<Exception> listener) {

    }

    @Override
    public void setInputListener(@Nullable Consumer<String> listener) {

    }

    @Override
    public void setMessageDelay(int delay) {

    }

    @Override
    public void setNick(@Nonnull String nick) {

    }

    @Override
    public void setOutputListener(@Nullable Consumer<String> listener) {

    }

    @Override
    public void shutdown(@Nullable String reason) {

    }
}