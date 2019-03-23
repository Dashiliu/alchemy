package com.dfire.platform.alchemy.api.util;

import com.maxmind.geoip2.DatabaseReader;

import java.io.File;
import java.io.IOException;

/**
 * Created by yuntun on 2019/3/20 0020.
 */
public class GeoIpDatabase {

    private static final String REGEX_YAML_PATH = "/GeoLite2-City.mmdb";

    private static DatabaseReader databaseReader = null;

    private GeoIpDatabase() {
    }

    public static DatabaseReader getDatabaseReader() {
        if (databaseReader == null){
            databaseReader = databaseBuild();
        }
        return databaseReader;
    }

    private static DatabaseReader databaseBuild() {
        File database = new File(REGEX_YAML_PATH);
        try {
            DatabaseReader reader = new DatabaseReader.Builder(database).build();
            return reader;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
