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
package org.kitteh.irc.client.library.feature.twitch.messagetag;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.ToStringer;
import org.kitteh.irc.client.library.util.TriFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * We don't need no stinkin' badges.
 */
public class Badges extends MessageTagManager.DefaultMessageTag {
    /**
     * Name of this message tag.
     */
    public static final String NAME = "badges";

    /**
     * Function to create this message tag.
     */
    @SuppressWarnings("ConstantConditions")
    public static final TriFunction<Client, String, String, Badges> FUNCTION = (client, name, value) -> new Badges(name, value);

    /**
     * Known badge names.
     */
    public static final class KnownNames {
        private KnownNames() {
        }

        /**
         * Admin.
         */
        public static final String ADMIN = "admin";

        /**
         * Bits.
         */
        public static final String BITS = "bits";

        /**
         * Broadcaster.
         */
        public static final String BROADCASTER = "broadcaster";

        /**
         * Global mod.
         */
        public static final String GLOBAL_MOD = "global_mod";

        /**
         * Moderator.
         */
        public static final String MODERATOR = "moderator";

        /**
         * Subscriber.
         */
        public static final String SUBSCRIBER = "subscriber";

        /**
         * Staff.
         */
        public static final String STAFF = "staff";

        /**
         * Turbo.
         */
        public static final String TURBO = "turbo";
    }

    /**
     * One badge.
     */
    public class Badge {
        private final String name;
        private final String version;

        private Badge(@NonNull String name, @NonNull String version) {
            this.name = name;
            this.version = version;
        }

        /**
         * Gets the badge name.
         *
         * @return badge name
         */
        public @NonNull String getName() {
            return this.name;
        }

        /**
         * Gets the badge version.
         *
         * @return badge version
         */
        public @NonNull String getVersion() {
            return this.version;
        }

        @Override
        public @NonNull String toString() {
            return new ToStringer(this).add("name", this.name).add("version", this.version).toString();
        }
    }

    private final List<Badge> badges;

    private Badges(@NonNull String name, @Nullable String value) {
        super(name, value);
        if (value == null) {
            this.badges = Collections.emptyList();
        } else {
            List<Badge> badges = new ArrayList<>();
            String[] badgesSplit = value.split(",");
            for (String badgeInfo : badgesSplit) {
                String[] split = badgeInfo.split("/");
                String version;
                if (split.length > 1) {
                    version = split[1];
                } else {
                    version = "";
                }
                badges.add(new Badge(split[0], version));
            }

            this.badges = Collections.unmodifiableList(badges);
        }
    }

    /**
     * Gets badges.
     *
     * @return list of badges
     */
    public @NonNull List<Badge> getBadges() {
        return this.badges;
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("badges", this.badges);
    }
}
