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
import org.kitteh.irc.client.library.MessageTagManager;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.exception.KittehServerMessageTagException;
import org.kitteh.irc.client.library.util.ToStringer;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class IRCMessageTagManager extends AbstractNameValueProcessor<MessageTag> implements MessageTagManager {
    private static class IRCMessageTag implements MessageTag {
        private final String name;
        private final Optional<String> value;

        private IRCMessageTag(@Nonnull String name, @Nonnull Optional<String> value) {
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

    private static class IRCMessageTagTime extends IRCMessageTag implements MessageTag.Time {
        private static final TriFunction<Client, String, Optional<String>, IRCMessageTagTime> FUNCTION = (client, name, value) -> new IRCMessageTagTime(name, value, Instant.parse(value.get()));

        private final Instant time;

        private IRCMessageTagTime(@Nonnull String name, @Nonnull Optional<String> value, @Nonnull Instant time) {
            super(name, value);
            this.time = time;
        }

        @Nonnull
        @Override
        public Instant getTime() {
            return this.time;
        }
    }

    protected static class TagCreator extends Creator<MessageTag> {
        private final String capability;

        private TagCreator(@Nonnull String capability, @Nonnull TriFunction<Client, String, Optional<String>, ? extends MessageTag> function) {
            super(function);
            this.capability = capability;
        }

        @Nonnull
        protected String getCapability() {
            return this.capability;
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("capability", this.capability).add("function", this.getFunction()).toString();
        }
    }

    private static final Pattern TAG_ESCAPE = Pattern.compile("\\\\([\\\\s:])");

    IRCMessageTagManager(InternalClient client) {
        super(client);
        this.registerTagCreator("server-time", "time", IRCMessageTagTime.FUNCTION);
    }

    @Nonnull
    @Override
    public Map<String, TriFunction<Client, String, Optional<String>, ? extends MessageTag>> getCapabilityTags(@Nonnull String capability) {
        return Collections.unmodifiableMap(this.getRegistrations().entrySet().stream().filter(e -> ((TagCreator) e.getValue()).getCapability().equals(capability)).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFunction())));
    }

    @Nonnull
    @Override
    public Optional<TriFunction<Client, String, Optional<String>, ? extends MessageTag>> getTagCreator(@Nonnull String tagName) {
        return this.getCreatorByName(tagName);
    }

    @Nonnull
    @Override
    public Optional<TriFunction<Client, String, Optional<String>, ? extends MessageTag>> registerTagCreator(@Nonnull String capability, @Nonnull String tagName, @Nonnull TriFunction<Client, String, Optional<String>, ? extends MessageTag> function) {
        return this.registerCreator(tagName, new TagCreator(capability, function));
    }

    @Nonnull
    @Override
    public Optional<TriFunction<Client, String, Optional<String>, ? extends MessageTag>> unregisterTag(@Nonnull String tagName) {
        return this.unregisterCreator(tagName);
    }

    @Nonnull
    List<MessageTag> getTags(@Nonnull String tagList) {
        String[] tags = tagList.split(";"); // Split up by semicolon
        List<MessageTag> list = new ArrayList<>();
        int index;
        TagCreator tagCreator;
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
            MessageTag messageTag = null;
            // Attempt creating from registered creator, fall back on default
            if ((tagCreator = (TagCreator) this.getRegistrations().get(tagName)) != null) {
                try {
                    messageTag = tagCreator.getFunction().apply(this.getClient(), tagName, value);
                } catch (Throwable thrown) {
                    this.getClient().getExceptionListener().queue(new KittehServerMessageTagException(tag, "Tag creator failed", thrown));
                }
            }
            if (messageTag == null) {
                messageTag = new IRCMessageTag(tagName, value);
            }
            list.add(messageTag);
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
