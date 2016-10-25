package org.kitteh.irc.client.library.feature.sts;

public interface StsStorageManager {
    void addEntry(String hostname, long duration);
    boolean hasEntry(String hostname);
    void removeEntry(String hostname);
}
