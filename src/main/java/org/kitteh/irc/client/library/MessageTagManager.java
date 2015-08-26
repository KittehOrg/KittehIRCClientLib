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

import org.kitteh.irc.client.library.element.MessageTag;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Manages message tags.
 */
public interface MessageTagManager {
    /**
     * Gets the registered tag creators for a given capability.
     *
     * @param capability capability name
     * @return mapping of tags to their creators for the given capability
     */
    @Nonnull
    Map<String, BiFunction<String, Optional<String>, ? extends MessageTag>> getCapabilityTags(@Nonnull String capability);

    /**
     * Gets the registered tag creator for a given tag name.
     *
     * @param tagName tag name
     * @return registered creator if present
     */
    @Nonnull
    Optional<BiFunction<String, Optional<String>, ? extends MessageTag>> getTagCreator(@Nonnull String tagName);

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
    Optional<BiFunction<String, Optional<String>, ? extends MessageTag>> registerTagCreator(@Nonnull String capability, @Nonnull String tagName, @Nonnull BiFunction<String, Optional<String>, ? extends MessageTag> tagCreator);

    /**
     * Removes the registered tag creator for a given tag name.
     *
     * @param tagName tag name
     * @return registered creator if present
     */
    @Nonnull
    Optional<BiFunction<String, Optional<String>, ? extends MessageTag>> unregisterTag(@Nonnull String tagName);
}