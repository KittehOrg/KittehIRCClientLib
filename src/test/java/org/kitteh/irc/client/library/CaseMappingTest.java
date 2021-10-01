package org.kitteh.irc.client.library;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.feature.CaseMapping;
import org.kitteh.irc.client.library.util.Pair;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Tests case mappings.
 */
public class CaseMappingTest {
    /**
     * Verifies all casemapping enums match names.
     */
    @Test
    public void verifyMatch() {
        for (CaseMapping caseMapping : CaseMapping.values()) {
            Optional<CaseMapping> acquired = CaseMapping.getByName(caseMapping.name().replace('_', '-'));
            Assertions.assertTrue(acquired.isPresent(), "Failed to acquire mapping for " + caseMapping.name());
            Assertions.assertEquals(caseMapping, acquired.get());
        }
        Assertions.assertEquals(Optional.empty(), CaseMapping.getByName(null));
    }

    /**
     * Tests casemapping lowercasing.
     */
    @Test
    public void lowerCase() {
        Map<CaseMapping, Pair<String, String>> test = new EnumMap<>(CaseMapping.class);
        test.put(CaseMapping.ASCII, new Pair<>("abcdwxyzABCDWXYZ!@#$%^&*(){}[];':,.<>", "abcdwxyzabcdwxyz!@#$%^&*(){}[];':,.<>"));
        test.put(CaseMapping.RFC1459, new Pair<>("abcdwxyzABCDWXYZ!@#$%^&*(){}[];':,.<>", "abcdwxyzabcdwxyz!@#$%~&*(){}{};':,.<>"));
        test.put(CaseMapping.STRICT_RFC1459, new Pair<>("abcdwxyzABCDWXYZ!@#$%^&*(){}[];':,.<>", "abcdwxyzabcdwxyz!@#$%^&*(){}{};':,.<>"));

        for (CaseMapping caseMapping : CaseMapping.values()) {
            Assertions.assertTrue(test.containsKey(caseMapping), "Missing CaseMapping " + caseMapping.name());
        }
        for (Map.Entry<CaseMapping, Pair<String, String>> entry : test.entrySet()) {
            Assertions.assertEquals(entry.getKey().toLowerCase(entry.getValue().getLeft()), entry.getValue().getRight(), "Incorrect lowercasing");
            Assertions.assertTrue(entry.getKey().areEqualIgnoringCase(entry.getValue().getLeft(), entry.getValue().getRight()), "Incorrect equalsIgnoreCase");
        }
    }
}
