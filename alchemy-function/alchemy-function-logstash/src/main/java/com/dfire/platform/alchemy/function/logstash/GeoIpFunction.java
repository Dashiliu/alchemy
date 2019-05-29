package com.dfire.platform.alchemy.function.logstash;

import com.dfire.platform.alchemy.function.BaseFunction;
import com.dfire.platform.alchemy.function.logstash.util.geoip.GeoIpDatabase;
import com.dfire.platform.alchemy.function.logstash.util.geoip.Geoip;
import com.dfire.platform.alchemy.function.logstash.util.geoip.Location;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Continent;
import com.maxmind.geoip2.record.Subdivision;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.table.shaded.org.apache.commons.collections.CollectionUtils;
import org.apache.flink.table.shaded.org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 *
 * gsub    替换
 * select gsub(field_name)
 */
public class GeoIpFunction extends ScalarFunction implements BaseFunction {

    private static final String FUNCTION_NANME = "GEOIP";
    private static final Logger logger = LoggerFactory.getLogger(GeoIpFunction.class);

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }

    public Map<String,Object> eval(String ip) {
        if (StringUtils.isBlank(ip)) {
            return Collections.EMPTY_MAP;
        }
        try {
            DatabaseReader reader = GeoIpDatabase.parse();
            InetAddress ipAddress = InetAddress.getByName(ip);
            Geoip geoip = new Geoip();
            CityResponse response = reader.city(ipAddress);
            Country country = response.getCountry();
            Continent continent = response.getContinent();
            geoip.setCountry_name(country.getNames().get("en"));
            geoip.setCountry_code(country.getIsoCode());
            geoip.setContinent_code(continent.getCode());
            geoip.setIp(ip);
            City city = response.getCity();
            com.maxmind.geoip2.record.Location location = response.getLocation();
            geoip.setLatitude(location.getLatitude());
            geoip.setLongitude(location.getLongitude());
            Location geoLocation = new Location();
            geoip.setCity_name(city.getName());

            geoLocation.setLat(location.getLatitude());
            geoLocation.setLon(location.getLongitude());
            List<Subdivision> subdivisions = response.getSubdivisions();
            if (CollectionUtils.isNotEmpty(subdivisions)){
                geoip.setRegion_name(subdivisions.get(0).getNames().get("en"));
                geoip.setRegion_code(subdivisions.get(0).getIsoCode());
            }

            geoip.setLocation(geoLocation);
            geoip.setTimezone(location.getTimeZone());
            Map<String,Object> map = new HashMap<>();

             map.put("geoip",geoip);
             return map;
        } catch (Exception e) {
            logger.error("geo ip " + e);
        }
        return Collections.emptyMap();
    }
}


