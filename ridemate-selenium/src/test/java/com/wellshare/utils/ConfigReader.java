package com.wellshare.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Reads values from config.properties
 * Usage: ConfigReader.get("base.url")
 */
public class ConfigReader {

    private static Properties props = new Properties();

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
        String value = props.getProperty(key);
        if (value == null) throw new RuntimeException("Missing key in config: " + key);
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
