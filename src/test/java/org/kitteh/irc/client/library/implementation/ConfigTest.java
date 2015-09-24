package org.kitteh.irc.client.library.implementation;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Tests the config class.
 */
public class ConfigTest {
    /**
     * Tests for attempts at null values in the declared Config entries.
     *
     * @throws IllegalAccessException if something goes wrong
     * @throws NoSuchFieldException if something goes wrong
     */
    @Test
    public void safeType() throws IllegalAccessException, NoSuchFieldException {
        Field typeField = Config.Entry.class.getDeclaredField("type");
        typeField.setAccessible(true);
        for (Field field : Config.class.getDeclaredFields()) {
            if (field.getType().equals(Config.Entry.class) && ((field.getModifiers() & (Modifier.PRIVATE | Modifier.PROTECTED | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.STATIC)) {
                Config.Entry<?> entry = (Config.Entry<?>) field.get(null);
                Assert.assertNotNull("Null type found for " + field.getName(), typeField.get(entry));
            }
        }
    }
}
