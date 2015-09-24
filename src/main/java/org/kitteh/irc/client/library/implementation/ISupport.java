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
package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.CaseMapping;
import org.kitteh.irc.client.library.element.ChannelMode;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.kitteh.irc.client.library.exception.KittehISupportProcessingFailureException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum ISupport {
    CASEMAPPING {
        @Override
        boolean process(@Nullable String value, @Nonnull InternalClient client) {
            Optional<CaseMapping> caseMapping = CaseMapping.getByName(value);
            if (caseMapping.isPresent()) {
                client.getServerInfo().setCaseMapping(caseMapping.get());
                return true;
            }
            return false;
        }
    },
    CHANNELLEN {
        @Override
        boolean process(@Nullable String value, @Nonnull InternalClient client) {
            try {
                client.getServerInfo().setChannelLengthLimit(Integer.parseInt(value));
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
    },
    CHANLIMIT {
        @Override
        boolean process(@Nullable String value, @Nonnull InternalClient client) {
            String[] pairs = value.split(",");
            Map<Character, Integer> limits = new HashMap<>();
            for (String p : pairs) {
                String[] pair = p.split(":");
                if (pair.length != 2) {
                    return false;
                }
                int limit;
                try {
                    limit = Integer.parseInt(pair[1]);
                } catch (Exception e) {
                    return false;
                }
                for (char prefix : pair[0].toCharArray()) {
                    limits.put(prefix, limit);
                }
            }
            if (limits.isEmpty()) {
                return false;
            }
            client.getServerInfo().setChannelLimits(limits);
            return true;
        }
    },
    CHANMODES {
        @Override
        boolean process(@Nullable String value, @Nonnull InternalClient client) {
            String[] modes = value.split(",");
            List<ChannelMode> modesList = new ArrayList<>();
            for (int typeId = 0; typeId < modes.length; typeId++) {
                for (char mode : modes[typeId].toCharArray()) {
                    ChannelMode.Type type = null;
                    switch (typeId) {
                        case 0:
                            type = ChannelMode.Type.A_MASK;
                            break;
                        case 1:
                            type = ChannelMode.Type.B_PARAMETER_ALWAYS;
                            break;
                        case 2:
                            type = ChannelMode.Type.C_PARAMETER_ON_SET;
                            break;
                        case 3:
                            type = ChannelMode.Type.D_PARAMETER_NEVER;
                    }
                    modesList.add(new ModeData.IRCChannelMode(client, mode, type));
                }
            }
            client.getServerInfo().setChannelModes(modesList);
            return true;
        }
    },
    CHANTYPES {
        @Override
        boolean process(@Nullable String value, @Nonnull InternalClient client) {
            if (value.isEmpty()) {
                return false;
            }
            List<Character> prefixes = new ArrayList<>();
            for (char c : value.toCharArray()) {
                prefixes.add(c);
            }
            client.getServerInfo().setChannelPrefixes(prefixes);
            return true;
        }
    },
    NETWORK {
        @Override
        boolean process(@Nullable String value, @Nonnull InternalClient client) {
            client.getServerInfo().setNetworkName(value);
            return true;
        }
    },
    NICKLEN {
        @Override
        boolean process(@Nullable String value, @Nonnull InternalClient client) {
            try {
                client.getServerInfo().setNickLengthLimit(Integer.parseInt(value));
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
    },
    PREFIX {
        private final Pattern PATTERN = Pattern.compile("\\(([a-zA-Z]+)\\)([^ ]+)");

        @Override
        boolean process(@Nullable String value, @Nonnull InternalClient client) {
            Matcher matcher = this.PATTERN.matcher(value);
            if (!matcher.find()) {
                return false;
            }
            String modes = matcher.group(1);
            String display = matcher.group(2);
            if (modes.length() == display.length()) {
                List<ChannelUserMode> prefixList = new ArrayList<>();
                for (int index = 0; index < modes.length(); index++) {
                    prefixList.add(new ModeData.IRCChannelUserMode(client, modes.charAt(index), display.charAt(index)));
                }
                client.getServerInfo().setChannelUserModes(prefixList);
            }
            return true;
        }
    },
    WHOX {
        @Override
        boolean process(@Nullable String value, @Nonnull InternalClient client) {
            client.getServerInfo().setWhoXSupport();
            return true;
        }
    };

    private static final Map<String, ISupport> MAP;
    private static final Pattern PATTERN = Pattern.compile("([A-Z0-9]+)(?:=(.*))?");

    static {
        MAP = new ConcurrentHashMap<>();
        for (ISupport iSupport : ISupport.values()) {
            MAP.put(iSupport.name(), iSupport);
        }
    }

    static void handle(@Nonnull String arg, @Nonnull InternalClient client) {
        Matcher matcher = PATTERN.matcher(arg);
        if (!matcher.find()) {
            return;
        }
        ISupport iSupport = MAP.get(matcher.group(1));
        if (iSupport != null) {
            String value = null;
            if (matcher.groupCount() > 1) {
                value = matcher.group(2);
            }
            boolean failure = !iSupport.process(value, client);
            if (failure) {
                client.getExceptionListener().queue(new KittehISupportProcessingFailureException(arg));
            }
        }
    }

    abstract boolean process(@Nullable String value, @Nonnull InternalClient client);
}
