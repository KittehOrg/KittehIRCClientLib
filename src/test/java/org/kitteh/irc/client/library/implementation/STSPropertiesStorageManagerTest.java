package org.kitteh.irc.client.library.implementation;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kitteh.irc.client.library.feature.sts.STSPolicy;
import org.kitteh.irc.client.library.feature.sts.STSPropertiesStorageManager;
import org.kitteh.irc.client.library.feature.sts.STSStorageManager;
import org.kitteh.irc.client.library.util.STSUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class STSPropertiesStorageManagerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Checks that the simple bundled storage manager works.
     */
    @Test
    public void testSimpleOperations() throws IOException {
        final File tempFile = this.temporaryFolder.newFile("sts.properties");
        final Path path = tempFile.toPath();

        STSPropertiesStorageManager sut = new STSPropertiesStorageManager(path);
        sut.addEntry("kitteh.org", 500, STSUtil.getSTSPolicyFromString(",", STSPolicy.POLICY_OPTION_KEY_PORT + "=6697,cats"));

        Assert.assertTrue(sut.hasEntry("kitteh.org"));
        final Optional<STSPolicy> optionalPolicy = sut.getEntry("kitteh.org");
        Assert.assertTrue(optionalPolicy.isPresent());
        final STSPolicy policy = optionalPolicy.get();
        Assert.assertTrue(policy.getOptions().containsKey(STSPolicy.POLICY_OPTION_KEY_PORT));
        Assert.assertTrue("6697".equals(policy.getOptions().get(STSPolicy.POLICY_OPTION_KEY_PORT)));
        Assert.assertTrue(policy.getFlags().contains("cats"));
    }

    /**
     * Checks that the simple bundled storage manager works
     * with reading.
     */
    @Test
    public void testSimpleReading() throws IOException {
        final File tempFile = this.temporaryFolder.newFile("sts.properties");
        final Path path = tempFile.toPath();
        STSPropertiesStorageManager sut1 = new STSPropertiesStorageManager(path);
        sut1.addEntry("kitteh.org", 500, STSUtil.getSTSPolicyFromString(",", STSPolicy.POLICY_OPTION_KEY_PORT + "=6697,cats"));

        Assert.assertTrue(sut1.hasEntry("kitteh.org"));
        STSPropertiesStorageManager sut2 = new STSPropertiesStorageManager(path);
        Assert.assertTrue(sut2.hasEntry("kitteh.org"));
        final Optional<STSPolicy> optionalPolicy = sut2.getEntry("kitteh.org");
        Assert.assertTrue(optionalPolicy.isPresent());
        final STSPolicy policy = optionalPolicy.get();
        Assert.assertTrue(policy.getOptions().containsKey(STSPolicy.POLICY_OPTION_KEY_PORT));
        Assert.assertTrue("6697".equals(policy.getOptions().get(STSPolicy.POLICY_OPTION_KEY_PORT)));
        Assert.assertTrue(policy.getFlags().contains("cats"));
    }

    /**
     * Checks that the simple bundled storage manager works
     * with expiration (UGLY).
     */
    @Test
    public void testDelay() throws InterruptedException, IOException {
        final File tempFile = this.temporaryFolder.newFile("sts.properties");
        final Path path = tempFile.toPath();
        STSPropertiesStorageManager sut = new STSPropertiesStorageManager(path);
        sut.addEntry("kitteh.org", 0, STSUtil.getSTSPolicyFromString(",", STSPolicy.POLICY_OPTION_KEY_PORT + "=6697,cats"));
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final AtomicBoolean okay = new AtomicBoolean(true);
        scheduler.schedule(() -> okay.set(sut.hasEntry("kitteh.org")), 1000, TimeUnit.MILLISECONDS);
        scheduler.awaitTermination(3000, TimeUnit.MILLISECONDS);
        Assert.assertFalse(okay.get());
    }

    @Test
    public void testReloading() throws IOException {
        final File tempFile = this.temporaryFolder.newFile("sts.properties");
        final Path path = tempFile.toPath();
        STSStorageManager sut1 = STSUtil.getDefaultStorageManager(path);
        sut1.addEntry("kitteh.org", 500, STSUtil.getSTSPolicyFromString(",", STSPolicy.POLICY_OPTION_KEY_PORT + "=6697,cats"));
        STSStorageManager sut2 = STSUtil.getDefaultStorageManager(path);
        Assert.assertTrue(sut2.hasEntry("kitteh.org"));
    }


}
