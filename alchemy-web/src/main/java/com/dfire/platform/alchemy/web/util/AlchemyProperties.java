package com.dfire.platform.alchemy.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author congbai
 * @date 2019/5/6
 */
public class AlchemyProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlchemyProperties.class);

    private static Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(AlchemyProperties.class.getClassLoader().getResourceAsStream("META-INF/alchemy.properties"));
        } catch (IOException e) {
            LOGGER.error("Failed load alchemy properties", e);
        }
    }
    public static String get(String key){
        return properties.getProperty(key);
    }
}
