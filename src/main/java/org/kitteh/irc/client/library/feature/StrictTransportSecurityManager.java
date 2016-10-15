package org.kitteh.irc.client.library.feature;

public interface StrictTransportSecurityManager {
    public void addEntry(String hostname, long duration);
    public boolean hasEntry(String hostname);
    public void removeEntry(String hostname);
}
