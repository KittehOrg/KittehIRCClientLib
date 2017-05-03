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
package org.kitteh.irc.client.library.feature;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.util.ToStringer;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
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
        private final Optional<String> value;

        /**
         * Constructs a default message tag.
         *
         * @param name tag name
         * @param value tag value or {@link Optional#empty()}
         */
        public DefaultMessageTag(@Nonnull String name, @Nonnull Optional<String> value) {
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

    /**
     * Gets the registered tag creators for a given capability.
     *
     * @param capability capability name
     * @return mapping of tags to their creators for the given capability
     */
    @Nonnull
    Map<String, TriFunction<Client, String, Optional<String>, ? extends MessageTag>> getCapabilityTags(@Nonnull String capability);

    /**
     * Gets the registered tag creator for a given tag name.
     *
     * @param tagName tag name
     * @return registered creator if present
     */
    @Nonnull
    Optional<TriFunction<Client, String, Optional<String>, ? extends MessageTag>> getTagCreator(@Nonnull String tagName);

    /**
     * Registers a function that creates a tag from given tag name and tag
     * value, only to run if the given capability is enabled.
     *
     * @param capability capability for which this tag exists
     * @param tagName name of the tag to register
     * @param tagCreator function that creates tags
     * @return displaced tag creator if one existed for the given tag name
     */
    @Nonnull
    Optional<TriFunction<Client, String, Optional<String>, ? extends MessageTag>> registerTagCreator(@Nonnull String capability, @Nonnull String tagName, @Nonnull TriFunction<Client, String, Optional<String>, ? extends MessageTag> tagCreator);

    /**
     * Removes the registered tag creator for a given tag name.
     *
     * @param tagName tag name
     * @return registered creator if present
     */
    @Nonnull
    Optional<TriFunction<Client, String, Optional<String>, ? extends MessageTag>> unregisterTag(@Nonnull String tagName);
}
