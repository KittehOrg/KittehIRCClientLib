package org.kitteh.irc.client.library;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.feature.CaseMapping;
import org.kitteh.irc.client.library.util.Pair;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests case mappings.
 */
public class CaseMappingTest {
    /**
     * Tests casemapping lowercasing.
     */
    @Test
    public void lowerCase() {
        Map<CaseMapping, Pair<String, String>> test = new HashMap<>();
        test.put(CaseMapping.ASCII, new Pair<>("abcdwxyzABCDWXYZ!@#$%^&*(){}[];':,.<>", "abcdwxyzabcdwxyz!@#$%^&*(){}[];':,.<>"));
        test.put(CaseMapping.RFC1459, new Pair<>("abcdwxyzABCDWXYZ!@#$%^&*(){}[];':,.<>", "abcdwxyzabcdwxyz!@#$%~&*(){}{};':,.<>"));
        test.put(CaseMapping.STRICT_RFC1459, new Pair<>("abcdwxyzABCDWXYZ!@#$%^&*(){}[];':,.<>", "abcdwxyzabcdwxyz!@#$%^&*(){}{};':,.<>"));
        test.put(CaseMapping.RFC8265, new Pair<>("ABCDwxyz[]^~ＫｉｔｔｅｈΣΑΣ Straße", "abcdwxyz[]^~kittehσασ straße"));

        Arrays.stream(CaseMapping.class.getDeclaredFields())
                .filter(field -> Modifier.isPublic(field.getModifiers()) && CaseMapping.class.isAssignableFrom(field.getType()))
                .forEach(field -> {
                            try {
                                Assertions.assertTrue(test.containsKey((CaseMapping) field.get(null)), "Missing CaseMapping " + field.getName());
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );

        for (Map.Entry<CaseMapping, Pair<String, String>> entry : test.entrySet()) {
            Assertions.assertEquals(entry.getKey().toLowerCase(entry.getValue().getLeft()), entry.getValue().getRight(), "Incorrect lowercasing");
            Assertions.assertTrue(entry.getKey().areEqualIgnoringCase(entry.getValue().getLeft(), entry.getValue().getRight()), "Incorrect equalsIgnoreCase");
        }
    }

    /**
     * Tests PRECIS cases that ASCII lowercasing gets wrong, checked against Ergo.
     */
    @Test
    public void rfc8265() {
        CaseMapping precis = CaseMapping.RFC8265;
        // Lowercasing, not case folding, as in Ergo (Go x/text precis)
        Assertions.assertFalse(precis.areEqualIgnoringCase("STRASSE", "straße"));
        Assertions.assertTrue(precis.areEqualIgnoringCase("ẞ", "ß"));
        // Capital sigma always lowercases to sigma, never final sigma
        Assertions.assertTrue(precis.areEqualIgnoringCase("ΟΔΟΣ", "οδοσ"));
        Assertions.assertFalse(precis.areEqualIgnoringCase("ΟΔΟΣ", "οδος"));
        // Dotless i lowercases only to itself
        Assertions.assertFalse(precis.areEqualIgnoringCase("ı", "i"));
        Assertions.assertFalse(precis.areEqualIgnoringCase("ı", "I"));
        Assertions.assertTrue(precis.areEqualIgnoringCase("İ", "i\u0307"));
        // Width mapping, including halfwidth katakana composing under NFC
        Assertions.assertTrue(precis.areEqualIgnoringCase("ＫＩＴＴＥＨ", "kitteh"));
        Assertions.assertTrue(precis.areEqualIgnoringCase("ｶﾞ", "ガ"));
        // NFC
        Assertions.assertTrue(precis.areEqualIgnoringCase("E\u0301", "é"));
        // ASCII is untouched beyond A-Z
        Assertions.assertFalse(precis.areEqualIgnoringCase("[", "{"));
    }
}
