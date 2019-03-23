package com.dfire.platform.alchemy.api.function.scalar;

import com.dfire.platform.alchemy.api.common.Geoip;
import com.dfire.platform.alchemy.api.util.GeoIpDatabase;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Continent;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.TableFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple1;

import java.net.InetAddress;

/***
 *
 * gsub    替换
 * select gsub(field_name)
 */
public class GeoIpFunction extends TableFunction<Tuple1<Geoip>> {
    private static final Logger logger = LoggerFactory.getLogger(GeoIpFunction.class);

    public void eval(String ip) {
        if (StringUtils.isNotBlank(ip)) {
            try {
//                File database = new File("C:\\Users\\Administrator\\Downloads\\GeoLite2-City\\GeoLite2-City.mmdb");
//                DatabaseReader reader = new DatabaseReader.Builder(database).build();
                DatabaseReader reader = GeoIpDatabase.getDatabaseReader();
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
                com.dfire.platform.alchemy.api.common.Location location1 = new com.dfire.platform.alchemy.api.common.Location();
                geoip.setCityName(city.getName());

                location1.setLat(location.getLatitude());
                location1.setLon(location.getLongitude());

                geoip.setLocation(location1);
                collect(new Tuple1<>(geoip));
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    }
}


