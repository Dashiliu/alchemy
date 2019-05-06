package com.dfire.platform.alchemy.function.logstash;

import com.dfire.platform.alchemy.function.BaseFunction;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.ScalarFunction;

/***
 *
 * gsub    替换
 * select gsub(field_name)
 */
public class GsubFunction extends ScalarFunction implements BaseFunction{

    private static final String FUNCTION_NANME = "GSUB";

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }


    public String eval(String input, String regex, String replacement) {
        if (StringUtils.isNotBlank(input)) {
            return input.replaceAll(regex, replacement);
        }
        return input;
    }
}
