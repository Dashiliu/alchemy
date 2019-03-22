package com.dfire.platform.alchemy.api.function.scalar;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.ScalarFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/***
 *
 * Urldecode    url转码
 * select urldecode(field_name)
 */
public class UrldecodeFunction extends ScalarFunction {
    private static final Logger logger = LoggerFactory.getLogger(UrldecodeFunction.class);

    public String eval(String input) {
        if (StringUtils.isNotBlank(input)) {
            try {
                return URLDecoder.decode(input, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("URLDecode failed", e);
            }
        }
        return input;
    }
}
