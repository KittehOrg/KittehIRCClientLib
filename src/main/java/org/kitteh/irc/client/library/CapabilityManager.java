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

import org.kitteh.irc.client.library.auth.protocol.SaslPlain;
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.channel.ChannelInviteEvent;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides information on IRCv3 extensions available and in use.
 */
public interface CapabilityManager {
    /**
     * Contains the capabilities natively supported by KICL, which will be
     * requested automatically upon availability. Defaults defined as
     * transient are not requested unless additional functionality is
     * enabled, as documented here.
     */
    class Defaults {
        /**
         * Account change notification.
         *
         * @see User#getAccount()
         */
        public static String ACCOUNT_NOTIFY = "account-notify";

        /**
         * Account message tags.
         */
        public static String ACCOUNT_TAG = "account-tag";

        /**
         * Away notification.
         *
         * @see User#isAway()
         */
        public static String AWAY_NOTIFY = "away-notify";

        /**
         * Self-sent message echoing.
         */
        public static String ECHO_MESSAGE = "echo-message";

        /**
         * Account listed in join message.
         *
         * @see User#getAccount()
         */
        public static String EXTENDED_JOIN = "extended-join";

        /**
         * Invite notification.
         *
         * @see ChannelInviteEvent
         */
        public static String INVITE_NOTIFY = "invite-notify";

        /**
         * Multiple prefixes sent in NAMES and WHO output.
         */
        public static String MULTI_PREFIX = "multi-prefix";

        /**
         * Server time message tag.
         */
        public static String SERVER_TIME = "server-time";

        /**
         * SASL authentication, not utilized unless a SASL authentication
         * protocol is enabled.
         *
         * @see SaslPlain
         */
        public transient static String SASL = "sasl";

        /**
         * User hosts sent in NAMES, allowing User creation prior to WHO.
         */
        public static String USERHOST_IN_NAMES = "userhost-in-names";

        private static List<String> ALL;

        /**
         * Gets all capabilities natively supported by KICL.
         *
         * @return all capability names
         */
        public static List<String> getAll() {
            return ALL;
        }

        static {
            ALL = Collections.unmodifiableList(Arrays.stream(Defaults.class.getDeclaredFields()).filter(field -> Modifier.isPublic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())).map(field -> {
                try {
                    return (String) field.get(null);
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }).collect(Collectors.toCollection(ArrayList::new)));
        }
    }

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