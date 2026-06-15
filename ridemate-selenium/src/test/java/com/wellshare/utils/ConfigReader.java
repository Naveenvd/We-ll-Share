package com.wellshare.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads values from config.properties.
 * Usage: ConfigReader.get("base.url")
 *
 * Two extras make the same suite run against any environment:
 *  1. System properties override file values, so you can point the suite at
 *     a deployed app without editing files:
 *         mvn test -Dbase.url=https://wellshare-frontend.onrender.com
 *  2. Values may reference other keys with ${...} (e.g. app.login.url builds
 *     on base.url), so overriding base.url cascades to every page URL.
 */
public class ConfigReader {

    private static final Properties props = new Properties();
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    static {
        try {
            FileInputStream fis = new FileInputStream(
                "src/test/resources/config.properties"
            );
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config.properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        String value = resolve(key);
        if (value == null) throw new RuntimeException("Missing key in config: " + key);
        return expand(value).trim();
    }

    /** System property wins over the file, so -Dkey=value overrides at runtime. */
    private static String resolve(String key) {
        String sys = System.getProperty(key);
        return (sys != null) ? sys : props.getProperty(key);
    }

    /** Replace ${other.key} references (one level deep — enough for base.url). */
    private static String expand(String value) {
        Matcher m = PLACEHOLDER.matcher(value);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String ref = resolve(m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(ref == null ? "" : ref.trim()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
