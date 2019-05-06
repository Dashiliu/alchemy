package com.dfire.platform.alchemy.function.logstash.util.geoip;

import com.maxmind.geoip2.DatabaseReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: yuntun
 * Date: 2019/3/23
 * Time: 10:14
 * Description:
 */
public class Parser {

    private static final Logger logger = LoggerFactory.getLogger(GeoIpDatabase.class);

    private static final String REGEX_MMDB_PATH = "/GeoLite2-City.mmdb";

    private static DatabaseReader databaseReader = null;


    public Parser() {
        this(Parser.class.getResourceAsStream(REGEX_MMDB_PATH));
    }

    public Parser(InputStream regexYaml) {
        initialize(regexYaml);
    }

    private void initialize(InputStream inputStream) {
        try {
            databaseReader = new DatabaseReader.Builder(inputStream).build();
        } catch (IOException e) {
            logger.error("create geoip parser error.", e);
        }
    }

    public DatabaseReader getDatabaseReader() {
        return databaseReader;
    }
}
