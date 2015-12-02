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
package org.kitteh.irc.client.library.command;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * How meta.
 */
public abstract class MetadataCommand extends Command {
    /**
     * Clears all metadata for a given target.
     */
    public class Clear extends MetadataCommand {
        /**
         * Constructs the command.
         *
         * @param client the client
         * @param target the target
         * @throws IllegalArgumentException if client is null
         */
        public Clear(@Nonnull Client client, @Nonnull String target) {
            super(client, target);
        }

        @Override
        public void execute() {
            this.getClient().sendRawLine(this.getStart() + "CLEAR");
        }
    }

    /**
     * Gets the requested metadata for a given target.
     */
    public class Get extends MetadataCommand {
        private final java.util.List<String> keys = new CopyOnWriteArrayList<>();

        /**
         * Constructs the command.
         *
         * @param client the client
         * @param target the target
         * @throws IllegalArgumentException if client is null
         */
        public Get(@Nonnull Client client, @Nonnull String target) {
            super(client, target);
        }

        /**
         * Adds a key to the GET request.
         *
         * @param key key to add
         * @return this command
         */
        @Nonnull
        public Get key(@Nonnull String key) {
            Sanity.safeMessageCheck(key, "key");
            Sanity.truthiness(METADATA_KEY_PATTERN.matcher(key).matches(), "Invalid key [" + key + ']');
            this.keys.add(key);
            return this;
        }

        @Override
        public void execute() {
            if (this.keys.isEmpty()) {
                throw new IllegalStateException("No keys added");
            }
            StringBuilder builder = new StringBuilder(200);
            builder.append(this.getStart()).append("GET ");
            for (String key : this.keys) {
                if (builder.length() > 0) {
                    if ((key.length() + builder.length()) > 200) {
                        this.getClient().sendRawLine(builder.toString());
                        builder.setLength(0);
                        builder.append(this.getStart()).append("GET ");
                    } else if (builder.length() > 0) {
                        builder.append(' ');
                    }
                }
                builder.append(key);
            }
            this.getClient().sendRawLine(builder.toString());
        }
    }

    /**
     * Lists the metadata for a given target.
     */
    public class List extends MetadataCommand {
        /**
         * Constructs the command.
         *
         * @param client the client
         * @param target the target
         * @throws IllegalArgumentException if client is null
         */
        public List(@Nonnull Client client, @Nonnull String target) {
            super(client, target);
        }

        @Override
        public void execute() {
            this.getClient().sendRawLine(this.getStart() + "LIST");
        }
    }

    /**
     * Sets the metadata for a given target.
     */
    public class Set extends MetadataCommand {
        private final java.util.List<String> metadata = new CopyOnWriteArrayList<>();

        /**
         * Constructs the command.
         *
         * @param client the client
         * @param target the target
         * @throws IllegalArgumentException if client is null
         */
        public Set(@Nonnull Client client, @Nonnull String target) {
            super(client, target);
        }

        /**
         * Sets a key and value.
         *
         * @param key key to add
         * @param value value to set
         * @return this command
         */
        @Nonnull
        public Set set(@Nonnull String key, @Nonnull String value) {
            Sanity.safeMessageCheck(key, "key");
            Sanity.truthiness(METADATA_KEY_PATTERN.matcher(key).matches(), "Invalid key [" + key + ']');
            this.metadata.add(key + ' ' + Sanity.safeMessageCheck(value, "value"));
            return this;
        }

        /**
         * Removes a key.
         *
         * @param key key to remove
         * @return this command
         */
        @Nonnull
        public Set unset(@Nonnull String key) {
            Sanity.safeMessageCheck(key, "key");
            Sanity.truthiness(METADATA_KEY_PATTERN.matcher(key).matches(), "Invalid key [" + key + ']');
            this.metadata.add(key);
            return this;
        }

        @Override
        public void execute() {
            if (this.metadata.isEmpty()) {
                throw new IllegalStateException("No keys added");
            }
            for (String data : this.metadata) {
                this.getClient().sendRawLine(this.getStart() + "SET " + data);
            }
        }
    }

    /**
     * Pattern checking for valid key characters (alphanumeric, period,
     * underscore, and colon).
     */
    public static final Pattern METADATA_KEY_PATTERN = Pattern.compile("[A-Za-z0-9_\\.:]+");
    private static final String METADATA_START = "METADATA";

    private final String target;

    /**
     * Constructs the command.
     *
     * @param client the client
     * @param target the target
     * @throws IllegalArgumentException if client is null
     */
    protected MetadataCommand(@Nonnull Client client, @Nonnull String target) {
        super(client);
        this.target = Sanity.safeMessageCheck(target);
    }

    /**
     * Gets the start to the METADATA command.
     *
     * @return "METADATA TARGET "
     */
    @Nonnull
    protected String getStart() {
        return METADATA_START + ' ' + this.target + ' ';
    }
}
