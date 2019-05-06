package com.dfire.platform.alchemy.function.logstash;

import com.dfire.platform.alchemy.function.BaseFunction;
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
public class UrldecodeFunction extends ScalarFunction implements BaseFunction{

    private static final String FUNCTION_NANME = "URLDECODE";
    private static final Logger logger = LoggerFactory.getLogger(UrldecodeFunction.class);

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }

    public String eval(Object input) {
        if (input != null) {
            String message = (String)input;
            try {
                return URLDecoder.decode(message, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("URLDecode failed", e);
            }
        }
        return null;
    }
}
