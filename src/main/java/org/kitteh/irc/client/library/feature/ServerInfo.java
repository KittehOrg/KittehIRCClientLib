/*
 * * Copyright (C) 2013-2019 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.feature;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.util.Sanity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides information about the server to which the client is connected.
 * This information is primarily acquired via ISUPPORT.
 */
public interface ServerInfo {
    /**
     * A server information provider with management features.
     */
    interface WithManagement extends ServerInfo {
        /**
         * Adds an ISUPPORT parameter to the list of supported parameters.
         *
         * @param parameter parameter to add
         */
        void addISupportParameter(@NonNull ISupportParameter parameter);

        /**
         * Gets the channel user mode for a targeted channel name, such as
         * +#channel targeting voiced users.
         *
         * @param name channel name with mode tag
         * @return user mode if a match is found
         */
        @NonNull Optional<ChannelUserMode> getTargetedChannelInfo(@NonNull String name);

        /**
         * Sets the server address.
         *
         * @param serverAddress server address
         */
        void setAddress(@NonNull String serverAddress);

        /**
         * Sets the MOTD.
         *
         * @param motd motd
         */
        void setMotd(@NonNull List<String> motd);

        /**
         * Sets the user modes.
         *
         * @param userModes user modes
         */
        void setUserModes(@NonNull List<UserMode> userModes);

        /**
         * Sets the version.
         *
         * @param version version
         */
        void setVersion(@NonNull String version);
    }

    /**
     * Adds a custom mode, for a server that doesn't correctly list modes.
     *
     * @param mode custom mode to support
     */
    void addCustomChannelMode(@NonNull ChannelMode mode);

    /**
     * Gets the server-stated address of the server.
     *
     * @return the server's address if known
     */
    @NonNull Optional<String> getAddress();

    /**
     * Gets the maximum length of away reasons.
     *
     * @return away reason length limit or -1 if unknown
     */
    default int getAwayReasonLengthLimit() {
        Optional<ISupportParameter.AwayLen> optional = this.getISupportParameter(ISupportParameter.AwayLen.NAME, ISupportParameter.AwayLen.class);
        return optional.map(ISupportParameter.IntegerParameter::getInteger).orElse(-1);
    }

    /**
     * Gets the casemapping for the server. By default {@link
     * CaseMapping#RFC1459}
     *
     * @return the casemapping
     */
    default @NonNull CaseMapping getCaseMapping() {
        Optional<ISupportParameter.CaseMapping> optional = this.getISupportParameter(ISupportParameter.CaseMapping.NAME, ISupportParameter.CaseMapping.class);
        return optional.map(ISupportParameter.CaseMapping::getCaseMapping).orElse(CaseMapping.RFC1459);
    }

    /**
     * Gets the maximum length of channel names.
     *
     * @return channel length limit or -1 if unknown
     */
    default int getChannelLengthLimit() {
        Optional<ISupportParameter.ChannelLen> optional = this.getISupportParameter(ISupportParameter.ChannelLen.NAME, ISupportParameter.ChannelLen.class);
        return optional.map(ISupportParameter.IntegerParameter::getInteger).orElse(-1);
    }

    /**
     * Gets the channel join limits.
     *
     * @return a map of channel prefixes to limits
     */
    default @NonNull Map<Character, Integer> getChannelLimits() {
        Optional<ISupportParameter.ChanLimit> optional = this.getISupportParameter(ISupportParameter.ChanLimit.NAME, ISupportParameter.ChanLimit.class);
        return optional.map(ISupportParameter.ChanLimit::getLimits).orElseGet(Collections::emptyMap);
    }

    /**
     * Gets a channel mode by specified character.
     *
     * @param character character to match
     * @return the found channel mode if present
     */
    default @NonNull Optional<ChannelMode> getChannelMode(char character) {
        return this.getChannelModes().stream().filter(channelMode -> channelMode.getChar() == character).findFirst();
    }

    /**
     * Gets the channel modes available. If the server has not provided
     * information on channel modes, defaults are used and returned here.
     *
     * @return available channel modes
     */
    @NonNull List<ChannelMode> getChannelModes();

    /**
     * Gets the list of accepted channel prefixes. If the server has not
     * provided this information all four listed in the IRC spec are
     * returned (#{@literal &}!+).
     *
     * @return available channel prefixes
     */
    @NonNull List<Character> getChannelPrefixes();

