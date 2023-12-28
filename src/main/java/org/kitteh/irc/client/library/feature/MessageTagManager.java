/*
 * * Copyright (C) 2013-2023 Matt Baxter https://kitteh.org
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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.util.ToStringer;
import org.kitteh.irc.client.library.util.TriFunction;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages message tags.
 */
public interface MessageTagManager {
    /**
     * Default message tag.
     */
    class DefaultMessageTag implements MessageTag {
        private final String name;
        private final String value;

        /**
         * Constructs a default message tag.
         *
         * @param name tag name
         * @param value tag value or {@link Optional#empty()}
         */
        public DefaultMessageTag(@NonNull String name, @Nullable String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public @NonNull String getName() {
            return this.name;
        }

        @Override
        public @NonNull Optional<String> getValue() {
            return Optional.ofNullable(this.value);
        }

        @Override
        public @NonNull String toString() {
            return this.toStringer().toString();
        }

        /**
         * ToStringPartyTime.
         *
         * @return a usable toString that will be used in {@link #toString()}
         */
        protected @NonNull ToStringer toStringer() {
            return new ToStringer(this).add("name", this.name).add("value", this.value);
        }
    }

    /**
     * Gets the registered tag creators for a given capability.
     *
     * @param capability capability name
     * @return mapping of tags to their creators for the given capability
     */
    @NonNull Map<String, TriFunction<Client, String, String, ? extends MessageTag>> getCapabilityTagCreators(@NonNull String capability);

    /**
     * Gets capability tags for a given raw tag list.
     *
     * @param tagList raw list
     * @return tags for the given list
     */
    @NonNull List<MessageTag> getCapabilityTags(@NonNull String tagList);

    /**
     * Gets the registered tag creator for a given tag name.
     *
     * @param tagName tag name
     * @return registered creator if present
     */
    @NonNull Optional<TriFunction<Client, String, String, ? extends MessageTag>> getTagCreator(@NonNull String tagName);

    /**
     * Registers a function that creates a tag from given tag name and tag
     * value, only to run if the given capability is enabled.
     *
     * @param capability capability for which this tag exists
     * @param tagName name of the tag to register
     * @param tagCreator function that creates tags
     * @return displaced tag creator if one existed for the given tag name
     */
    @NonNull Optional<TriFunction<Client, String, String, ? extends MessageTag>> registerTagCreator(@NonNull String capability, @NonNull String tagName, @NonNull TriFunction<Client, String, String, ? extends MessageTag> tagCreator);

    /**
     * Removes the registered tag creator for a given tag name.
     *
     * @param tagName tag name
     * @return registered creator if present
     */
    @NonNull Optional<TriFunction<Client, String, String, ? extends MessageTag>> unregisterTag(@NonNull String tagName);
}
