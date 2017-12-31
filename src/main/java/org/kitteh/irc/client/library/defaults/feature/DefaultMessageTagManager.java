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
package org.kitteh.irc.client.library.defaults.feature;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.messagetag.DefaultMessageTagTime;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.exception.KittehServerMessageTagException;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.AbstractNameValueProcessor;
import org.kitteh.irc.client.library.util.ToStringer;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link MessageTagManager}.
 */
public class DefaultMessageTagManager extends AbstractNameValueProcessor<MessageTag> implements MessageTagManager {
    protected static class TagCreator extends Creator<MessageTag> {
        private final String capability;

        private TagCreator(@Nonnull String capability, @Nonnull TriFunction<Client, String, String, ? extends MessageTag> function) {
            super(function);
            this.capability = capability;
        }

        /**
         * Gets capability for which this is registered.
         *
         * @return capability
         */
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

    /**
     * Constructs the default tag manager.
     *
     * @param client client
     */
    public DefaultMessageTagManager(Client.WithManagement client) {
        super(client);
        this.registerTagCreator("server-time", "time", DefaultMessageTagTime.FUNCTION);
    }

    @Nonnull
    @Override
    public Map<String, TriFunction<Client, String, String, ? extends MessageTag>> getCapabilityTagCreators(@Nonnull String capability) {
        return Collections.unmodifiableMap(this.getRegistrations().entrySet().stream().filter(e -> ((TagCreator) e.getValue()).getCapability().equals(capability)).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFunction())));
    }

    @Nonnull
    @Override
    public Optional<TriFunction<Client, String, String, ? extends MessageTag>> getTagCreator(@Nonnull String tagName) {
        return this.getCreatorByName(tagName);
    }

    @Nonnull
    @Override
    public Optional<TriFunction<Client, String, String, ? extends MessageTag>> registerTagCreator(@Nonnull String capability, @Nonnull String tagName, @Nonnull TriFunction<Client, String, String, ? extends MessageTag> function) {
        return this.registerCreator(tagName, new TagCreator(capability, function));
    }

    @Nonnull
    @Override
    public Optional<TriFunction<Client, String, String, ? extends MessageTag>> unregisterTag(@Nonnull String tagName) {
        return this.unregisterCreator(tagName);
    }

    @Nonnull
    @Override
    public List<MessageTag> getCapabilityTags(@Nonnull String tagList) {
        String[] tags = tagList.split(";"); // Split up by semicolon
        List<MessageTag> list = new ArrayList<>();
        int index;
        TagCreator tagCreator;
        for (String tag : tags) {
            String tagName;
            @Nullable
            String value;
            // Split out value if present
            if (((index = tag.indexOf('=')) > -1) && (index < (tag.length() - 1))) {
                tagName = tag.substring(0, index);
                value = this.getTagValue(tag.substring(index + 1));
            } else {
                tagName = (index < 0) ? tag : tag.substring(0, index);
                value = null;
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
                messageTag = new DefaultMessageTag(tagName, value);
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
}
