package org.kitteh.irc.client.library.util;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.feature.CaseMapping;
import org.kitteh.irc.client.library.feature.ServerInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class StubServerInfo implements ServerInfo {
    private final CaseMapping caseMapping;

    StubServerInfo(CaseMapping caseMapping) {
        this.caseMapping = caseMapping;
    }

    @Override
    public void addCustomChannelMode(@NonNull ChannelMode mode) {

    }

    @Override
    public void addCustomUserMode(@NonNull UserMode mode) {

    }

    @Override
    public @NonNull Optional<String> getAddress() {
        return Optional.empty();
    }

    @Override
    public @NonNull CaseMapping getCaseMapping() {
        return this.caseMapping;
    }

    @Override
    public int getChannelLengthLimit() {
        return 0;
    }

    @Override
    public @NonNull Map<Character, Integer> getChannelLimits() {
        return Collections.emptyMap();
    }

    @Override
    public @NonNull List<ChannelMode> getChannelModes() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull List<Character> getChannelPrefixes() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull List<ChannelUserMode> getChannelUserModes() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull Optional<ISupportParameter> getISupportParameter(@NonNull String name) {
        return Optional.empty();
    }

    @Override
    public @NonNull Map<String, ISupportParameter> getISupportParameters() {
        return null;
    }

    @Override
    public @NonNull Optional<List<String>> getMotd() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<String> getNetworkName() {
        return Optional.empty();
    }

    @Override
    public int getNickLengthLimit() {
        return 0;
    }

    @Override
    public @NonNull List<UserMode> getUserModes() {
        return null;
    }

    @Override
    public @NonNull Optional<String> getVersion() {
        return Optional.empty();
    }

    @Override
    public boolean hasWhoXSupport() {
        return false;
    }

    @Override
    public boolean isValidChannel(@NonNull String name) {
        return false;
    }
}
