package com.dfire.platform.alchemy.api.function.scalar;

import com.dfire.platform.alchemy.api.common.Geoip;
import com.dfire.platform.alchemy.api.function.BaseFunction;
import com.dfire.platform.alchemy.api.util.geoip.GeoIpDatabase;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.*;
import org.apache.calcite.interpreter.Scalar;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.table.functions.TableFunction;
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
//                File database = new File("C:\\Users\\Administrator\\Downloads\\GeoLite2-City\\GeoLite2-City.mmdb");
//                DatabaseReader reader = new DatabaseReader.Builder(database).build();
            DatabaseReader reader = GeoIpDatabase.parse();
            InetAddress ipAddress = InetAddress.getByName(ip);
            Geoip geoip = new Geoip();
            CityResponse response = reader.city(ipAddress);
            Country country = response.getCountry();
            Continent continent = response.getContinent();
            geoip.setCountryName(country.getNames().get("en"));
            geoip.setCountryCode(country.getIsoCode());
            geoip.setContinentCode(continent.getCode());
            geoip.setIp(ip);
            City city = response.getCity();
            Location location = response.getLocation();
            geoip.setLatitude(location.getLatitude());
            geoip.setLongitude(location.getLongitude());
            com.dfire.platform.alchemy.api.common.Location geoLocation = new com.dfire.platform.alchemy.api.common.Location();
            geoip.setCityName(city.getName());

            geoLocation.setLat(location.getLatitude());
            geoLocation.setLon(location.getLongitude());
            List<Subdivision> subdivisions = response.getSubdivisions();
            if (CollectionUtils.isNotEmpty(subdivisions)){
                geoip.setRegionName(subdivisions.get(0).getNames().get("en"));
                geoip.setRegionCode(subdivisions.get(0).getIsoCode());
            }

            geoip.setLocation(geoLocation);
            geoip.setTimezone(location.getTimeZone());
            Map<String,Object> map = new HashMap<>();

             map.put("geoip",geoip);
             return map;
        } catch (Exception e) {
            logger.error("geo ip " + e);
//                e.getStackTrace();
        }
        return Collections.emptyMap();
    }


//    @Override
//    public TypeInformation<Geoip> getResultType() {
//        //return MapTypeInfo.of(HashMap.class);
//        return PojoTypeInfo.of(Geoip.class);
//    }
}