    /**
     * Gets a channel user mode by specified character.
     *
     * @param character character to match
     * @return the found channel user mode if present
     */
    default @NonNull Optional<ChannelUserMode> getChannelUserMode(char character) {
        return this.getChannelUserModes().stream().filter(channelUserMode -> channelUserMode.getChar() == character).findFirst();
    }

    /**
     * Gets the list of accepted channel user modes, such as op. Modes are
     * listed from most powerful to least powerful. If the server has not
     * provided this information a default list of op (mode o for prefix @)
     * and voice (mode v for prefix +) are returned.
     *
     * @return channel modes defining user status
     */
    @NonNull List<ChannelUserMode> getChannelUserModes();

    /**
     * Gets the named ISUPPORT parameter if present.
     *
     * @param name parameter name, case insensitive
     * @return parameter if present
     */
    @NonNull Optional<ISupportParameter> getISupportParameter(@NonNull String name);

    /**
     * Gets the named ISUPPORT parameter if present and if of the specified
     * type.
     *
     * @param name parameter name, case insensitive
     * @param clazz parameter type
     * @param <ISupport> parameter type
     * @return parameter if present
     */
    @SuppressWarnings("unchecked")
    default <ISupport extends ISupportParameter> @NonNull Optional<ISupport> getISupportParameter(@NonNull String name, @NonNull Class<ISupport> clazz) {
        Optional<ISupportParameter> optional = this.getISupportParameter(name);
        if (optional.isPresent() && Sanity.nullCheck(clazz, "Class").isInstance(optional.get())) {
            return (Optional<ISupport>) optional;
        }
        return Optional.empty();
    }

    /**
     * Gets the ISUPPORT parameters sent to the client.
     *
     * @return all parameters stored
     */
    @NonNull Map<String, ISupportParameter> getISupportParameters();

    /**
     * Gets the maximum length of kick reasons.
     *
     * @return kick reason length limit or -1 if unknown
     */
    default int getKickReasonLengthLimit() {
        final Optional<ISupportParameter.KickLen> optional = this.getISupportParameter(ISupportParameter.KickLen.NAME, ISupportParameter.KickLen.class);
        return optional.map(ISupportParameter.IntegerParameter::getInteger).orElse(-1);
    }

    /**
     * Gets the server's MOTD.
     *
     * @return motd if known
     */
    @NonNull Optional<List<String>> getMotd();

    /**
     * Gets the name of this network.
     *
     * @return network name if known
     */
    default @NonNull Optional<String> getNetworkName() {
        Optional<ISupportParameter.Network> optional = this.getISupportParameter(ISupportParameter.Network.NAME, ISupportParameter.Network.class);
        return optional.map(ISupportParameter.Network::getNetworkName);
    }

    /**
     * Gets the maximum length of nicknames.
     *
     * @return nickname length limit or -1 if unknown
     */
    default int getNickLengthLimit() {
        Optional<ISupportParameter.NickLen> optional = this.getISupportParameter(ISupportParameter.NickLen.NAME, ISupportParameter.NickLen.class);
        return optional.map(ISupportParameter.IntegerParameter::getInteger).orElse(-1);
    }

    /**
     * Gets the maximum length of topics.
     *
     * @return topic length limit or -1 if unknown
     */
    default int getTopicLengthLimit() {
        final Optional<ISupportParameter.TopicLen> optional = this.getISupportParameter(ISupportParameter.TopicLen.NAME, ISupportParameter.TopicLen.class);
        return optional.map(ISupportParameter.IntegerParameter::getInteger).orElse(-1);
    }

    /**
     * Gets the user modes available. If the server has not provided
     * information on user modes, defaults are used and returned here.
     *
     * @return a list of user modes
     */
    @NonNull List<UserMode> getUserModes();

    /**
     * Gets the version of the IRCd.
     *
     * @return server version if known
     */
    @NonNull Optional<String> getVersion();

    /**
     * Gets if the server supports WHOX.
     *
     * @return true if WHOX is supported
     */
    default boolean hasWhoXSupport() {
        return this.getISupportParameter(ISupportParameter.WhoX.NAME).isPresent();
    }

    /**
     * Gets if a given string is a valid channel name according to the
     * available server information.
     *
     * @param name potentially valid name
     * @return true if valid according to the known info
     */
    boolean isValidChannel(@NonNull String name);
}
