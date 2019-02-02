/*
 * * Copyright (C) 2013-2019 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.command.CapabilityRequestCommand;
import org.kitteh.irc.client.library.element.CapabilityState;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.channel.ChannelInviteEvent;
import org.kitteh.irc.client.library.event.user.UserHostnameChangeEvent;
import org.kitteh.irc.client.library.event.user.UserUserStringChangeEvent;
import org.kitteh.irc.client.library.feature.auth.SaslEcdsaNist256PChallenge;
import org.kitteh.irc.client.library.feature.auth.SaslPlain;
import org.kitteh.irc.client.library.util.Resettable;
import org.kitteh.irc.client.library.util.RiskyBusiness;
import org.kitteh.irc.client.library.util.Sanity;

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
         *
         * @see User#getAccount()
         */
        public static final String ACCOUNT_TAG = "account-tag";

        /**
         * Away notification.
         *
         * @see User#isAway()
         */
        public static final String AWAY_NOTIFY = "away-notify";

        /**
         * Batched messages.
         */
        public static final String BATCH = "batch";

        /**
         * Capability change notification. Implicitly enabled by the server
         * when "CAP LS 302" (or higher version) is sent and therefore it
         * is not requested by the default capability manager.
         */
        public static final transient String CAP_NOTIFY = "cap-notify";

        /**
         * Self-sent message echoing, not utilized unless requested.
         */
        public static final transient String ECHO_MESSAGE = "echo-message";

        /**
         * Account listed in join message.
         *
         * @see User#getAccount()
         */
        public static final String EXTENDED_JOIN = "extended-join";

        /**
         * Invite notification, not utilized unless requested.
         *
         * @see ChannelInviteEvent
         */
        public static final transient String INVITE_NOTIFY = "invite-notify";

        /**
         * Multiple prefixes sent in NAMES and WHO output.
         *
         * @see Channel#getUserModes
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
         * @see SaslEcdsaNist256PChallenge
         */
        public static final transient String SASL = "sasl";

        /**
         * User hosts sent in NAMES, allowing User creation prior to WHO.
         */
        public static final String USERHOST_IN_NAMES = "userhost-in-names";

        private static final List<String> DEFAULTS;
        private static final Supplier<List<String>> SUPPLIER = ArrayList::new;

        private Defaults() {
        }

        /**
         * Gets all capabilities requested by KICL by default.
         *
         * @return all capability names
         */
        public static List<String> getDefaults() {
            return DEFAULTS;
        }

        static {
            DEFAULTS = Collections.unmodifiableList(Arrays.stream(Defaults.class.getDeclaredFields())
                    .filter(field -> Modifier.isPublic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()))
                    .map(Defaults::getStringForCapabilityField).collect(Collectors.toCollection(SUPPLIER)));
        }

        /**
         * Tries to get the String value of a capability field, provided
         * it's accessible.
         *
         * @param field the field to try and get the value for
         * @return the capability extension name
         */
        @SuppressWarnings("ConstantConditions")
        private static String getStringForCapabilityField(@NonNull Field field) {
            return RiskyBusiness.assertSafe(f -> (String) f.get(null), field);
        }
    }

    /**
     * A capability manager with management features.
     */
    interface WithManagement extends CapabilityManager, Resettable {
        /**
         * Gets if we are still in negotiation. True on construction and
         * after a {@link #reset()}.
         *
         * @return true if still negotiating
         */
        boolean isNegotiating();

        /**
         * Ends negotiation status, making {@link #isNegotiating()} false.
         */
        void endNegotiation();

        /**
         * Updates the current active capabilities, adding new and removing
         * any labeled with {@link CapabilityState#isDisabled()}.
         *
         * @param capabilityStates capability states
         */
        void updateCapabilities(@NonNull List<CapabilityState> capabilityStates);

        /**
         * Wipes the previously known active capabilities, setting only those
         * in the provided list.
         *
         * @param capabilityStates fresh set of capability states
         */
        void setCapabilities(@NonNull List<CapabilityState> capabilityStates);

        /**
         * Sets the supported capabilities as reported by the server.
         *
         * @param capabilityStates supported capabilities
         */
        void setSupportedCapabilities(@NonNull List<CapabilityState> capabilityStates);
    }

    /**
     * Gets capabilities currently enabled.
     *
     * @return the capabilities currently enabled
     * @see CapabilityRequestCommand
     */
    @NonNull List<CapabilityState> getCapabilities();

    /**
     * Gets an enabled capability by name.
     *
     * @param name capability name
     * @return the named capability if enabled
     */
    default @NonNull Optional<CapabilityState> getCapability(@NonNull String name) {
        Sanity.nullCheck(name, "Name cannot be null");
        return this.getCapabilities().stream().filter(capabilityState -> capabilityState.getName().equals(name)).findFirst();
    }

    /**
     * Gets capabilities supported by the server.
     *
     * @return the capabilities supported
     * @see CapabilityRequestCommand
     */
    @NonNull List<CapabilityState> getSupportedCapabilities();

    /**
     * Gets a supported capability by name.
     *
     * @param name capability name
     * @return the named capability if supported
     */
    default @NonNull Optional<CapabilityState> getSupportedCapability(@NonNull String name) {
        Sanity.nullCheck(name, "Name cannot be null");
        return this.getSupportedCapabilities().stream().filter(capabilityState -> capabilityState.getName().equals(name)).findFirst();
    }
}
