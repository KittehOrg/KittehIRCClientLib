package org.kitteh.irc.client.library.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Represents a class that serves no other purpose than to provide the current KittehIRCClientLib version.
 */
public final class KiclVersion {

    /**
     * KittehIRCClientLib maven version at time of compilation.
     */
    public static final String VERSION;

    static {
        String version = "unknown";
        // try to load from maven properties first
        try {
            Properties p = new Properties();
            InputStream is = KiclVersion.class.getResourceAsStream("/META-INF/maven/org.kitteh.irc/client-lib/pom.properties");
            if (is != null) {
                p.load(is);
                version = p.getProperty("version", version);
            }
        } catch (Exception ignored) {
        }
        VERSION = version;
    }

}
