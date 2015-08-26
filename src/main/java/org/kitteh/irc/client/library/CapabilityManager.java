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

import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * Provides information on IRCv3 extensions available and in use.
 */
public interface CapabilityManager {
    /**
     * Gets capabilities currently enabled.
     *
     * @return the capabilities currently enabled
     * @see CapabilityRequestCommand
     */
    @Nonnull
    List<CapabilityState> getCapabilities();

    /**
     * Gets an enabled capability by name.
     *
     * @param name capability name
     * @return the named capability if enabled
     */
    @Nonnull
    default Optional<CapabilityState> getCapability(@Nonnull String name) {
        Sanity.nullCheck(name, "Name cannot be null");
        return this.getCapabilities().stream().filter(capabilityState -> capabilityState.getName().equals(name)).findFirst();
    }

    /**
     * Gets capabilities supported by the server.
     *
     * @return the capabilities supported
     * @see CapabilityRequestCommand
     */
    @Nonnull
    List<CapabilityState> getSupportedCapabilities();

    /**
     * Gets a supported capability by name.
     *
     * @param name capability name
     * @return the named capability if supported
     */
    @Nonnull
    default Optional<CapabilityState> getSupportedCapability(@Nonnull String name) {
        Sanity.nullCheck(name, "Name cannot be null");
        return this.getSupportedCapabilities().stream().filter(capabilityState -> capabilityState.getName().equals(name)).findFirst();
    }
}