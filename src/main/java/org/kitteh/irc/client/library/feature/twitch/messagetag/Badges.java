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
package org.kitteh.irc.client.library.feature.twitch.messagetag;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.MessageTagManager;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * We don't need no stinkin' badges.
 */
public class Badges extends MessageTagManager.DefaultMessageTag {
    public static final TriFunction<Client, String, Optional<String>, Badges> FUNCTION = (client, name, value) -> new Badges(name, value);

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

        private Badge(@Nonnull String name, @Nonnull String version) {
            this.name = name;
            this.version = version;
        }

        /**
         * Gets the badge name.
         *
         * @return badge name
         */
        @Nonnull
        public String getName() {
            return this.name;
        }

        /**
         * Gets the badge version.
         *
         * @return badge version
         */
        @Nonnull
        public String getVersion() {
            return this.version;
        }
    }

    private final List<Badge> badges;

    /**
     * Constructs the message tag.
     *
     * @param name tag name
     * @param value tag value or {@link Optional#empty()}
     */
    public Badges(@Nonnull String name, @Nonnull Optional<String> value) {
        super(name, value);
        if (!value.isPresent()) {
            this.badges = Collections.unmodifiableList(new ArrayList<>());
        } else {
            List<Badge> badges = new ArrayList<>();
            String[] badgesSplit = value.get().split(",");
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
    @Nonnull
    public List<Badge> getBadges() {
        return this.badges;
    }
}
