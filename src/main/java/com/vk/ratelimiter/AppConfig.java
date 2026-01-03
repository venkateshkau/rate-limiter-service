package com.vk.ratelimiter;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream is = AppConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (is != null) {
                PROPS.load(is);
            }

        } catch (Exception e) {
            //throw new RuntimeException("Failed to load configuration", e);
        }
    }

    private AppConfig() {}

    public static String getString(String key, String defaultValue) {
        return PROPS.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String value = PROPS.getProperty(key, String.valueOf(defaultValue));
        int intVal;
        try {
            intVal = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException( " Port number " + value + " is not an integer value");
        }
        return intVal;
    }
}
