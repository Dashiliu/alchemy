package com.dfire.platform.web.configuration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
public class WebConfigurer extends WebMvcConfigurerAdapter {

    private final Logger log = LoggerFactory.getLogger(WebConfigurer.class);

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = null;
        if (converters.size() == 0) {
            jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
            converters.add(jackson2HttpMessageConverter);
        } else {
            jackson2HttpMessageConverter = (MappingJackson2HttpMessageConverter)converters.get(0);
        }
        ObjectMapper objectMapper = jackson2HttpMessageConverter.getObjectMapper();
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        SimpleModule simpleModule = new SimpleModule();
        // 序列换成json时,将所有的long变成string 因为js中得数字类型不能包含所有的java long值
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
        jackson2HttpMessageConverter.setObjectMapper(objectMapper);
    }
}
