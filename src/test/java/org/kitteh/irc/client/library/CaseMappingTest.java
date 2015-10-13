package org.kitteh.irc.client.library;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Tests case mappings.
 */
public class CaseMappingTest {
    @Test
    public void verifyMatch() {
        for (CaseMapping caseMapping : CaseMapping.values()) {
            Optional<CaseMapping> acquired = CaseMapping.getByName(caseMapping.name().replace('_', '-'));
            Assert.assertTrue("Failed to acquire mapping for " + caseMapping.name(), acquired.isPresent());
            Assert.assertEquals(caseMapping, acquired.get());
        }
        Assert.assertEquals(Optional.empty(), CaseMapping.getByName(null));
    }

    @Test
    public void lowerCase() {
        Map<CaseMapping, Pair<String, String>> test = new HashMap<CaseMapping, Pair<String, String>>() {
            {
                this.put(CaseMapping.ASCII, new Pair<>("abcdwxyzABCDWXYZ!@#$%^&*(){}[];':,.<>", "abcdwxyzabcdwxyz!@#$%^&*(){}[];':,.<>"));
                this.put(CaseMapping.RFC1459, new Pair<>("abcdwxyzABCDWXYZ!@#$%^&*(){}[];':,.<>", "abcdwxyzabcdwxyz!@#$%~&*(){}{};':,.<>"));
                this.put(CaseMapping.STRICT_RFC1459, new Pair<>("abcdwxyzABCDWXYZ!@#$%^&*(){}[];':,.<>", "abcdwxyzabcdwxyz!@#$%^&*(){}{};':,.<>"));
            }
        };
        for (CaseMapping caseMapping : CaseMapping.values()) {
            Assert.assertTrue("Missing CaseMapping " + caseMapping.name(), test.containsKey(caseMapping));
        }
        for (Map.Entry<CaseMapping, Pair<String, String>> entry : test.entrySet()) {
            Assert.assertEquals("", entry.getKey().toLowerCase(entry.getValue().getLeft()), entry.getValue().getRight());
        }
    }
}
