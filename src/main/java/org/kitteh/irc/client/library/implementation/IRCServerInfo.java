/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.defaults.mode.DefaultChannelMode;
import org.kitteh.irc.client.library.element.defaults.mode.DefaultChannelUserMode;
import org.kitteh.irc.client.library.element.defaults.mode.DefaultUserMode;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

class IRCServerInfo implements Resettable, ServerInfo {
    private final InternalClient client;
    private final Map<String, ISupportParameter> iSupportParameterMap = new ConcurrentHashMap<>();
    private final List<ChannelMode> defaultChannelModes;
    private final List<Character> defaultChannelPrefixes = Arrays.asList('#', '&', '!', '+');
    private final List<ChannelUserMode> defaultChannelUserModes;
    private Optional<List<String>> motd = Optional.empty();
    private Optional<String> address = Optional.empty();
    private Optional<String> version = Optional.empty();
    private List<UserMode> userModes;

    // Pattern: ([#!&\+][^ ,\07\r\n]{1,49})
    // Screw it, let's assume IRCDs disregard length policy
    // New pattern: ([#!&\+][^ ,\07\r\n]+)
    private final Pattern channelPattern = Pattern.compile("([#!&+][^ ,\\07\\r\\n]+)");

    IRCServerInfo(@Nonnull InternalClient client) {
        this.client = client;
        // RFC 1459
        List<ChannelMode> defaultChannelModes = new ArrayList<>(9);
        defaultChannelModes.add(new DefaultChannelMode(client, 't', ChannelMode.Type.D_PARAMETER_NEVER)); // Topic settable by channel operator only
        defaultChannelModes.add(new DefaultChannelMode(client, 's', ChannelMode.Type.D_PARAMETER_NEVER)); // Secret
        defaultChannelModes.add(new DefaultChannelMode(client, 'p', ChannelMode.Type.D_PARAMETER_NEVER)); // Private
        defaultChannelModes.add(new DefaultChannelMode(client, 'n', ChannelMode.Type.D_PARAMETER_NEVER)); // No messages from outside
        defaultChannelModes.add(new DefaultChannelMode(client, 'm', ChannelMode.Type.D_PARAMETER_NEVER)); // Moderated
        defaultChannelModes.add(new DefaultChannelMode(client, 'i', ChannelMode.Type.D_PARAMETER_NEVER)); // Invite-only
        defaultChannelModes.add(new DefaultChannelMode(client, 'l', ChannelMode.Type.C_PARAMETER_ON_SET)); // User limit
        defaultChannelModes.add(new DefaultChannelMode(client, 'k', ChannelMode.Type.B_PARAMETER_ALWAYS)); // Channel key
        defaultChannelModes.add(new DefaultChannelMode(client, 'b', ChannelMode.Type.A_MASK)); // Ban mask
        this.defaultChannelModes = Collections.unmodifiableList(defaultChannelModes);
        List<ChannelUserMode> defaultChannelUserModes = new ArrayList<>(2);
        defaultChannelUserModes.add(new DefaultChannelUserMode(client, 'o', '@')); // OP
        defaultChannelUserModes.add(new DefaultChannelUserMode(client, 'v', '+')); // Voice
        this.defaultChannelUserModes = Collections.unmodifiableList(defaultChannelUserModes);
        List<UserMode> defaultUserModes = new ArrayList<>(4);
        defaultUserModes.add(new DefaultUserMode(client, 'i')); // Invisible
        defaultUserModes.add(new DefaultUserMode(client, 's')); // Can receive server notices
        defaultUserModes.add(new DefaultUserMode(client, 'w')); // Can receive wallops
        defaultUserModes.add(new DefaultUserMode(client, 'o')); // Operator
        this.userModes = Collections.unmodifiableList(defaultUserModes);
    }

    @Override
    public void reset() {
        this.iSupportParameterMap.clear();
    }

    @Nonnull
    @Override
    public Optional<String> getAddress() {
        return this.address;
    }

