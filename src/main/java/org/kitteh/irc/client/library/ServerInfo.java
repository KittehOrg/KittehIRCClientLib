/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library;

import org.kitteh.irc.client.library.element.ChannelMode;
import org.kitteh.irc.client.library.element.ChannelUserMode;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides information about the server to which the client is connected.
 * This information is primarily acquired via ISUPPORT.
 */
public interface ServerInfo {
    /**
     * Gets the server-stated address of the server.
     *
     * @return the server's address if known
     */
    @Nonnull
    Optional<String> getAddress();

    /**
     * Gets the casemapping for the server. By default {@link
     * CaseMapping#RFC1459}
     *
     * @return the casemapping
     */
    @Nonnull
    CaseMapping getCaseMapping();

    /**
     * Gets the maximum length of channel names.
     *
     * @return channel length limit or -1 if unknown
     */
    int getChannelLengthLimit();

    /**
     * Gets the channel join limits.
     *
     * @return a map of channel prefixes to limits
     */
    @Nonnull
    Map<Character, Integer> getChannelLimits();

    /**
     * Gets a channel mode by specified character.
     *
     * @param character character to match
     * @return the found channel mode if present
     */
    @Nonnull
    default Optional<ChannelMode> getChannelMode(char character) {
        return this.getChannelModes().stream().filter(channelMode -> channelMode.getChar() == character).findFirst();
    }

    /**
     * Gets the channel modes available. If the server has not provided
     * information on channel modes, defaults are used and returned here.
     *
     * @return a mapping of mode characters to their type
     */
    @Nonnull
    List<ChannelMode> getChannelModes();

    /**
     * Gets the list of accepted channel prefixes. If the server has not
     * provided this information all four listed in the IRC spec are
     * returned (#{@literal &}!+).
     *
     * @return available channel prefixes
     */
    @Nonnull
    List<Character> getChannelPrefixes();

    /**
     * Gets a channel user mode by specified character.
     *
     * @param character character to match
     * @return the found channel user mode if present
     */
    @Nonnull
    default Optional<ChannelUserMode> getChannelUserMode(char character) {
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
    @Nonnull
    List<ChannelUserMode> getChannelUserModes();

    /**
     * Gets the server's MOTD.
     *
     * @return motd if known
     */
    @Nonnull
    Optional<List<String>> getMOTD();

    /**
     * Gets the name of this network.
     *
     * @return network name if known
     */
    @Nonnull
    Optional<String> getNetworkName();

    /**
     * Gets the maximum length of nicknames.
     *
     * @return nickname length limit or -1 if unknown
     */
    int getNickLengthLimit();

    /**
     * Gets the version of the IRCd.
     *
     * @return server version if known
     */
    @Nonnull
    Optional<String> getVersion();

    /**
     * Gets if the server supports WHOX.
     *
     * @return true if WHOX is supported
     */
    boolean hasWhoXSupport();

    /**
     * Gets if a given string is a valid channel name according to the
     * available server information.
     *
     * @param name potentially valid name
     * @return true if valid according to the known info
     */
    boolean isValidChannel(@Nonnull String name);
}
