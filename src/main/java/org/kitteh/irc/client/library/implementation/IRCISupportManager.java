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

import org.kitteh.irc.client.library.ISupportManager;
import org.kitteh.irc.client.library.element.ChannelMode;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.exception.KittehServerISupportException;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IRCISupportManager extends AbstractNameValueProcessor<ISupportParameter> implements ISupportManager {
    private static class IRCISupportParameter implements ISupportParameter {
        private final String name;
        private final Optional<String> value;

        private IRCISupportParameter(@Nonnull String name, @Nonnull Optional<String> value) {
            this.name = name;
            this.value = value;
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

    private static abstract class IRCISupportParameterInteger extends IRCISupportParameter implements ISupportParameter.IntegerParameter {
        private final int integer;

        private IRCISupportParameterInteger(@Nonnull String name, @Nonnull Optional<String> value) {
            super(name, value);
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

        private ISupportCaseMapping(@Nonnull String name, @Nonnull Optional<String> value) {
            super(name, value);
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
        private ISupportChannelLen(@Nonnull String name, @Nonnull Optional<String> value) {
            super(name, value);
        }
    }

    private static class ISupportChanLimit extends IRCISupportParameter implements ISupportParameter.ChanLimit {
        private final Map<Character, Integer> limits;

        private ISupportChanLimit(@Nonnull String name, @Nonnull Optional<String> value) {
            super(name, value);
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

        private ISupportChanModes(@Nonnull String name, @Nonnull Optional<String> value) {
            super(name, value);
            String[] modes = value.get().split(",");
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
            this.modes = Collections.unmodifiableList(modesList);
        }

        @Nonnull
        @Override
        public List<ChannelMode> getModes() {
            return this.modes;
        }
    }

    private static final Pattern TAG_ESCAPE = Pattern.compile("\\\\([\\\\s:])");

    IRCISupportManager(InternalClient client) {
        super(client);
        this.registerParameter("CASEMAPPING", ISupportCaseMapping::new);
        this.registerParameter("CHANNELLEN", ISupportChannelLen::new);
        this.registerParameter("CHANLIMIT", ISupportChanLimit::new);
    }

    private static KittehServerISupportException valueUndefined(@Nonnull String parameter) {
        return new KittehServerISupportException(parameter, "Value not defined when mandatory");
    }

    @Nonnull
    @Override
    public Optional<BiFunction<String, Optional<String>, ? extends ISupportParameter>> getCreator(@Nonnull String tagName) {
        return this.getCreatorByName(tagName);
    }

    @Nonnull
    @Override
    public Optional<BiFunction<String, Optional<String>, ? extends ISupportParameter>> registerParameter(@Nonnull String tagName, @Nonnull BiFunction<String, Optional<String>, ? extends ISupportParameter> function) {
        return this.registerCreator(tagName, new Creator<>(function));
    }

    @Nonnull
    @Override
    public Optional<BiFunction<String, Optional<String>, ? extends ISupportParameter>> unregisterParameter(@Nonnull String tagName) {
        return this.unregisterCreator(tagName);
    }

    @Nonnull
    List<ISupportParameter> getTags(@Nonnull String tagList) {
        String[] tags = tagList.split(";"); // Split up by semicolon
        List<ISupportParameter> list = new ArrayList<>();
        int index;
        Creator<ISupportParameter> creator;
        for (String tag : tags) {
            String tagName;
            Optional<String> value;
            // Split out value if present
            if (((index = tag.indexOf('=')) > -1) && (index < (tag.length() - 1))) {
                tagName = tag.substring(0, index);
                value = Optional.of(this.getTagValue(tag.substring(index + 1)));
            } else {
                tagName = tag;
                value = Optional.empty();
            }
            ISupportParameter ISupportParameter = null;
            // Attempt creating from registered creator, fall back on default
            if ((creator = this.getRegistrations().get(tagName)) != null) {
                try {
                    ISupportParameter = creator.getFunction().apply(tagName, value);
                } catch (Throwable thrown) {
                    this.getClient().getExceptionListener().queue(new KittehServerISupportException(tag, "Creator failed", thrown));
                }
            }
            if (ISupportParameter == null) {
                ISupportParameter = new IRCISupportParameter(tagName, value);
            }
            list.add(ISupportParameter);
        }
        return Collections.unmodifiableList(list);
    }

    @Nonnull
    private String getTagValue(@Nonnull String tag) {
        StringBuilder builder = new StringBuilder(tag.length());
        int currentIndex = 0;
        Matcher matcher = TAG_ESCAPE.matcher(tag);
        while (matcher.find()) {
            if (matcher.start() > currentIndex) {
                builder.append(tag.substring(currentIndex, matcher.start()));
            }
            switch (matcher.group(1)) {
                case ":":
                    builder.append(';');
                    break;
                case "s":
                    builder.append(' ');
                    break;
                case "\\":
                    builder.append('\\');
                    break;
                case "r":
                    builder.append('\r');
                    break;
                case "n":
                    builder.append('\n');
                    break;
                default:
                    // Ignore it? Technically not specified since the format MUST reflect documentation.
            }
            currentIndex = matcher.end();
        }
        if (currentIndex < tag.length()) {
            builder.append(tag.substring(currentIndex));
        }
        return builder.toString();
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).toString();
    }
}
