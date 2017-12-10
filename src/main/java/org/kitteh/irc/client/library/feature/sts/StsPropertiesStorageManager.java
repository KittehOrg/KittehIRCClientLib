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
package org.kitteh.irc.client.library.feature.sts;

import org.kitteh.irc.client.library.exception.KittehStsException;
import org.kitteh.irc.client.library.util.StsUtil;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Simple example implementation of an STSStorageManager.
 */
public class StsPropertiesStorageManager implements StsStorageManager {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private final Properties properties = new Properties();
    private final Path filePath;

    /**
     * Simple implementation of STSStorageManager which uses a properties file.
     *
     * @param filePath the path to the properties file used to persist the data
     */
    public StsPropertiesStorageManager(@Nonnull Path filePath) {
        this.filePath = Sanity.nullCheck(filePath, "Must provide a valid path to the properties file to use.");
        this.readData();
    }

    private void readData() {
        if (!Files.exists(this.filePath)) {
            return;
        }

        try (BufferedReader bufferedReader = Files.newBufferedReader(this.filePath, StandardCharsets.UTF_8)) {
            this.properties.load(bufferedReader);
        } catch (IOException e) {
            throw new KittehStsException(e.getMessage(), e);
        }
    }

    /**
     * Adds an entry to the store, storing data in the backing properties file.
     *
     * @param hostname the hostname (as sent in the SNI by the client)
     * @param duration the length (in seconds) until the expiry of this stored policy
     * @param policy the STS policy instance, including all data sent from the server
     */
    @Override
    public void addEntry(@Nonnull String hostname, long duration, @Nonnull StsPolicy policy) {
        Sanity.nullCheck(hostname, "A valid hostname must be provided for this entry.");
        Sanity.nullCheck(policy, "A valid policy must be provided to be inserted.");
        if (!policy.getOptions().containsKey("duration")) {
            policy.getOptions().put("duration", String.valueOf(duration));
        }
        this.properties.setProperty(hostname, this.getExpiryFromDuration(duration) + "; " + this.reserializeData(policy));
        this.saveData();
    }

    /**
     * Saves data using the provided Writer.
     */
    private void saveData() {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.filePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            this.properties.store(bufferedWriter, "This file contains all the gathered STS policies.");
        } catch (IOException e) {
            throw new KittehStsException(e.getMessage());
        }
    }

    /**
     * Gets an STS policy from the store, looking it up via hostname.
     *
     * @param hostname the hostname (as sent in the SNI by the client)
     * @return all data sent by the server in the CAP LS "sts" value when we connected securely
     */
    @Override
    @Nonnull
    public Optional<StsPolicy> getEntry(@Nonnull String hostname) {
        Sanity.nullCheck(hostname, "A valid hostname must be provided for this entry.");

        this.pruneEntries();
        if (!this.hasEntry(hostname)) {
            return Optional.empty();
        }

        String value = this.properties.getProperty(hostname);
        String[] components = value.split("; ");
        String data = components[1];
        return Optional.of(StsUtil.getStsPolicyFromString(",", data));
    }

    /**
     * Checks if a policy has been stored for the hostname.
     *
     * @param hostname the hostname to check
     * @return whether the entry exists in the store
     */
    @Override
    public boolean hasEntry(@Nonnull String hostname) {
        Sanity.nullCheck(hostname, "A valid hostname must be provided for this entry.");

        this.pruneEntries();
        return this.properties.containsKey(hostname);
    }

    /**
     * Cleans up the outdated entries from the properties instance.
     */
    private void pruneEntries() {
        Set<String> stagedRemovals = new HashSet<>();

        for (String hostname : this.properties.stringPropertyNames()) {
            String value = this.properties.getProperty(hostname);
            String[] components = value.split("; ");
            ZonedDateTime dt = ZonedDateTime.parse(components[0], DATE_TIME_FORMATTER);
            if (dt.isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
                stagedRemovals.add(hostname); // CME
            }
        }

        this.properties.keySet().removeAll(stagedRemovals);
        this.saveData();
    }

    /**
     * Deletes an entry from the store (used for 0 duration policies).
     * <p>
     * Implementers MUST ignore requests to remove entries that do not exist.
     *
     * @param hostname the hostname to remove the policy for
     */
    @Override
    public void removeEntry(@Nonnull String hostname) {
        Sanity.nullCheck(hostname, "A valid hostname must be provided for this entry.");

        this.pruneEntries();
        this.properties.remove(hostname);
    }

    /**
     * Reserialize the STS policy in the same form as used in the spec.
     *
     * @param policy The map of keys -> optional string values
     * @return a serialized string
     */
    private String reserializeData(StsPolicy policy) {
        StringBuilder sb = new StringBuilder((policy.getOptions().size() * 10) + (policy.getFlags().size() * 5));
        sb.append(String.join(",", policy.getFlags()));
        if (!policy.getFlags().isEmpty()) {
            sb.append(',');
        }
        sb.append(policy.getOptions().entrySet().stream().map(e -> e.getKey() + '=' + e.getValue()).collect(Collectors.joining(",")));
        return sb.toString();
    }

    /**
     * Returns an ISO8601 DateTime string using UTC.
     *
     * @param duration the time in seconds from now
     * @return the date-time string e.g. 2016-01-01T00:00:00Z
     */
    private String getExpiryFromDuration(long duration) {
        return ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(duration).format(DATE_TIME_FORMATTER);
    }
}
