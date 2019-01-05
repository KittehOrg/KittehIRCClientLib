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
package org.kitteh.irc.client.library.element;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents an ISUPPORT parameter sent by the server.
 */
public interface ISupportParameter extends ClientLinked {
    /**
     * Represents a parameter which always has an integer value.
     */
    interface IntegerParameter extends ISupportParameter {
        /**
         * Gets the value of this parameter.
         *
         * @return the processed value
         */
        int getInteger();
    }

    /**
     * Represents the length limit of an away reason.
     */
    interface AwayLen extends IntegerParameter {
        /**
         * Parameter name.
         */
        String NAME = "AWAYLEN";
    }

    /**
     * Represents the {@link org.kitteh.irc.client.library.feature.CaseMapping}
     * supported by the server.
     */
    interface CaseMapping extends ISupportParameter {
        /**
         * Parameter name.
         */
        String NAME = "CASEMAPPING";

        /**
         * Gets the casemapping specified.
         *
         * @return casemapping
         */
        org.kitteh.irc.client.library.feature.@NonNull CaseMapping getCaseMapping();
    }

    /**
     * Represents the length limit of channels.
     */
    interface ChannelLen extends IntegerParameter {
        /**
         * Parameter name.
         */
        String NAME = "CHANNELLEN";
    }

    /**
     * Represents the join limit of channels by prefix.
     */
    interface ChanLimit extends ISupportParameter {
        /**
         * Parameter name.
         */
        String NAME = "CHANLIMIT";

        /**
         * Gets the channel limits.
         *
         * @return limits by prefix
         */
        @NonNull Map<Character, Integer> getLimits();
    }

    /**
     * Represents the channel modes supported.
     */
    interface ChanModes extends ISupportParameter {
        /**
         * Parameter name.
         */
        String NAME = "CHANMODES";

        /**
         * Gets the channel modes.
         *
         * @return channel modes
         */
        @NonNull List<ChannelMode> getModes();
    }

    /**
     * Represents the channel prefixes supported.
     */
    interface ChanTypes extends ISupportParameter {
        /**
         * Parameter name.
         */
        String NAME = "CHANTYPES";

        /**
         * Gets the channel types.
         *
         * @return supported prefixes
         */
        @NonNull List<Character> getTypes();
    }

    /**
     * Represents the length limit of a kick reason.
     */
    interface KickLen extends IntegerParameter {
        /**
         * Parameter name.
         */
        String NAME = "KICKLEN";
    }

    /**
     * Represents the number of modes with parameters allowed per line.
     */
    interface Modes extends IntegerParameter {
        /**
         * Parameter name.
         */
        String NAME = "MODES";
    }

    /**
     * Represents the network name.
     */
    interface Network extends ISupportParameter {
        /**
         * Parameter name.
         */
        String NAME = "NETWORK";

        /**
         * Gets the network name.
         *
         * @return network name
         */
        @NonNull String getNetworkName();
    }

    /**
     * Represents the limit to nickname length.
     */
    interface NickLen extends IntegerParameter {
        /**
         * Parameter name.
         */
        String NAME = "NICKLEN";
    }

    /**
     * Represents channel user modes (which define nick prefix).
     */
    interface Prefix extends ISupportParameter {
        /**
         * Parameter name.
         */
        String NAME = "PREFIX";

        /**
         * Gets the modes granting prefixes.
         *
         * @return channel user modes
         */
        @NonNull List<ChannelUserMode> getModes();
    }

    /**
     * Represents support for WHOX.
     */
    interface WhoX extends ISupportParameter {
        /**
         * Parameter name.
         */
        String NAME = "WHOX";
    }

    /**
     * Represents the length limit of topics.
     */
    interface TopicLen extends IntegerParameter {
        /**
         * Parameter name.
         */
        String NAME = "TOPICLEN";
    }

    /**
     * Gets the name of the parameter.
     *
     * @return parameter name
     */
    @NonNull String getName();

    /**
     * Gets the unprocessed value of the parameter if provided.
     *
     * @return parameter value if set
     */
    @NonNull Optional<String> getValue();
}
