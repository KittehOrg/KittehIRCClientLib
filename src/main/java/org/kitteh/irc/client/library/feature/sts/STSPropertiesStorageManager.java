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
package org.kitteh.irc.client.library.feature.sts;

import org.kitteh.irc.client.library.exception.KittehSTSException;
import org.kitteh.irc.client.library.util.StringUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * Simple example implementation of an STSStorageManager.
 */
public class STSPropertiesStorageManager implements STSStorageManager {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private final Properties properties = new Properties();
    private final Writer writer;

    /**
     * Simple implementation of STSStorageManager which uses a properties file.
     */
    public STSPropertiesStorageManager(Writer writer, Reader reader) {
        this.readData(reader);
        this.writer = writer;
    }

    /**
     * Reads in old data.
     */
    public void readData(Reader reader) {
        try {
            this.properties.load(reader);
            reader.close();
        } catch (IOException e) {
            throw new KittehSTSException(e.getMessage());
        }
    }

    /**
     * Adds an STS policy to the store.
     *
     * @param hostname the hostname (as sent in the SNI by the client)
     * @param duration the length (in seconds) until the expiry of this stored policy
     * @param data all data sent by the server in the CAP LS "sts" value when connecting securely
     */
    @Override
    public void addEntry(String hostname, long duration, Map<String, Optional<String>> data) {
        this.properties.setProperty(hostname, getExpiryFromDuration(duration) + "; " + this.reserializeData(data));
        this.saveData();
    }

    /**
     * Saves data using the provided Writer.
     */
    private void saveData() {
        try {
            this.properties.store(this.writer, "This file contains all the gathered STS policies.");
        } catch (IOException e) {
            throw new KittehSTSException(e.getMessage());
        }
    }

    /**
     * Gets an STS policy from the store, looking it up via hostname.
     *
     * @param hostname the hostname (as sent in the SNI by the client)
     * @return all data sent by the server in the CAP LS "sts" value when we connected securely
     */
    @Override
    public Map<String, Optional<String>> getEntry(String hostname) {
        this.pruneEntries();
        if (!this.hasEntry(hostname)) {
            return null;
        }

        String value = this.properties.getProperty(hostname);
        String[] components = value.split("; ");
        String data = components[1];
        return StringUtil.parseSeparatedKeyValueString(",", data);
    }

    /**
     * Checks if a policy has been stored for the hostname.
     *
     * @param hostname the hostname to check
     * @return whether the entry exists in the store
     */
    @Override
    public boolean hasEntry(String hostname) {
        this.pruneEntries();
        return this.properties.containsKey(hostname);
    }

    /**
     * Cleans up the outdated entries from the properties instance.
     */
    private void pruneEntries() {
        Set<String> stagedRemovals = new HashSet<>();

        for (Object keyObj : this.properties.keySet()) {
            String hostname = (String) keyObj;

            String value = this.properties.getProperty(hostname);
            String[] components = value.split("; ");
            ZonedDateTime dt = ZonedDateTime.parse(components[0], DATE_TIME_FORMATTER);
            if (dt.isBefore(ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))) {
                stagedRemovals.add(hostname); // CME
            }
        }

        stagedRemovals.forEach(this.properties::remove);
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
    public void removeEntry(String hostname) {
        if (!this.hasEntry(hostname)) {
            return;
        }
        this.properties.remove(hostname);
    }

    /**
     * Reserialize the STS data in the same form as used in the spec.
     *
     * @param data The map of keys -> optional string values
     * @return a serialized string
     */
    private String reserializeData(Map<String, Optional<String>> data) {
        StringBuilder sb = new StringBuilder(data.keySet().size()*5);
        Iterator<String> iterator = data.keySet().iterator();
        while (iterator.hasNext()) {
            String s = iterator.next();

            sb.append(s);
            final Optional<String> value = data.get(s);
            if (value.isPresent()) {
                sb.append("=").append(value.get());
            }
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * Returns an ISO8601 DateTime string using UTC.
     *
     * @param duration the time in seconds from now
     * @return the date-time string e.g. 2016-01-01T00:00:00Z
     */
    private String getExpiryFromDuration(long duration) {
        Instant now = Instant.now();
        Instant then = now.plusSeconds(duration);
        return ZonedDateTime.ofInstant(then, ZoneOffset.UTC).format(DATE_TIME_FORMATTER);
    }


    @Override
    public void close() throws Exception {
        System.out.println("Called close");
        this.writer.flush();
        this.writer.close();
    }
}
