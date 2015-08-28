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

import org.kitteh.irc.client.library.MessageTagManager;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.exception.KittehServerMessageTagException;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class IRCMessageTagManager implements MessageTagManager {
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
        private static final BiFunction<String, Optional<String>, IRCMessageTagTime> FUNCTION = (name, value) -> new IRCMessageTagTime(name, value, Instant.parse(value.get()));

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

    private static class TagCreator {
        private final String capability;
        private final BiFunction<String, Optional<String>, ? extends MessageTag> function;

        private TagCreator(@Nonnull String capability, @Nonnull BiFunction<String, Optional<String>, ? extends MessageTag> function) {
            this.capability = capability;
            this.function = function;
        }

        @Nonnull
        private String getCapability() {
            return this.capability;
        }

        @Nonnull
        private BiFunction<String, Optional<String>, ? extends MessageTag> getFunction() {
            return this.function;
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).add("capability", this.capability).add("function", this.function).toString();
        }
    }

    private static final Pattern TAG_ESCAPE = Pattern.compile("\\\\([\\\\s:])");

    private final InternalClient client;
    private final Map<String, TagCreator> tags = new ConcurrentHashMap<>();

    IRCMessageTagManager(InternalClient client) {
        this.client = client;
        this.registerTagCreator("server-time", "time", IRCMessageTagTime.FUNCTION);
    }

    @Nonnull
    @Override
    public Map<String, BiFunction<String, Optional<String>, ? extends MessageTag>> getCapabilityTags(@Nonnull String capability) {
        return Collections.unmodifiableMap(this.tags.entrySet().stream().filter(e -> e.getValue().getCapability().equals(capability)).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFunction())));
    }

    @Nonnull
    @Override
    public Optional<BiFunction<String, Optional<String>, ? extends MessageTag>> getTagCreator(@Nonnull String tagName) {
        return this.optional(this.tags.get(tagName));
    }

    @Nonnull
    @Override
    public Optional<BiFunction<String, Optional<String>, ? extends MessageTag>> registerTagCreator(@Nonnull String capability, @Nonnull String tagName, @Nonnull BiFunction<String, Optional<String>, ? extends MessageTag> tagCreator) {
        return this.optional(this.tags.put(tagName, new TagCreator(capability, tagCreator)));
    }

    @Nonnull
    @Override
    public Optional<BiFunction<String, Optional<String>, ? extends MessageTag>> unregisterTag(@Nonnull String tagName) {
        return this.optional(this.tags.remove(tagName));
    }

    @Nonnull
    private Optional<BiFunction<String, Optional<String>, ? extends MessageTag>> optional(@Nullable TagCreator creator) {
        return (creator == null) ? Optional.empty() : Optional.of(creator.getFunction());
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
            if ((tagCreator = this.tags.get(tagName)) != null) {
                try {
                    messageTag = tagCreator.getFunction().apply(tagName, value);
                } catch (Throwable thrown) {
                    this.client.getExceptionListener().queue(new KittehServerMessageTagException(tag, "Tag creator failed", thrown));
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