package com.dfire.platform.alchemy.function.logstash.util.geoip;

import com.maxmind.geoip2.DatabaseReader;

/**
 * Created by yuntun on 2019/3/20 0020.
 */
public class GeoIpDatabase {

    public static Parser geoIpParser = null;


    private GeoIpDatabase() {
    }

    static {
        if (geoIpParser == null) {
            geoIpParser = new Parser();
        }
    }

    public static DatabaseReader parse() {
        return geoIpParser.getDatabaseReader();
    }

}
