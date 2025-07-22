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
}
