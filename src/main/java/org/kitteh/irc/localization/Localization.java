/*
 * * Copyright (C) 2013-2014 Matt Baxter http://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.irc.localization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public enum Localization {
    BOT_PINGTIMEOUT("Ping timeout! Reconnecting..."),

    CHANNEL_KICK("%1$s kicked %2$s"),

    CTCP_FINGER("I playfully bite that finger"),
    CTCP_TIME("The time is apurrximately %s"),
    CTCP_VERSION("I am %s");

    public class LocaleItem {
        private final String locale;

        private LocaleItem(String locale) {
            this.locale = locale;
        }

        public String format(Object... objects) {
            return String.format(Localization.this.map.containsKey(this.locale) ? Localization.this.map.get(this.locale) : Localization.this.englishDefault, objects);
        }

        public String get() {
            return Localization.this.map.containsKey(this.locale) ? Localization.this.map.get(this.locale) : Localization.this.englishDefault;
        }
    }

    public static void load(File file, String lang) {
        if (!file.exists()) {
            return; // Just in case
        }
        final Yaml yaml = new Yaml();
        Map<?, ?> map = null;
        try {
            map = yaml.loadAs(new FileInputStream(file), Map.class);
        } catch (final FileNotFoundException e) {
        }
        if (map == null) {
            return;
        }
        final Object o = map.get(lang);
        if (o instanceof Map) {
            map = (Map<?, ?>) o;
        }
        for (final Localization local : Localization.values()) {
            local.set(lang, Localization.get(map, local.key));
        }
    }

    /**
     * I can be run to generate the default file! :3
     * 
     * @param args arguments, yo
     */
    public static void main(String[] args) {
        final Map<String, Object> map = new LinkedHashMap<>();
        final Map<String, Object> inner = new LinkedHashMap<>();
        map.put("en", inner);
        for (final Localization local : Localization.values()) {
            Localization.set(inner, local.key, local.englishDefault);
        }
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        final Yaml yaml = new Yaml(options);
        final String output = yaml.dump(map);
        System.out.println(output);
    }

    private static String get(Map<?, ?> map, String path) {
        final int indexOf = path.indexOf('.');
        if (indexOf != -1) {
            final Object value = map.get(path.substring(0, indexOf));
            return value instanceof Map ? Localization.get((Map<?, ?>) value, path.substring(indexOf + 1)) : null;
        }
        final Object value = map.get(path);
        return value instanceof String ? (String) value : null;
    }

    private static void set(Map<String, Object> map, String path, String value) {
        final int indexOf = path.indexOf('.');
        if (indexOf != -1) {
            final String current = path.substring(0, indexOf);
            Map<String, Object> m;
            final Object o = map.get(current);
            if (o instanceof Map) {
                m = Localization.totallySafeCheckedMapCast((Map<?, ?>) o);
            } else {
                m = new LinkedHashMap<>();
                map.put(current, m);
            }
            Localization.set(m, path.substring(indexOf + 1), value);
        } else {
            map.put(path, value);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> totallySafeCheckedMapCast(Map<?, ?> definitelyChecked) {
        return (Map<String, Object>) definitelyChecked;
    }

    private final Map<String, String> map = new HashMap<>();
    private final Map<String, LocaleItem> localeItemMap = new HashMap<>();
    private final String englishDefault;
    private final String key;

    private Localization(String englishDefault) {
        this.map.put("en", englishDefault);
        this.englishDefault = englishDefault;
        this.key = this.name().toLowerCase().replace('_', '.');
    }

    public LocaleItem locale(String locale) {
        if (!this.localeItemMap.containsKey(locale)) {
            this.localeItemMap.put(locale, new LocaleItem(locale));
        }
        return this.localeItemMap.get(locale);
    }

    private void set(String lang, String string) {
        System.out.println("Setting " + this.name() + " to " + string);
        this.map.put(lang, string == null ? this.englishDefault : string);
    }
}