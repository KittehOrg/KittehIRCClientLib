/*
 * * Copyright (C) 2013-2016 Matt Baxter http://kitteh.org
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

import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.channel.ChannelInviteEvent;
import org.kitteh.irc.client.library.event.user.UserHostnameChangeEvent;
import org.kitteh.irc.client.library.event.user.UserUserStringChangeEvent;
import org.kitteh.irc.client.library.feature.auth.SaslECDSANIST256PChallenge;
import org.kitteh.irc.client.library.feature.auth.SaslPlain;
import org.kitteh.irc.client.library.util.RiskyBusiness;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
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
    final class Defaults {
        /**
         * Account change notification.
         *
         * @see User#getAccount()
         */
        public static final String ACCOUNT_NOTIFY = "account-notify";

        /**
         * Account message tags.
         */
        public static final String ACCOUNT_TAG = "account-tag";

        /**
         * Away notification.
         *
         * @see User#isAway()
         */
        public static final String AWAY_NOTIFY = "away-notify";

        /**
         * Self-sent message echoing.
         */
        public static final String ECHO_MESSAGE = "echo-message";

        /**
         * Account listed in join message.
         *
         * @see User#getAccount()
         */
        public static final String EXTENDED_JOIN = "extended-join";

        /**
         * Invite notification.
         *
         * @see ChannelInviteEvent
         */
        public static final String INVITE_NOTIFY = "invite-notify";

        /**
         * Multiple prefixes sent in NAMES and WHO output.
         */
        public static final String MULTI_PREFIX = "multi-prefix";

        /**
         * Server time message tag.
         *
         * @see ServerMessage#getTags()
         * @see MessageTag.Time
         */
        public static final String SERVER_TIME = "server-time";

        /**
         * The chghost extension, allows users to change user string or
         * hostname.
         *
         * @see UserHostnameChangeEvent
         * @see UserUserStringChangeEvent
         */
        public static final String CHGHOST = "chghost";

        /**
         * SASL authentication, not utilized unless a SASL authentication
         * protocol is enabled.
         *
         * @see SaslPlain
         * @see SaslECDSANIST256PChallenge
         */
        public static final transient String SASL = "sasl";

        /**
         * User hosts sent in NAMES, allowing User creation prior to WHO.
         */
        public static final String USERHOST_IN_NAMES = "userhost-in-names";

        private static final List<String> ALL;
        private static final Supplier<List<String>> SUPPLIER = ArrayList::new;

        private Defaults() {
        }

        /**
         * Gets all capabilities natively supported by KICL.
         *
         * @return all capability names
         */
        public static List<String> getAll() {
            return ALL;
        }

        static {
            ALL = Collections.unmodifiableList(Arrays.stream(Defaults.class.getDeclaredFields()).filter(field -> Modifier.isPublic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())).map(Defaults::getStringForCapabilityField).collect(Collectors.toCollection(SUPPLIER)));
        }

        /**
         * Tries to get the String value of a capability field, provided
         * it's accessible.
         *
         * @param field the field to try and get the value for
         * @return the capability extension name
         */
        private static String getStringForCapabilityField(Field field) {
            return RiskyBusiness.assertSafe(f -> (String) f.get(null), field);
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