    void setAddress(@Nonnull String serverAddress) {
        this.address = Optional.of(serverAddress);
    }

    @Nonnull
    @Override
    public List<ChannelMode> getChannelModes() {
        Optional<ISupportParameter.ChanModes> optional = this.getISupportParameter(ISupportParameter.ChanModes.NAME, ISupportParameter.ChanModes.class);
        return new ArrayList<>(optional.isPresent() ? optional.get().getModes() : this.defaultChannelModes);
    }

    @Nonnull
    @Override
    public List<Character> getChannelPrefixes() {
        Optional<ISupportParameter.ChanTypes> optional = this.getISupportParameter(ISupportParameter.ChanTypes.NAME, ISupportParameter.ChanTypes.class);
        return new ArrayList<>(optional.isPresent() ? optional.get().getTypes() : this.defaultChannelPrefixes);
    }

    @Nonnull
    @Override
    public List<ChannelUserMode> getChannelUserModes() {
        Optional<ISupportParameter.Prefix> optional = this.getISupportParameter(ISupportParameter.Prefix.NAME, ISupportParameter.Prefix.class);
        return new ArrayList<>(optional.isPresent() ? optional.get().getModes() : this.defaultChannelUserModes);
    }

    @Nonnull
    @Override
    public Optional<ISupportParameter> getISupportParameter(@Nonnull String name) {
        Sanity.nullCheck(name, "Name cannot be null");
        return Optional.ofNullable(this.iSupportParameterMap.get(name.toUpperCase()));
    }

    @Nonnull
    @Override
    public Map<String, ISupportParameter> getISupportParameters() {
        return Collections.unmodifiableMap(new HashMap<>(this.iSupportParameterMap));
    }

    void addISupportParameter(@Nonnull ISupportParameter parameter) {
        this.iSupportParameterMap.put(parameter.getName().toUpperCase(), parameter);
    }

    @Nonnull
    @Override
    public Optional<List<String>> getMOTD() {
        return this.motd;
    }

    void setMOTD(@Nonnull List<String> motd) {
        this.motd = Optional.of(Collections.unmodifiableList(motd));
    }

    @Nonnull
    @Override
    public Optional<String> getVersion() {
        return this.version;
    }

    void setVersion(@Nonnull String version) {
        this.version = Optional.of(version);
    }

    // Util stuffs
    @Override
    public boolean isValidChannel(@Nonnull String name) {
        Sanity.nullCheck(name, "Channel name cannot be null");
        int channelLengthLimit = this.getChannelLengthLimit();
        return (name.length() > 1) && ((channelLengthLimit < 0) || (name.length() <= channelLengthLimit)) && this.getChannelPrefixes().contains(name.charAt(0)) && this.channelPattern.matcher(name).matches();
    }

    @Nullable
    ChannelUserMode getTargetedChannelInfo(@Nonnull String name) {
        if (name.length() < 2) {
            return null;
        }
        final char first = name.charAt(0);
        final String shorter = name.substring(1);
        if (!this.getChannelPrefixes().contains(first) && this.isValidChannel(shorter)) {
            for (ChannelUserMode mode : this.getChannelUserModes()) {
                if (mode.getNickPrefix() == first) {
                    return mode;
                }
            }
        }
        return null;
    }

    @Override
    @Nonnull
    public List<UserMode> getUserModes() {
        return this.userModes;
    }

    void setUserModes(@Nonnull List<UserMode> userModes) {
        this.userModes = Collections.unmodifiableList(userModes);
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this)
                .add("client", this.client)
                .add("address", this.address)
                .add("version", this.version)
                .add("motd", this.motd)
                .add("defaultChannelModes", this.getChannelModes())
                .add("defaultChannelPrefixes", this.getChannelPrefixes())
                .add("defaultChannelUserModes", this.getChannelUserModes())
                .add("userModes", this.userModes)
                .add("iSupportParameters", this.iSupportParameterMap)
                .toString();
    }
}
