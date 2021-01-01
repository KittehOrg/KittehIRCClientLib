/*
 * * Copyright (C) 2013-2021 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.util;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.event.helper.ClientReceiveServerMessageEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a BATCH capability reference tag.
 */
public class BatchReferenceTag {
    private final String referenceTag;
    private final String batchType;
    private final List<String> batchTypeParameters;
    private final List<ClientReceiveServerMessageEvent> events = new CopyOnWriteArrayList<>();

    /**
     * Constructs a reference tag.
     *
     * @param referenceTag the tag
     * @param type the tag's type
     * @param parameters any additional parameters
     */
    public BatchReferenceTag(@NonNull String referenceTag, @NonNull String type, @NonNull List<String> parameters) {
        this.referenceTag = referenceTag;
        this.batchType = type;
        this.batchTypeParameters = Collections.unmodifiableList(parameters);
    }

    /**
     * Constructs a reference tag without parameters.
     *
     * @param referenceTag the tag
     * @param type the tag's type
     */
    public BatchReferenceTag(@NonNull String referenceTag, @NonNull String type) {
        this(referenceTag, type, Collections.emptyList());
    }

    /**
     * Gets the reference tag.
     *
     * @return the tag name
     */
    public @NonNull String getReferenceTag() {
        return this.referenceTag;
    }

    /**
     * Gets the tag's type
     *
     * @return type
     */
    public @NonNull String getType() {
        return this.batchType;
    }

    /**
     * Gets the tag's parameters.
     *
     * @return parameters or an empty list if there aren't any
     */
    public @NonNull List<String> getParameters() {
        return this.batchTypeParameters;
    }

    /**
     * Gets the events, in order, assigned to this tag.
     *
     * @return events
     */
    public @NonNull List<ClientReceiveServerMessageEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<>(this.events));
    }

    /**
     * Adds an event to the tag.
     *
     * @param event event to add
     */
    public void addEvent(@NonNull ClientReceiveServerMessageEvent event) {
        this.events.add(event);
    }
}
