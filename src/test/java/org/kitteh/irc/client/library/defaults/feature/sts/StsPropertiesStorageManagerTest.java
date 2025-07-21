package org.kitteh.irc.client.library.defaults.feature.sts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kitteh.irc.client.library.feature.sts.StsPolicy;
import org.kitteh.irc.client.library.feature.sts.StsPropertiesStorageManager;
import org.kitteh.irc.client.library.feature.sts.StsStorageManager;
import org.kitteh.irc.client.library.util.StsUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class StsPropertiesStorageManagerTest {
    @TempDir
    public File temporaryFolder;

    /**
     * Checks that the simple bundled storage manager works.
     */
    @Test
    public void testSimpleOperations() {
        final File tempFile = new File(this.temporaryFolder, "sts.properties");
        final Path path = tempFile.toPath();

        StsPropertiesStorageManager sut = new StsPropertiesStorageManager(path);
        sut.addEntry("kitteh.org", 500, StsUtil.getStsPolicyFromString(",", StsPolicy.POLICY_OPTION_KEY_PORT + "=6697,cats"));

        Assertions.assertTrue(sut.hasEntry("kitteh.org"));
        final Optional<StsPolicy> optionalPolicy = sut.getEntry("kitteh.org");
        Assertions.assertTrue(optionalPolicy.isPresent());
        final StsPolicy policy = optionalPolicy.get();
        Assertions.assertTrue(policy.getOptions().containsKey(StsPolicy.POLICY_OPTION_KEY_PORT));
        Assertions.assertEquals("6697", policy.getOptions().get(StsPolicy.POLICY_OPTION_KEY_PORT));
        Assertions.assertTrue(policy.getFlags().contains("cats"));
    }

    /**
     * Checks that the simple bundled storage manager works
     * with reading.
     */
    @Test
    public void testSimpleReading() {
        final File tempFile = new File(this.temporaryFolder, "sts.properties");
        final Path path = tempFile.toPath();
        StsPropertiesStorageManager sut1 = new StsPropertiesStorageManager(path);
        sut1.addEntry("kitteh.org", 500, StsUtil.getStsPolicyFromString(",", StsPolicy.POLICY_OPTION_KEY_PORT + "=6697,cats"));

        Assertions.assertTrue(sut1.hasEntry("kitteh.org"));
        StsPropertiesStorageManager sut2 = new StsPropertiesStorageManager(path);
        Assertions.assertTrue(sut2.hasEntry("kitteh.org"));
        final Optional<StsPolicy> optionalPolicy = sut2.getEntry("kitteh.org");
        Assertions.assertTrue(optionalPolicy.isPresent());
        final StsPolicy policy = optionalPolicy.get();
        Assertions.assertTrue(policy.getOptions().containsKey(StsPolicy.POLICY_OPTION_KEY_PORT));
        Assertions.assertEquals("6697", policy.getOptions().get(StsPolicy.POLICY_OPTION_KEY_PORT));
        Assertions.assertTrue(policy.getFlags().contains("cats"));
    }

    /**
     * Checks that the simple bundled storage manager works
     * with expiration (UGLY).
     */
    @Test
    public void testDelay() throws InterruptedException {
        final File tempFile = new File(this.temporaryFolder, "sts.properties");
        final Path path = tempFile.toPath();
        StsPropertiesStorageManager sut = new StsPropertiesStorageManager(path);
        sut.addEntry("kitteh.org", 0, StsUtil.getStsPolicyFromString(",", StsPolicy.POLICY_OPTION_KEY_PORT + "=6697,cats"));
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final AtomicBoolean okay = new AtomicBoolean(true);
        scheduler.schedule(() -> okay.set(sut.hasEntry("kitteh.org")), 1000, TimeUnit.MILLISECONDS);
        scheduler.awaitTermination(3000, TimeUnit.MILLISECONDS);
        Assertions.assertFalse(okay.get());
    }

    @Test
    public void testReloading() {
        final File tempFile = new File(this.temporaryFolder, "sts.properties");
        final Path path = tempFile.toPath();
        StsStorageManager sut1 = StsUtil.getDefaultStorageManager(path);
        sut1.addEntry("kitteh.org", 500, StsUtil.getStsPolicyFromString(",", StsPolicy.POLICY_OPTION_KEY_PORT + "=6697,cats"));
        StsStorageManager sut2 = StsUtil.getDefaultStorageManager(path);
        Assertions.assertTrue(sut2.hasEntry("kitteh.org"));
    }
}
