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

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.ISupportManager;
import org.kitteh.irc.client.library.element.ChannelMode;
import org.kitteh.irc.client.library.element.ChannelUserMode;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.exception.KittehServerISupportException;
import org.kitteh.irc.client.library.util.ToStringer;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IRCISupportManager extends AbstractNameValueProcessor<ISupportParameter> implements ISupportManager {
    private static class IRCISupportParameter implements ISupportParameter {
        private final Client client;
        private final String name;
        private final Optional<String> value;

        private IRCISupportParameter(@Nonnull Client client, @Nonnull String name, @Nonnull Optional<String> value) {
            this.client = client;
            this.name = name;
            this.value = value;
        }

        @Nonnull
        @Override
        public Client getClient() {
            return this.client;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Nonnull
        @Override
        public Optional<String> getValue() {
            return this.value;
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("name", this.name).add("value", this.value).toString();
        }
    }

    private abstract static class IRCISupportParameterInteger extends IRCISupportParameter implements ISupportParameter.IntegerParameter {
        private final int integer;

        private IRCISupportParameterInteger(@Nonnull Client client, @Nonnull String name, @Nonnull Optional<String> value) {
            super(client, name, value);
            try {
                this.integer = Integer.parseInt(value.get());
            } catch (Exception e) {
                throw new KittehServerISupportException(name, "Could not parse value", e);
            }
        }

        @Override
        public int getInteger() {
            return this.integer;
        }
    }

    private static class ISupportCaseMapping extends IRCISupportParameter implements ISupportParameter.CaseMapping {
        private final org.kitteh.irc.client.library.CaseMapping caseMapping;

        private ISupportCaseMapping(@Nonnull Client client, @Nonnull String name, @Nonnull Optional<String> value) {
            super(client, name, value);
            Optional<org.kitteh.irc.client.library.CaseMapping> caseMapping = org.kitteh.irc.client.library.CaseMapping.getByName(value.get());
            if (caseMapping.isPresent()) {
                this.caseMapping = caseMapping.get();
            }
            throw valueUndefined(name);
        }

        @Nonnull
        @Override
        public org.kitteh.irc.client.library.CaseMapping getCaseMapping() {
            return this.caseMapping;
        }
    }

    private static class ISupportChannelLen extends IRCISupportParameterInteger implements ISupportParameter.ChannelLen {
        private ISupportChannelLen(@Nonnull Client client, @Nonnull String name, @Nonnull Optional<String> value) {
            super(client, name, value);
        }
    }

    private static class ISupportChanLimit extends IRCISupportParameter implements ISupportParameter.ChanLimit {
        private final Map<Character, Integer> limits;

        private ISupportChanLimit(@Nonnull Client client, @Nonnull String name, @Nonnull Optional<String> value) {
            super(client, name, value);
            String[] pairs = value.get().split(",");
            Map<Character, Integer> limits = new HashMap<>();
            for (String p : pairs) {
                String[] pair = p.split(":");
                if (pair.length != 2) {
                    throw new KittehServerISupportException(name, "Invalid format");
                }
                int limit;
                try {
                    limit = Integer.parseInt(pair[1]);
                } catch (Exception e) {
                    throw new KittehServerISupportException(name, "Non-integer limit", e);
                }
                for (char prefix : pair[0].toCharArray()) {
                    limits.put(prefix, limit);
                }
            }
            if (limits.isEmpty()) {
                throw new KittehServerISupportException(name, "Found no limits!");
            }
            this.limits = Collections.unmodifiableMap(limits);
        }

        @Nonnull
        @Override
        public Map<Character, Integer> getLimits() {
            return this.limits;
        }
    }

    private static class ISupportChanModes extends IRCISupportParameter implements ISupportParameter.ChanModes {
        private final List<ChannelMode> modes;

        private ISupportChanModes(@Nonnull Client client, @Nonnull String name, @Nonnull Optional<String> value) {
            super(client, name, value);
            String[] modes = value.get().split(",");
            List<ChannelMode> modesList = new ArrayList<>();
            for (int typeId = 0; (typeId < modes.length) && (typeId < 4); typeId++) {
                for (char mode : modes[typeId].toCharArray()) {
                    ChannelMode.Type type;
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
                        default:
                            type = ChannelMode.Type.D_PARAMETER_NEVER;
                    }
                    modesList.add(new ModeData.IRCChannelMode(client, mode, type));
                }
            }
            this.modes = Collections.unmodifiableList(modesList);
        }

        @Nonnull
        @Override
        public List<ChannelMode> getModes() {
            return this.modes;
        }
    }

    private static class ISupportChanTypes extends IRCISupportParameter implements ISupportParameter.ChanTypes {
        private final List<Character> prefixes;

        private ISupportChanTypes(@Nonnull Client client, @Nonnull String name, @Nonnull Optional<String> value) {
            super(client, name, value);
            if (!value.isPresent()) {
                throw new KittehServerISupportException(name, "No channel types defined");
            }
            List<Character> prefixes = new ArrayList<>();
            for (char c : value.get().toCharArray()) {
                prefixes.add(c);
            }
            this.prefixes = Collections.unmodifiableList(prefixes);
        }

        @Nonnull
        @Override
        public List<Character> getTypes() {
            return this.prefixes;
        }
    }

    private static class ISupportNetwork extends IRCISupportParameter implements ISupportParameter.Network {
        private ISupportNetwork(@Nonnull Client client, @Nonnull String name, @Nonnull Optional<String> value) {
            super(client, name, value);
            if (!value.isPresent()) {
                throw new KittehServerISupportException(name, "No network name");
            }
        }

        @Nonnull
        @Override
        public String getNetworkName() {
            return this.getValue().get();
        }
    }

    private static class ISupportNickLen extends IRCISupportParameterInteger implements ISupportParameter.NickLen {
        private ISupportNickLen(@Nonnull Client client, @Nonnull String name, @Nonnull Optional<String> value) {
            super(client, name, value);
        }
    }

    private static class ISupportPrefix extends IRCISupportParameter implements ISupportParameter.Prefix {
        private final Pattern PATTERN = Pattern.compile("\\(([a-zA-Z]+)\\)([^ ]+)");

        private final List<ChannelUserMode> modes;

        private ISupportPrefix(@Nonnull Client client, @Nonnull String name, @Nonnull Optional<String> value) {
            super(client, name, value);
            if (!value.isPresent()) {
                throw new KittehServerISupportException(name, "No prefix data");
            }
            Matcher matcher = this.PATTERN.matcher(value.get());
            if (!matcher.find()) {
                throw new KittehServerISupportException(name, "Data does not match expected pattern");
            }
            String modes = matcher.group(1);
            String display = matcher.group(2);
            if (modes.length() != display.length()) {
                throw new KittehServerISupportException(name, "Prefix and mode size mismatch");
            }
            List<ChannelUserMode> prefixList = new ArrayList<>();
            for (int index = 0; index < modes.length(); index++) {
                prefixList.add(new ModeData.IRCChannelUserMode(client, modes.charAt(index), display.charAt(index)));
            }
            this.modes = Collections.unmodifiableList(prefixList);
        }

        @Nonnull
        @Override
        public List<ChannelUserMode> getModes() {
            return this.modes;
        }
    }

    private static class ISupportWHOX extends IRCISupportParameter implements ISupportParameter.WHOX {
        private ISupportWHOX(@Nonnull Client client, @Nonnull String name, @Nonnull Optional<String> value) {
            super(client, name, value);
        }
    }

    IRCISupportManager(InternalClient client) {
        super(client);
        this.registerParameter(ISupportParameter.CaseMapping.NAME, ISupportCaseMapping::new);
        this.registerParameter(ISupportParameter.ChannelLen.NAME, ISupportChannelLen::new);
        this.registerParameter(ISupportParameter.ChanLimit.NAME, ISupportChanLimit::new);
        this.registerParameter(ISupportParameter.ChanModes.NAME, ISupportChanModes::new);
        this.registerParameter(ISupportParameter.ChanTypes.NAME, ISupportChanTypes::new);
        this.registerParameter(ISupportParameter.Network.NAME, ISupportNetwork::new);
        this.registerParameter(ISupportParameter.NickLen.NAME, ISupportNickLen::new);
        this.registerParameter(ISupportParameter.Prefix.NAME, ISupportPrefix::new);
        this.registerParameter(ISupportParameter.WHOX.NAME, ISupportWHOX::new);
    }

    private static KittehServerISupportException valueUndefined(@Nonnull String parameter) {
        return new KittehServerISupportException(parameter, "Value not defined when mandatory");
    }

    @Nonnull
    @Override
    public Optional<TriFunction<Client, String, Optional<String>, ? extends ISupportParameter>> getCreator(@Nonnull String tagName) {
        return this.getCreatorByName(tagName);
    }

    @Nonnull
    @Override
    public Optional<TriFunction<Client, String, Optional<String>, ? extends ISupportParameter>> registerParameter(@Nonnull String tagName, @Nonnull TriFunction<Client, String, Optional<String>, ? extends ISupportParameter> function) {
        return this.registerCreator(tagName, new Creator<>(function));
    }

    @Nonnull
    @Override
    public Optional<TriFunction<Client, String, Optional<String>, ? extends ISupportParameter>> unregisterParameter(@Nonnull String tagName) {
        return this.unregisterCreator(tagName);
    }

    @Nonnull
    ISupportParameter getTag(@Nonnull String tag) {
        int index;
        Creator<ISupportParameter> creator;
        String tagName;
        Optional<String> value;
        // Split out value if present
        if (((index = tag.indexOf('=')) > -1) && (index < (tag.length() - 1))) {
            tagName = tag.substring(0, index);
            value = Optional.of(tag.substring(index + 1));
        } else {
            tagName = tag;
            value = Optional.empty();
        }
        ISupportParameter iSupportParameter = null;
        // Attempt creating from registered creator, fall back on default
        if ((creator = this.getRegistrations().get(tagName)) != null) {
            try {
                iSupportParameter = creator.getFunction().apply(this.getClient(), tagName, value);
            } catch (Throwable thrown) {
                this.getClient().getExceptionListener().queue(new KittehServerISupportException(tag, "Creator failed", thrown));
            }
        }
        if (iSupportParameter == null) {
            iSupportParameter = new IRCISupportParameter(this.getClient(), tagName, value);
        }
        return iSupportParameter;
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).toString();
    }
}
