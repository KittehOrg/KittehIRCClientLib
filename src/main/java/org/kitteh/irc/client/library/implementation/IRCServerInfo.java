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
package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.ISupportParameter;
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
    private final List<ChannelMode> channelModes;
    private final List<Character> channelPrefixes = Arrays.asList('#', '&', '!', '+');
    private final List<ChannelUserMode> channelUserModes;
    private Optional<List<String>> motd = Optional.empty();
    private Optional<String> address = Optional.empty();
    private Optional<String> version = Optional.empty();
    private List<UserMode> userModes;

    // Pattern: ([#!&\+][^ ,\07\r\n]{1,49})
    // Screw it, let's assume IRCDs disregard length policy
    // New pattern: ([#!&\+][^ ,\07\r\n]+)
    private final Pattern channelPattern = Pattern.compile("([#!&\\+][^ ,\\07\\r\\n]+)");

    IRCServerInfo(@Nonnull InternalClient client) {
        this.client = client;
        // RFC 1459
        List<ChannelMode> channelModes = new ArrayList<>(9);
        channelModes.add(new ModeData.IRCChannelMode(client, 't', ChannelMode.Type.D_PARAMETER_NEVER)); // Topic settable by channel operator only
        channelModes.add(new ModeData.IRCChannelMode(client, 's', ChannelMode.Type.D_PARAMETER_NEVER)); // Secret
        channelModes.add(new ModeData.IRCChannelMode(client, 'p', ChannelMode.Type.D_PARAMETER_NEVER)); // Private
        channelModes.add(new ModeData.IRCChannelMode(client, 'n', ChannelMode.Type.D_PARAMETER_NEVER)); // No messages from outside
        channelModes.add(new ModeData.IRCChannelMode(client, 'm', ChannelMode.Type.D_PARAMETER_NEVER)); // Moderated
        channelModes.add(new ModeData.IRCChannelMode(client, 'i', ChannelMode.Type.D_PARAMETER_NEVER)); // Invite-only
        channelModes.add(new ModeData.IRCChannelMode(client, 'l', ChannelMode.Type.C_PARAMETER_ON_SET)); // User limit
        channelModes.add(new ModeData.IRCChannelMode(client, 'k', ChannelMode.Type.B_PARAMETER_ALWAYS)); // Channel key
        channelModes.add(new ModeData.IRCChannelMode(client, 'b', ChannelMode.Type.A_MASK)); // Ban mask
        this.channelModes = Collections.unmodifiableList(channelModes);
        List<ChannelUserMode> channelUserModes = new ArrayList<>(2);
        channelUserModes.add(new ModeData.IRCChannelUserMode(client, 'o', '@')); // OP
        channelUserModes.add(new ModeData.IRCChannelUserMode(client, 'v', '+')); // Voice
        this.channelUserModes = Collections.unmodifiableList(channelUserModes);
        List<UserMode> userModes = new ArrayList<>(4);
        userModes.add(new ModeData.IRCUserMode(client, 'i')); // Invisible
        userModes.add(new ModeData.IRCUserMode(client, 's')); // Can receive server notices
        userModes.add(new ModeData.IRCUserMode(client, 'w')); // Can receive wallops
        userModes.add(new ModeData.IRCUserMode(client, 'o')); // Operator
        this.userModes = Collections.unmodifiableList(userModes);
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
        return new ArrayList<>(optional.isPresent() ? optional.get().getModes() : this.channelModes);
    }

    @Nonnull
    @Override
    public List<Character> getChannelPrefixes() {
        Optional<ISupportParameter.ChanTypes> optional = this.getISupportParameter(ISupportParameter.ChanTypes.NAME, ISupportParameter.ChanTypes.class);
        return new ArrayList<>(optional.isPresent() ? optional.get().getTypes() : this.channelPrefixes);
    }

    @Nonnull
    @Override
    public List<ChannelUserMode> getChannelUserModes() {
        Optional<ISupportParameter.Prefix> optional = this.getISupportParameter(ISupportParameter.Prefix.NAME, ISupportParameter.Prefix.class);
        return new ArrayList<>(optional.isPresent() ? optional.get().getModes() : this.channelUserModes);
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
        Sanity.nullCheck(name, "Name cannot be null");
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
        return new ToStringer(this).add("client", this.client).toString();
    }
}
