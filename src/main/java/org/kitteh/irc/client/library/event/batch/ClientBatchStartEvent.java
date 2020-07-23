/*
 * * Copyright (C) 2013-2020 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.event.batch;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.abstractbase.ClientBatchEventBase;
import org.kitteh.irc.client.library.util.BatchReferenceTag;

/**
 * A batch has started.
 */
public class ClientBatchStartEvent extends ClientBatchEventBase {
    private boolean ignore = false;

    /**
     * Constructs the event.
     *
     * @param client the client
     * @param sourceMessage source message
     * @param batchReferenceTag reference-tag and associated information
     */
    public ClientBatchStartEvent(@NonNull Client client, @NonNull ServerMessage sourceMessage, @NonNull BatchReferenceTag batchReferenceTag) {
        super(client, sourceMessage, batchReferenceTag);
    }

    /**
     * Gets if the reference tag will be ignored, resulting in tagged messages
     * being processed as if the batch tag were not holding them back.
     *
     * @return true if the tag will be ignored
     * @see #setReferenceTagIgnored(boolean)
     */
    public boolean isReferenceTagIgnored() {
        return this.ignore;
    }

    /**
     * Sets if the reference tag will be ignored, resulting in tagged messages
     * being processed as if the batch tag were not holding them back.
     *
     * @param ignore true to ignore the tag
     */
    public void setReferenceTagIgnored(boolean ignore) {
        this.ignore = ignore;
    }
}
