package com.dfire.platform.alchemy.web.util;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.map.HashedMap;

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

    public static Properties fromYamlMap(Map<String, Object> properties) {
        Map<String, Object> prop = new HashedMap();
        for (Object object : properties.values()) {
            if (object instanceof Map) {
                Map<String, Object> value = (Map<String, Object>)object;
                Object key = value.get("key");
                if (key == null) {
                    continue;
                }
                prop.put(String.valueOf(value.get("key")), value.get("value"));
            }
        }
        return createProperties(prop);
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
