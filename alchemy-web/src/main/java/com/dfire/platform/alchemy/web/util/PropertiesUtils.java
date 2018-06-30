package com.dfire.platform.alchemy.web.util;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.dfire.platform.alchemy.web.common.Pair;

public class PropertiesUtils {

    private PropertiesUtils() {}

    public static Properties getProperties(Map<String, String> prop) {
        Properties p = new Properties();
        for (Map.Entry<String, String> e : prop.entrySet()) {
            p.put(e.getKey(), e.getValue());
        }
        return p;
    }

    public static Properties createProperties(Map<String, Object> prop) {
        Properties p = new Properties();
        for (Map.Entry<String, Object> e : prop.entrySet()) {
            p.put(e.getKey(), e.getValue());
        }
        return p;
    }

    public static Properties getProperties(List<Pair<String, String>> prop) {
        Properties p = new Properties();
        for (Pair<String, String> e : prop) {
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
