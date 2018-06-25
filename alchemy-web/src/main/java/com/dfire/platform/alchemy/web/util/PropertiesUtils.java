package com.dfire.platform.alchemy.web.util;

import java.util.Map;
import java.util.Properties;

public class PropertiesUtils {

    private PropertiesUtils() {}

    public static Properties getProperties(Map<String, String> prop) {
        Properties p = new Properties();
        for (Map.Entry<String, String> e : prop.entrySet()) {
            p.put(e.getKey(), e.getValue());
        }
        return p;
    }

    public static Integer getProperty(Integer value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static Long getProperty(Long value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
