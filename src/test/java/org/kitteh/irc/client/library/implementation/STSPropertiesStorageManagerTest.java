package org.kitteh.irc.client.library.implementation;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kitteh.irc.client.library.feature.sts.STSPropertiesStorageManager;
import org.kitteh.irc.client.library.feature.sts.STSStorageManager;
import org.kitteh.irc.client.library.util.STSUtil;
import org.kitteh.irc.client.library.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
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
    public void testSimpleOperations() {
        StringWriter sw = new StringWriter();
        StringReader sr = new StringReader("");

        STSPropertiesStorageManager sut = new STSPropertiesStorageManager(sw, sr);
        sut.addEntry("kitteh.org", 500, StringUtil.parseSeparatedKeyValueString(",", "port=6697,cats"));

        Assert.assertTrue(sut.hasEntry("kitteh.org"));
        final Map<String, Optional<String>> entry = sut.getEntry("kitteh.org");
        Assert.assertTrue(entry.containsKey("port"));
        Assert.assertTrue(entry.get("port").isPresent());
        Assert.assertTrue(entry.get("port").get().equals("6697"));
        Assert.assertTrue(entry.containsKey("cats"));
        sut.removeEntry("kitteh.org");
        Assert.assertFalse(sut.hasEntry("kitteh.org"));
    }

    /**
     * Checks that the simple bundled storage manager works
     * with reading.
     */
    @Test
    public void testSimpleReading() {
        StringWriter sw = new StringWriter();
        StringReader sr = new StringReader("");
        STSPropertiesStorageManager sut1 = new STSPropertiesStorageManager(sw, sr);
        sut1.addEntry("kitteh.org", 500, StringUtil.parseSeparatedKeyValueString(",", "port=6697,cats"));

        Assert.assertTrue(sut1.hasEntry("kitteh.org"));
        STSPropertiesStorageManager sut2 = new STSPropertiesStorageManager(new StringWriter(), new StringReader(sw.toString()));
        Assert.assertTrue(sut2.hasEntry("kitteh.org"));
        final Map<String, Optional<String>> entry = sut2.getEntry("kitteh.org");

        Assert.assertTrue(entry.containsKey("port"));
        Assert.assertTrue(entry.get("port").isPresent());
        Assert.assertTrue(entry.get("port").get().equals("6697"));
        Assert.assertTrue(entry.containsKey("cats"));
    }

    /**
     * Checks that the simple bundled storage manager works
     * with expiration (UGLY).
     */
    @Test
    public void testDelay() {
        StringWriter sw = new StringWriter();
        StringReader sr = new StringReader("");
        STSPropertiesStorageManager sut = new STSPropertiesStorageManager(sw, sr);
        sut.addEntry("kitteh.org", 0, StringUtil.parseSeparatedKeyValueString(",", "port=6697,cats"));
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final AtomicBoolean okay = new AtomicBoolean(true);
        scheduler.schedule(() -> okay.set(sut.hasEntry("kitteh.org")), 1000, TimeUnit.MILLISECONDS);
        try {
            scheduler.awaitTermination(3000, TimeUnit.MILLISECONDS);
            Assert.assertFalse(okay.get());
        } catch (InterruptedException e) {
            Assert.fail("Thread interrupted");
        }
    }

    @Test
    public void testWithFiles() {
        try {
            final File tempFile = temporaryFolder.newFile("sts.properties");
            try (STSStorageManager sut1 = STSUtil.getDefaultStorageManager(tempFile)) {
                sut1.addEntry("kitteh.org", 500, StringUtil.parseSeparatedKeyValueString(",", "port=6697,cats"));
                Assert.assertTrue(sut1.hasEntry("kitteh.org"));
            } catch (Exception e) {
                Assert.fail();
            }

            try (STSStorageManager sut2 = STSUtil.getDefaultStorageManager(tempFile)) {
                Assert.assertTrue(sut2.hasEntry("kitteh.org"));
            } catch (Exception e) {
                Assert.fail();
            }

        } catch (IOException e) {
            Assert.fail();
        }

    }


}
